package backend

import java.net.{MalformedURLException, URL}
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
    d.id="Domibus3"
    return d;
  }

  /**
   * Convert the submissiondata object into an AS4 object and send it to the MSH
   *
   * @param submissionData
   * @return
   */
  def submitMessage(submissionData: SubmissionData): SubmissionResult = {
    Logger.debug(label + " wants to submit a message to [" + submissionData.from + "]")

    val message: SOAPMessage = Util.convert2Soap(submissionData)
    val reply: SOAPMessage = AS4Utils.sendSOAPMessage(message, resolveAddressFromToPartyId(submissionData))

    Logger.debug("Submit this to backend")
    Logger.debug(AS4Utils.describe(message))
    Logger.debug("====================")
    //TODO: resolve the actual ebms message id
    val id = Util.getElementText(AS4Utils.findSingleNode(reply.getSOAPHeader, "//:MessageInfo/:MessageId"))
    val result: SubmissionResult = new SubmissionResult
    //TODO: this is wrong, need to correct it
    result.ebmsMessageId = id;
    Logger.debug("EBMS MESSAGE ID: " + result.ebmsMessageId)
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
          processDelivery(message)
        }
        case "Notify" => {
          processNotification(message)
        }
        case "Submit" => {
          processDelivery(message)
        } case _ => {
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
    Logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>> MESSAGE ATTACHMENT COUNT " + message.countAttachments())

    Logger.debug("PRETTY PRINTED " + AS4Utils.describe(message))
    Logger.debug("DONE")

    deliver(Util.convert2SubmissionData(message))
  }

  def processNotification(message: SOAPMessage): Unit = {
    /*
    Property name	Required?
    MessageId	         N
    RefToMessageId	   Y
    SignalType	       Y
    ErrorCode          Y if Error
    ShortDescription   N
    Description        N
    */
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