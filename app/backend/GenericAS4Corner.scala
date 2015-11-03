package backend

import java.net.{MalformedURLException, URL}
import java.util.UUID
import javax.xml.soap.SOAPMessage

import controllers.KerkoviAS4Controller._
import controllers.Databeyz
import esens.wp6.esensMshBackend._
import minder.as4Utils.AS4Utils
import org.w3c.dom.{Node, Element}
import play.api.Logger
import play.api.mvc.{RawBuffer, Request, Result}
import utils.Util
import scala.collection.JavaConversions._

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
  def submitMessage(submissionData: SubmissionData): SubmissionResult = {
    Logger.debug("[" + label + "] wants to submit a message to [" + submissionData.from + "]")

    //update the message id (if its null, generate one)
    if (submissionData.messageId == null){
      submissionData.messageId = UUID.randomUUID().toString
    }

    val message: SOAPMessage = Util.convert2Soap(submissionData)

    Logger.debug("[" + label + "] Submit AS4 Message to backend")
    Logger.debug(AS4Utils.describe(message))
    Logger.debug("====================")


    val reply: SOAPMessage = AS4Utils.sendSOAPMessage(message, resolveAddressFromToPartyId(submissionData))
    Logger.debug("[" + label + "] Receipt received from the AS4 backend")
    Logger.debug(AS4Utils.describe(reply))
    Logger.debug("====================")

    val result: SubmissionResult = new SubmissionResult
    result.ebmsMessageId = submissionData.messageId;
    Logger.debug("MSH ebms message id: " + result.ebmsMessageId)
    return result
  }

  def resolveAddressFromToPartyId(submissionData: SubmissionData): URL = {
    new URL(Databeyz.findByPartyId(submissionData.from).address)
  }

  def process(request: Request[RawBuffer]): Result = {
    try {
      val message = Util.extractSOAPMessageFromRequest(request)
      //validate the service
      val service = Util.getElementText(AS4Utils.findSingleNode(message.getSOAPHeader, "//:CollaborationInfo/:Service"))
      if ("http://www.esens.eu/as4/conformancetest" != service) {
        Logger.error("Service [" + service + "] not supported")
        return BadRequest("Service [" + service + "] not supported")
      }

      //TODO : process wrt sanders PMODE
      val action = Util.getElementText(AS4Utils.findSingleNode(message.getSOAPHeader, "//:CollaborationInfo/:Action"))

      action match {
        case "Deliver" => {
          Logger.debug("Deliver message")
          processDelivery(message)
        }
        case "Notify" => {
          Logger.debug("Process notification")
          processNotification(message)
        }
        case "Submit" => {
          Logger.debug("Process submission")
          processDelivery(message)
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
    //create a submissiondata object from the SOAP message
    deliver(Util.convert2SubmissionData(message))
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
  def processNotification(message: SOAPMessage): Unit = {
    val status = new MessageNotification

    val properties = AS4Utils.findSingleNode(message.getSOAPHeader, "//:MessageProperties")

    var element: Node = AS4Utils.findSingleNode(properties, ".//:MessageProperty[@name='RefToMessageId']/@value")
    status.messageId = element.getNodeValue

    element = AS4Utils.findSingleNode(properties, ".//:MessageProperty[@name='SignalType']/@value")

    status.status = element.getNodeValue match {
      case "Receipt" => MessageNotification.MessageDeliveryStatus.Receipt
      case "Error" => {
        //in case of error, we also expect
        element = AS4Utils.findSingleNode(properties, ".//:MessageProperty[@name='ErrorCode']/@value")
        status.errorCode = element.getNodeValue

        //take description if any
        Try {
          element = AS4Utils.findSingleNode(properties, ".//:MessageProperty[@name='Description']/@value")
          status.description = element.getNodeValue
        }
        Try {
          element = AS4Utils.findSingleNode(properties, ".//:MessageProperty[@name='ShortDescription']/@value")
          status.shortDescription = element.getNodeValue
        }

        MessageNotification.MessageDeliveryStatus.Error
      }
    }

    processNotification(status)

  }
}