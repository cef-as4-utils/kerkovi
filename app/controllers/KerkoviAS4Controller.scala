package controllers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URL
import java.util
import javax.xml.soap.{MimeHeader, SOAPMessage}

import minder.as4Utils.SWA12Util
import model.AS4Gateway
import org.w3c.dom.Element
import play.api.Logger
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import utils.Util

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

object KerkoviAS4Controller extends Controller {

  def postCorner1() = Action(parse.raw) { request =>
    Logger.debug("Corner 1 received a message")
    Global.genericCorner1.process(request)
  }

  def postCorner4() = Action(parse.raw) { request =>
    Logger.debug("Corner 1 received a message")
    Global.genericCorner4.process(request)
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

      logItem.valid = true;
      logItem.setAS4Message(sOAPMessage)
      logItem.toPartyId = toPartyId.getTextContent;
      logItem.fromPartyId = fromPartyId.getTextContent;
      logItem.messageType = "Interceptor"

      val content: String = toPartyId.getTextContent
      //find the matching party in the database
      //and immediately forward it.
      val gateway: AS4Gateway = Databeyz.findByPartyId(content)

      if (gateway == null) {
        logItem.setException(new IllegalArgumentException("A gateway with party id [" + content + "] not found"))
        return Util.sendFault(null, "A gateway with party id [" + content + "] not found")
      }

      Logger.info("Target gateway " + gateway.partyID + " ==> " + gateway.mshAddress)

      if (gateway.proxyMode) {
        logItem.directMode = true;
        Logger.info("Send message to " + gateway.mshAddress)
        val reply = SWA12Util.sendSOAPMessage(sOAPMessage, new URL(gateway.mshAddress))

        logItem.setReply(reply)

        //var bytes = SWA12Util.serializeSOAPMessage(sOAPMessage.getMimeHeaders, sOAPMessage);
        //var fileOutputStream = new FileOutputStream("david.txt");
        //fileOutputStream.write(bytes);
        //fileOutputStream.close();

        Logger.info("Reply received")
        Logger.debug(SWA12Util.prettyPrint(reply.getSOAPPart));
        //bytes = SWA12Util.serializeSOAPMessage(reply.getMimeHeaders, reply);
        //fileOutputStream = new FileOutputStream("reply.txt");
        //fileOutputStream.write(bytes);
        //fileOutputStream.close();

        logItem.success = true;
        try {
          val elm = utils.Util.evaluateXpath("//:SignalMessage/:Error", reply.getSOAPPart)
          if (elm != null)
            logItem.success = false;
        } catch {
          case _ =>
        }

        val baos = new ByteArrayOutputStream()
        reply.writeTo(baos)
        Logger.debug("Reply headers")


        val headers: Seq[(String, String)] = populateHeaders(reply)
        val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(new ByteArrayInputStream(baos.toByteArray))

        return Ok.chunked(dataContent).withHeaders(headers: _*)
      } else {
        logItem.directMode = false;

        //forward to minder
        if (!Global.as4Adapter.isRunning) {
          Logger.warn("Test Not Started. Bad Request!")
          logItem.setException(new RuntimeException("Test Not Started. Bad Request!"))
          return BadRequest("<html><head><title>Error</title></head><body><h1>Sorry, Minder Test Not Active</h1></body></html>".getBytes)
        }

        //signal the minder adapter
        Global.as4Adapter.messageReceived(SWA12Util.serializeSOAPMessage(sOAPMessage.getMimeHeaders, sOAPMessage));
        //wait for adapter to return the receipt
        val reply = Global.as4Adapter.consumeReceipt()
        logItem.setReply(reply)

        if (reply == null) {
          Logger.warn("Receipt is null. Send bad request")
          logItem.setException(new RuntimeException("Receipt is null. Send bad request"))
          return BadRequest("Couldn't receive receipt")
        }

        logItem.success = true;
        try {
          val elm = utils.Util.evaluateXpath("//:SignalMessage/:Error", reply.getSOAPPart)
          if (elm != null)
            logItem.success = false;
        } catch {
          case _ =>
        }

        //return the reply to the client
        val baos = new ByteArrayOutputStream()
        reply.writeTo(baos)
        Logger.debug("Reply headers")
        val headers: Seq[(String, String)] = populateHeaders(reply)
        val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(new ByteArrayInputStream(baos.toByteArray))
        return Ok.chunked(dataContent).withHeaders(headers: _*)
      }
    } catch {
      case th: Throwable => {
        Logger.error(th.getMessage, th)
        logItem.success = false;
        logItem.setException(th);
        BadRequest(th.getMessage)
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
}