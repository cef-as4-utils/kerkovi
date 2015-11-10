package controllers

import java.io.{FileOutputStream, ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URL

import minder.as4Utils.AS4Utils
import model.AS4Gateway
import org.w3c.dom.Element
import play.api.Logger
import play.api.libs.iteratee.Enumerator
import play.api.mvc._

import scala.collection.JavaConversions._

import utils.Util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

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
  def getCorner4() = Action{ request =>
    BadRequest("Please use POST")
  }

  def process(request: Request[RawBuffer]): Result = {
    Logger.info("AS4 Interceptor received a message")
    try {
      val sOAPMessage = Util.extractSOAPMessageFromRequest(request)
     // Try {
     //   val fos = new FileOutputStream("sampleas4message.txt")
     //   sOAPMessage.writeTo(fos);
     //   fos.close();
     // }
      val toPartyId: Element = AS4Utils.findSingleNode(sOAPMessage.getSOAPHeader, "//:PartyInfo/:To/:PartyId")

      val content: String = toPartyId.getTextContent
      //find the matching party in the database
      //and immediately forward it.
      val gateway: AS4Gateway = Databeyz.findByPartyId(content)

      if (gateway == null) {
        return Util.sendFault(null, "A gateway with party id [" + content + "] not found")
      }

      Logger.info("Target gateway " + gateway.partyID + " ==> " + gateway.address)

      if (gateway.proxyMode) {
        Logger.debug("Send message to " + gateway.address)
        val reply = AS4Utils.sendSOAPMessage(sOAPMessage, new URL(gateway.address))

        Logger.debug("Reply received")
        Logger.debug(AS4Utils.prettyPrint(reply.getSOAPPart));
        //TODO: wrong code
        val k = reply.getMimeHeaders.getAllHeaders.toSeq.map(elm => (elm.toString -> reply.getMimeHeaders.getHeader(elm.toString).toString))

        val baos = new ByteArrayOutputStream()
        reply.writeTo(baos)
        val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(new ByteArrayInputStream(baos.toByteArray))
        return Ok.chunked(dataContent).withHeaders(k: _*)
      }

      //forward to minder
      if (!Global.as4Adapter.isRunning) {
        Logger.warn("Test Not Started. Bad Request!")
        return BadRequest("<html><head><title>Error</title></head><body><h1>Sorry, Minder Test Not Active</h1></body></html>".getBytes)
      }

      //signal the minder adapter
      Global.as4Adapter.messageReceived(AS4Utils.serializeSOAPMessage(sOAPMessage.getMimeHeaders, sOAPMessage));
      //wait for adapter to return the receipt
      val reply = Global.as4Adapter.consumeReceipt()

      if (reply == null) {
        Logger.warn("Receipt is null. Send bad request")
        return BadRequest("Coudln't receive receipt")
      }
      //return the reply to the client
      val baos = new ByteArrayOutputStream()
      reply.writeTo(baos)
      val array: Array[Byte] = baos.toByteArray
      val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(new ByteArrayInputStream(array))

      if (reply.countAttachments() > 0)
        return Ok.chunked(dataContent).as("multipart/related")
      else
        return Ok.chunked(dataContent).as("application/soap+xml;charset=UTF-8")
    } catch {
      case th: Throwable => {
        Logger.error(th.getMessage, th)
        BadRequest(th.getMessage)
      }
    }
  }

  def as4Interceptor() = Action(parse.raw) {
    request => process(request)
  }
}