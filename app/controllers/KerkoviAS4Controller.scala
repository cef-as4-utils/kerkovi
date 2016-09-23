package controllers

import java.io.{FileOutputStream, ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URL
import java.util
import javax.xml.soap.{MimeHeader, SOAPMessage}

import akka.stream.IOResult
import akka.stream.scaladsl.{StreamConverters, Source}
import akka.util.ByteString
import minder.as4Utils.SWA12Util
import model.AS4Gateway
import org.w3c.dom.Element
import play.api.Logger
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import utils.{Tic, Util}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KerkoviAS4Controller extends Controller {

  def postCorner1() = Action(parse.raw) { request =>
    Logger.debug("Corner 1 received a message")
    KerkoviApplicationContext.genericCorner1.process(request)
  }

  def postCorner4() = Action(parse.raw) { request =>
    Logger.debug("Corner 4 received a message")
    KerkoviApplicationContext.genericCorner4.process(request)
  }

  def getCorner1() = Action { request =>
    BadRequest("Please use POST")
  }

  def getCorner4() = Action { request =>
    BadRequest("Please use POST")
  }

  def process(request: Request[RawBuffer]): Result = {
    Logger.info("AS4 Interceptor received a message")
    val logItem = new LogItem();

    try {
      val sOAPMessage = Util.extractSOAPMessageFromRequest(request)

      val toPartyId: Element = SWA12Util.findSingleNode(sOAPMessage.getSOAPHeader, "//:PartyInfo/:To/:PartyId")
      val fromPartyId: Element = SWA12Util.findSingleNode(sOAPMessage.getSOAPHeader, "//:PartyInfo/:From/:PartyId")
      val conversationId: Element = SWA12Util.findSingleNode(sOAPMessage.getSOAPHeader, "//:eb:CollaborationInfo/:ConversationId")

      logItem.valid = true;
      logItem.setAS4Message(sOAPMessage)
      logItem.toPartyId = toPartyId.getTextContent;
      logItem.fromPartyId = fromPartyId.getTextContent;
      logItem.messageType = "Interceptor"
      logItem.conversationId = conversationId.getTextContent

      var toPartyIdStr: String = toPartyId.getTextContent

      if (logItem.toPartyId.indexOf(':') != -1) {
        logItem.toPartyId = logItem.toPartyId.substring(logItem.toPartyId.lastIndexOf(':') + 1);
      }

      if (logItem.fromPartyId.indexOf(':') != -1) {
        logItem.fromPartyId = logItem.fromPartyId.substring(logItem.fromPartyId.lastIndexOf(':') + 1);
      }


      Logger.info("A message from " + logItem.fromPartyId + " to  " + logItem.toPartyId)

      //find the matching party in the database
      //and immediately forward it.
      val gateway: AS4Gateway = Databeyz.findByPartyId(logItem.toPartyId)

      if (gateway == null) {
        logItem.setException(new IllegalArgumentException("A gateway with party id [" + logItem.toPartyId + "] not found"))
        return Util.sendFault(null, "A gateway with party id [" + logItem.toPartyId + "] not found")
      }
      if (gateway.proxyMode) {
        Logger.info("Will send the message directly to the receiving party")
        Logger.info("Target gateway " + gateway.partyID + " ==> " + gateway.mshAddress)
        logItem.directMode = true;
        Logger.info("Send message to " + gateway.mshAddress)
        val reply = SWA12Util.sendSOAPMessage(sOAPMessage, new URL(gateway.mshAddress))
        //        var bytes = SWA12Util.serializeSOAPMessage(sOAPMessage.getMimeHeaders, sOAPMessage);
        //        var fileOutputStream = new FileOutputStream("domibus-c2-message.txt");
        //        fileOutputStream.write(bytes);
        //        fileOutputStream.close();

        if (reply == null) {
          logItem.success = LogItemSuccess.UNKNOWN
          return Ok //empty response
        } else {
          logItem.setReply(reply)
          Logger.info("Reply received")
          Logger.debug(SWA12Util.prettyPrint(reply.getSOAPPart));
          //        bytes = SWA12Util.serializeSOAPMessage(reply.getMimeHeaders, reply);
          //        fileOutputStream = new FileOutputStream("domibus-c3-reply.txt");
          //        fileOutputStream.write(bytes);
          //        fileOutputStream.close();

          logItem.success = LogItemSuccess.TRUE;
          try {
            val elm = utils.Util.evaluateXpath("//:SignalMessage/:Error", reply.getSOAPPart)
            if (elm != null)
              logItem.success = LogItemSuccess.FALSE;
          } catch {
            case th : Throwable =>
          }

          val baos = new ByteArrayOutputStream()
          reply.writeTo(baos)
          Logger.debug("Reply headers")
          val headers: Seq[(String, String)] = populateHeaders(reply)
          val source: Source[ByteString, Future[IOResult]] = StreamConverters.fromInputStream(() => new ByteArrayInputStream(baos.toByteArray))
          return Ok.chunked(source).withHeaders(headers: _*)
        }
      } else {

        logItem.directMode = false;

        //forward to minder
        if (!KerkoviApplicationContext.isTestActive) {
          Logger.warn("Test Not Started. Bad Request!")
          logItem.setException(new RuntimeException("Test Not Started. Bad Request!"))
          return Forbidden("Sorry, Minder Test Not Active".getBytes)
        }

        Logger.info("Forward the message to Minder")

        //signal the minder adapter
        Tic.tic()
        KerkoviApplicationContext.as4Adapter.messageReceived(SWA12Util.serializeSOAPMessage(sOAPMessage.getMimeHeaders, sOAPMessage));
        //wait for adapter to return the receipt

        Logger.debug("Wait for minder response")
        val reply = KerkoviApplicationContext.as4Adapter.consumeReceipt()
        Logger.debug(">>>>>> Timeout between request and reply: " + Tic.toc)

        if (reply == null) {
          Logger.warn("Receipt is null. Send bad request")
          logItem.setException(new RuntimeException("Receipt is null. Send bad request"))
          logItem.success = LogItemSuccess.FALSE;
          return ServiceUnavailable("")
        }

        logItem.setReply(reply)
        logItem.success = LogItemSuccess.TRUE;
        try {
          val elm = utils.Util.evaluateXpath("//:SignalMessage/:Error", reply.getSOAPPart)
          if (elm != null)
            logItem.success = LogItemSuccess.FALSE;
        } catch {
          case th : Throwable =>
        }

        val baos = new ByteArrayOutputStream()
        reply.writeTo(baos)
        Logger.debug("Reply headers")
        val headers: Seq[(String, String)] = populateHeaders(reply)

        return Ok.chunked(StreamConverters.fromInputStream(() => new ByteArrayInputStream(baos.toByteArray))).withHeaders(headers: _*)
      }
    } catch {
      case th: Throwable => {
        Logger.error(th.getMessage, th)
        logItem.success = LogItemSuccess.FALSE;
        logItem.setException(th);
        InternalServerError(th.getMessage)
      }
    } finally {
      if (logItem.valid) logItem.persist()
    }
  }

  def populateHeaders(reply: SOAPMessage): Seq[(String, String)] = {
    val list = new util.ArrayList[(String, String)]()
    for (mime <- reply.getMimeHeaders.getAllHeaders) {
      val mm = mime.asInstanceOf[MimeHeader]

      Logger.debug(mm.getName + " + " + mm.getValue)

      if (!"Content-Length".equalsIgnoreCase(mm.getName)) {
        list.add(mm.getName -> mm.getValue)
      }
    }

    val headers = list.toSeq;


    for (kv <- headers) {
      Logger.info(kv._1 + ":" + kv._2)
    }
    headers
  }

  def as4Interceptor() = Action(parse.raw) {
    request => process(request)
  }

  def zeroAS4() = Action(parse.raw) { request =>
    Logger.info("Zero endpoint received a message")
    try {
      val sOAPMessage = Util.extractSOAPMessageFromRequest(request)
      Logger.info("Send success receipt")
      Util.sendSuccessResponse(sOAPMessage);
    } catch {
      case th: Throwable => {
        Logger.error(th.getMessage, th)
        BadRequest(th.getMessage)
      }
    }
  }

  def as4InterceptorError() = Action(parse.raw) {
    request => Ok("Please use POST")
  }
}