package backend

import java.io.FileOutputStream
import java.net.URL
import java.util.UUID
import javax.xml.soap.SOAPMessage

import controllers.KerkoviAS4Controller._
import controllers.{LogItem, Databeyz, Global}
import esens.wp6.esensMshBackend._
import minder.as4Utils.SWA12Util
import model.AS4Gateway
import org.w3c.dom.{Element, Node}
import play.api.Logger
import play.api.mvc.{RawBuffer, Request, Result}
import utils.Util

import scala.util.Try

/**
  * Author: yerlibilgin
  * Date: 04/08/15.
  */
class GenericAS4Corner extends AbstractMSHBackend {
  var label: String = "Corner"

  def getGatewayID: GatewayID = {
    val d: GatewayID = new GatewayID
    d.id = "Domibus3"
    return d;
  }

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
        submissionData.messageId = UUID.randomUUID().toString
      }

      val message: SOAPMessage = Util.convert2Soap(submissionData, Global.myPartyID,
        submissionData.from, "Submit", Global.serviceProperties.getProperty(submissionData.pModeId),
        Global.actionProperties.getProperty(submissionData.pModeId))

      Logger.debug("[" + label + "] Submit AS4 Message to backend")
      Logger.debug(SWA12Util.describe(message))
      Logger.debug("====================")

      val toPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:To/:PartyId")
      val fromPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:From/:PartyId")

      logItem.valid = true;
      logItem.setAS4Message(message)
      logItem.toPartyId = toPartyId.getTextContent;
      logItem.fromPartyId = fromPartyId.getTextContent;
      logItem.messageType = "Submission " + label

      val address: URL = resolveBackendAddress(submissionData)

      Logger.debug("URL of the backend: " + address)
      val reply: SOAPMessage = SWA12Util.sendSOAPMessage(message, address)
      if (reply != null)
        logItem.setReply(reply);

      logItem.success = true;
      try {
        val elm = utils.Util.evaluateXpath("//:SignalMessage/:Error", reply.getSOAPPart)
        if (elm != null)
          logItem.success = false;
      } catch {
        case _ =>
      }
      Logger.debug("[" + label + "] Receipt received from the AS4 backend")
      Logger.debug(SWA12Util.describe(reply))
      Logger.debug("====================")
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
    try {
      val message = Util.extractSOAPMessageFromRequest(request)
      //validate the service
      val service = Util.getElementText(SWA12Util.findSingleNode(message.getSOAPHeader, "//:CollaborationInfo/:Service"))
      if ("http://www.esens.eu/as4/conformancetest" != service) {
        Logger.error("Service [" + service + "] not supported")
        return BadRequest("Service [" + service + "] not supported")
      }

      //TODO : process wrt sanders PMODE
      val action = Util.getElementText(SWA12Util.findSingleNode(message.getSOAPHeader, "//:CollaborationInfo/:Action"))

      action match {
        case "Deliver" => {
          Logger.debug("Deliver message")
          processDelivery(message)
        }
        case "Notify" => {
          Logger.debug("Process notification")
          processNotification(message)
        }
        case "SubmitResponse" => {
          Logger.debug("Process submission response")
          processSubmissionResponse(message)
        }
        case _ => {
          Logger.error("Service [" + service + "] not supported")
          return BadRequest("Action [" + action + "] not supported")
        }
      }

      Logger.debug("Success Receipt")
      return Util.sendSuccessResponse(message);
    } catch {
      case th: Throwable => {
        Logger.error(th.getMessage, th)
        BadRequest(th.getMessage)
      }
    }
  }


  def processDelivery(message: SOAPMessage): Unit = {
    val logItem = new LogItem();
    try {
      val toPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:To/:PartyId")
      val fromPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:From/:PartyId")

      logItem.valid = true;
      logItem.setAS4Message(message);
      logItem.toPartyId = toPartyId.getTextContent;
      logItem.fromPartyId = fromPartyId.getTextContent;
      logItem.messageType = "Delivery " + label

      //create a submissiondata object from the SOAP message
      deliver(Util.convert2SubmissionData(message))
      logItem.success = true
    } catch {
      case th: Throwable => {
        logItem.setException(th);
      }
    } finally {
      if (logItem.valid) logItem.persist()
    }
  }


  /**
  <table border="1" cellspacing="0"><tr><th>Property name</th><th>Required?</th></tr>
    <tr><td>MessageId</td><td>N</td></tr>
    <tr><td>RefToMessageId</td><td>Y</td></tr>
    <tr><td>SignalType</td><td>Y</td></tr>
    <tr><td>ErrorCode</td><td>Y if Error</td></tr>
    <tr><td>ShortDescription </td><td>N</td></tr>
    <tr><td>Description</td><td>N</td></tr>
    </table>
    */
  def processSubmissionResponse(message: SOAPMessage): Unit = {
    val logItem = new LogItem();
    try {
      val toPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:To/:PartyId")
      val fromPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:From/:PartyId")

      logItem.valid = true;
      logItem.setAS4Message(message)
      logItem.toPartyId = toPartyId.getTextContent;
      logItem.fromPartyId = fromPartyId.getTextContent;
      logItem.messageType = "SubmissionResult " + label

      val submissionResult = new SubmissionResult

      val properties: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:MessageProperties")

      try {
        val element: Node = SWA12Util.findSingleNode(properties, ".//:Property[@name='MessageId']/text()")
        submissionResult.ebmsMessageId = element.getNodeValue
      } catch {
        case th: Throwable => {
          val element: Node = SWA12Util.findSingleNode(properties, ".//:Property[@name='Error']/text()")
          submissionResult.error = new SubmissionError;
          submissionResult.error.errorCode = element.getNodeValue
          submissionResult.error.description = ""
        }
      }

      processSubmissionResult(submissionResult)
      logItem.success = true
    } catch {
      case th: Throwable => {
        logItem.setException(th);
      }
    } finally {
      if (logItem.valid) logItem.persist()
    }
  }


  def processNotification(message: SOAPMessage): Unit = {
    val logItem = new LogItem();
    try {
      val toPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:To/:PartyId")
      val fromPartyId: Element = SWA12Util.findSingleNode(message.getSOAPHeader, "//:PartyInfo/:From/:PartyId")

      logItem.valid = true;
      logItem.setAS4Message(message)
      logItem.toPartyId = toPartyId.getTextContent;
      logItem.fromPartyId = fromPartyId.getTextContent;
      logItem.messageType = "Notification " + label

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
      logItem.success = true
    } catch {
      case th: Throwable => {
        logItem.setException(th);
      }
    } finally {
      if (logItem.valid) logItem.persist()
    }
  }
}