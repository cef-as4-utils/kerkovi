package backend

import java.io.ByteArrayInputStream
import java.net.URL
import javax.xml.soap.SOAPMessage

import controllers.{Databeyz, Global, LogItem, LogItemSuccess}
import esens.wp6.esensMshBackend._
import minder.as4Utils.SWA12Util
import model.AS4Gateway
import org.w3c.dom.{Element, Node}
import play.api.Logger
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{RawBuffer, Request, Result}
import utils.Util._
import utils.{Tic, Util}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
  * Author: yerlibilgin
  * Date: 04/08/15.
  */
class GenericAS4Corner extends AbstractMSHBackend {
  var label: String = "Corner"

  /**
    * Convert the submissiondata object into an AS4 object and send it to the MSH
    *
    * @param submissionData
    * @return
    */
  def submitMessage(submissionData: SubmissionData): Unit = {
    val logItem = new LogItem();
    try {
      Logger.debug("[" + label + "] wants to submit a message to [" + submissionData.from + "]")

      //update the message id (if its null, generate one)
      if (submissionData.messageId == null) {
        submissionData.messageId = Util.genereateEbmsMessageId("mindertestbed.org")
      }

      val message: SOAPMessage = Util.convert2Soap(submissionData, Global.myPartyID,
        submissionData.from, "Submit", Global.serviceProperties.getProperty(submissionData.pModeId),
        Global.actionProperties.getProperty(submissionData.pModeId))

      Logger.debug("[" + label + "] Submit AS4 Message to backend")
      Logger.debug(SWA12Util.describe(message))
      Logger.debug("====================")

      val toPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:To/:PartyId")
      val fromPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:From/:PartyId")
      val conversationId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:eb:CollaborationInfo/:ConversationId")

      logItem.valid = true;
      logItem.setAS4Message(message)
      logItem.toPartyId = toPartyId.getTextContent;
      logItem.fromPartyId = fromPartyId.getTextContent;
      logItem.messageType = "Submission " + label
      logItem.conversationId = conversationId.getTextContent

      val address: URL = resolveBackendAddress(submissionData)

      Logger.debug("URL of the gateway: " + address)
      Tic.tic()
      val reply: SOAPMessage = SWA12Util.sendSOAPMessage(message, address)
      Logger.debug("Sending message to [" + label + "] took " + Tic.toc() + " milliseconds")
      if (reply != null) {
        logItem.setReply(reply);

        logItem.success = LogItemSuccess.TRUE;
        try {
          val elm = utils.Util.evaluateXpath("//:SignalMessage/:Error", reply.getSOAPPart)
          if (elm != null)
            logItem.success = LogItemSuccess.TRUE;
        } catch {
          case _ =>
        }
        Logger.debug("[" + label + "] Receipt received from the AS4 backend")
        Logger.debug(SWA12Util.describe(reply))
        Logger.debug("====================")
      } else {
        logItem.success = LogItemSuccess.UNKNOWN
      }
    } catch {
      case th: Throwable => {
        Logger.error(th.getMessage, th)
        logItem.setException(th);
      }
    } finally {
      if (logItem.valid) logItem.persist()
    }

  }

  def resolveBackendAddress(submissionData: SubmissionData): URL = {
    val gateway: AS4Gateway = Databeyz.findByPartyId(submissionData.from)
    if (gateway == null) {
      throw new IllegalArgumentException("A party with id [" + submissionData.from + "] was not found")
    }
    val address: String = gateway.getBackendAddress
    new URL(address)
  }

  def process(request: Request[RawBuffer]): Result = {
    if (!Global.as4Adapter.isRunning) {
      Logger.warn("Test Not Started. Bad Request!")
      return Forbidden("<html><head><title>Error</title></head><body><h1>Sorry, Minder Test Not Active</h1></body></html>".getBytes)
    }

    val logItem = new LogItem();
    logItem.valid = false;
    try {
      val message = Util.extractSOAPMessageFromRequest(request)
      //validate the service
      val service = Util.getElementText(SWA12Util.findSingleNode(message.getSOAPHeader, "//:CollaborationInfo/:Service"))
      if ("http://www.esens.eu/as4/conformancetest" != service) {
        Logger.error("Service [" + service + "] not supported")
        return BadRequest("Service [" + service + "] not supported")
      }

      val toPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:To/:PartyId")
      val fromPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:From/:PartyId")
      val conversationId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:eb:CollaborationInfo/:ConversationId")
      logItem.setAS4Message(message);
      logItem.valid = true;
      logItem.toPartyId = toPartyId.getTextContent;
      logItem.fromPartyId = fromPartyId.getTextContent;
      logItem.conversationId = conversationId.getTextContent;
      val action = Util.getElementText(SWA12Util.findSingleNode(message.getSOAPHeader, "//:CollaborationInfo/:Action"))

      action match {
        case "Deliver" => {
          logItem.messageType = "Delivery " + label
          Logger.debug("Deliver message")
          processDelivery(message)
        }
        case "Notify" => {
          logItem.valid = true;
          logItem.messageType = "Notification " + label
          Logger.debug("Process notification")
          processNotification(message)
        }
        case _ => {
          Logger.error("Service [" + service + "] not supported")
          return BadRequest("Action [" + action + "] not supported")
        }
      }

      logItem.success = LogItemSuccess.TRUE;
      Logger.debug("Success Receipt")
      val array: Array[Byte] = Util.createSuccessReceipt(message)
      logItem.reply = array;
      val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(new ByteArrayInputStream(array))
      Ok.chunked(dataContent).as("application/soap+xml;charset=UTF-8")
    } catch {
      case th: Throwable => {
        logItem.success = LogItemSuccess.FALSE;
        logItem.setException(th)
        Logger.error(th.getMessage, th)
        InternalServerError(th.getMessage)
      }
    } finally {
      if (logItem.valid)
        logItem.persist();
    }
  }


  def processDelivery(message: SOAPMessage): Unit = {
    //create a submissiondata object from the SOAP message
    deliver(Util.convert2SubmissionData(message))
  }

  def processNotification(message: SOAPMessage): Unit = {
    val status = new MessageNotification

    val properties: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:MessageProperties")

    var element: Node = SWA12Util.findSingleNode(properties, ".//:Property[@name='RefToMessageId']/text()")
    status.messageId = element.getNodeValue
    element = SWA12Util.findSingleNode(properties, ".//:Property[@name='SignalType']/text()")
    status.status = MessageDeliveryStatus.valueOf(element.getNodeValue)

    Try {
      element = SWA12Util.findSingleNode(properties, ".//:Property[@name='ErrorCode']/text()")
      status.errorCode = element.getNodeValue
      element = SWA12Util.findSingleNode(properties, ".//:Property[@name='ShortDescription']/text()")
      status.shortDescription = element.getNodeValue
      element = SWA12Util.findSingleNode(properties, ".//:Property[@name='Description']/text()")
      status.description = element.getNodeValue
    }

    processNotification(status);
  }
}