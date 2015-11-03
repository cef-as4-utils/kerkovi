package utils

import java.io.{FileInputStream, ByteArrayInputStream, ByteArrayOutputStream}
import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.soap.{AttachmentPart, MimeHeaders, SOAPMessage}
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.{StreamResult, StreamSource}

import controllers.Global
import esens.wp6.esensMshBackend.{Payload, SubmissionData}
import minder.as4Utils.AS4Utils
import minder.as4Utils.AS4Utils._
import org.w3c.dom.{Node, Element}
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Controller, RawBuffer, Result}
import play.api.{Logger, mvc}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * Author: yerlibilgin
 * Date:   28/10/15.
 */
/**
 * Utility class
 */
object Util extends Controller {

  /**
   * See
   * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/os/AS4-profile-v1.0-os.html#__RefHeading__26454_1909778835
   * @param soapMessage
   * @return
   */
  def sendSuccessResponse(soapMessage: SOAPMessage) = {
    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val stylesource = new StreamSource(new FileInputStream("receipt-generator.xslt"));
    val transformer = TransformerFactory.newInstance().newTransformer(stylesource);
    transformer.setParameter("messageid", genereateEbmsMessageId("minder"));
    transformer.setParameter("timestamp", ZonedDateTime.now(ZoneOffset.UTC).toString);
    val baos = new ByteArrayOutputStream()
    transformer.transform(new DOMSource(soapMessage.getSOAPPart), new StreamResult(baos))
    val array: Array[Byte] = baos.toByteArray
    val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(new ByteArrayInputStream(array))
    Ok.chunked(dataContent).as("application/soap+xml;charset=UTF-8")
  }


  /**
   * Create a fault message based on the infro provided
   * and return it
   * @param soapMessage
   * @param faultMessage
   * @return
   */
  def sendFault(soapMessage: SOAPMessage, faultMessage: String): Result = {
    //faultCode : env:Receiver
    var xml = scala.io.Source.fromFile("fault-template.xml").mkString

    val refToMessageInError = if (soapMessage != null) {
      getElementText(findSingleNode(soapMessage.getSOAPHeader, "//:MessageInfo/:MessageId"))
    } else {
      ""
    }

    val fm = if (faultMessage == null) "UNKNOWN ERROR" else faultMessage

    val ebmsMessageId = genereateEbmsMessageId("minder")
    val category = "CONTENT"
    val errorCode = "EBMS:0004"
    val origin = "ebMS"
    val severity = "failure"
    val shortDescription = "Error"
    val description = fm
    val errorDetail = fm
    val reason = fm
    val faultCode = "envReceiver"

    val keyCategory = "${category}"
    val keyErrorCode = "${errorCode}"
    val keyOrigin = "${origin}"
    val keySeverity = "${severity}"
    val keyShortDescription = "${shortDescription}"
    val keyDescription = "${description}"
    val keyErrorDetail = "${errorDetail}"
    val keyFaultCode = "${faultCode}"
    val keyReason = "${reason}"
    xml = xml
      .replace("${timeStamp}", ZonedDateTime.now(ZoneOffset.UTC).toString)
      .replace("${refToMessageInError}", refToMessageInError)
      .replace("${messageId}", ebmsMessageId)
      .replace(keyCategory, category)
      .replace(keyErrorCode, errorCode)
      .replace(keyOrigin, origin)
      .replace(keySeverity, severity)
      .replace(keyShortDescription, shortDescription)
      .replace(keyDescription, description)
      .replace(keyErrorDetail, errorDetail)
      .replace(keyFaultCode, faultCode)
      .replace(keyReason, reason)

    val instance: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    instance.setNamespaceAware(true);
    val document = instance.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("utf-8")))

    //create a soap message based on this XML
    val receipt = createMessage();

    val element: Element = document.getDocumentElement

    val importNode = receipt.getSOAPHeader.getOwnerDocument.importNode(element, true)
    receipt.getSOAPHeader.appendChild(importNode)

    Logger.info(prettyPrint(receipt.getSOAPPart))

    val baos = new ByteArrayOutputStream()
    receipt.writeTo(baos)
    val array: Array[Byte] = baos.toByteArray
    val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(new ByteArrayInputStream(array))
    BadRequest.chunked(dataContent).as("application/soap+xml;charset=UTF-8")
  }

  def getHeaders(request: mvc.Request[RawBuffer]): MimeHeaders = {
    val headers: MimeHeaders = new MimeHeaders
    request.headers.headers.foreach(k => headers.addHeader(k._1, k._2))
    return headers
  }


  def genereateEbmsMessageId(s: String) = {
    UUID.randomUUID() + "@" + s
  }


  /**
   * Return the text content of an org.w3c.dom.Element obj.
   * saves us from creating a temporary variable
   */
  def getElementText(element: Element) = element.getTextContent

  /**
   * @param request
   * @return
   */
  def extractSOAPMessageFromRequest(request: mvc.Request[RawBuffer]): SOAPMessage = {
    try {
      val stream = request.body
      val bytes = stream.asBytes().get
      val headers: MimeHeaders = new MimeHeaders
      headers.addHeader("content-type", request.headers.get("content-type").get)

      val soapMessage = createMessage(headers, new ByteArrayInputStream(bytes))
      soapMessage;
    } catch {
      case th: RuntimeException => {
        throw th
      }
      case th: Throwable => {
        throw new RuntimeException(th.getMessage, th)
      }
    }
  }


  /**
   * The conversion procedure goes here
   *
   * @param submissionData
   * @return
   */
  def convert2Soap(submissionData: SubmissionData): SOAPMessage = {
    Logger.debug("Convert submission data to SOAP Message")
    var xml = scala.io.Source.fromFile("as4template.xml").mkString

    val keyTimeStamp = "${timeStamp}"
    val keyMessageId = "${ebmsMessageID}"
    val keyFrom = "${fromPartyID}"
    val keyTo = "${toPartyID}"
    val keyMessageProps = "${messageProperties}"
    val keyPartInfo = "${partInfo}"

    val ebmsMessageId = genereateEbmsMessageId("minder")

    xml = xml
      .replace(keyTimeStamp, ZonedDateTime.now(ZoneOffset.UTC).toString)
      .replace(keyMessageId, ebmsMessageId)
      //why from instead of to?
      // because, we are submitting the message to the guy who has the equal to FROM.
      // He then, will send it to the TO
      .replace(keyTo, submissionData.from)
      .replace(keyMessageProps, generateMessageProperties(submissionData))
      .replace(keyPartInfo, generatePartInfo(submissionData))

    val instance: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    instance.setNamespaceAware(true);
    val document = instance.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("utf-8")))

    //create a soap message based on this XML
    val message = createMessage();
    val element: Element = document.getDocumentElement
    val importNode = message.getSOAPHeader.getOwnerDocument.importNode(element, true)
    message.getSOAPHeader.appendChild(importNode)

    submissionData.getPayloads.foreach(p => {
      val att = message.createAttachmentPart()
      att.setContentId(p.payloadId)
      att.setRawContentBytes(p.data, 0, p.data.length, p.mimeType)
      message.addAttachmentPart(att)
    })

    if (message.saveRequired())
      message.saveChanges()

    Logger.debug(AS4Utils.describe(message))
    return message
  }

  /**
   * <table><tr><th>Property name	</th><th>     Required?</th></tr>
   * <tr><td>MessageId	    </td><td>     M (should be Y)</td></tr>
   * <tr><td>ConversationId	</td><td>   Y</td></tr>
   * <tr><td>RefToMessageId	</td><td>   N</td></tr>
   * <tr><td>ToPartyId	    </td><td>     Y</td></tr>
   * <tr><td>ToPartyRole	  </td><td>     Y</td></tr>
   * <tr><td>Service	      </td><td>     Y</td></tr>
   * <tr><td>ServiceType	  </td><td>     N // not used</td></tr>
   * <tr><td>Action	        </td><td>   Y</td></tr>
   * <tr><td>originalSender	</td><td>   Y</td></tr>
   * <tr><td>finalRecipient	</td><td>   Y</td></tr></table>
   */
  def generateMessageProperties(submissionData: SubmissionData): String = {
    val propertiesBuilder = new StringBuilder
    if (submissionData.messageId != null) {
      propertiesBuilder.append("<ns2:Property name=\"MessageId\">" + submissionData.messageId + "</ns2:Property>")
    }

    val convId = if (submissionData.conversationId != null) {
      submissionData.conversationId
    } else {
      "1"
    }
    propertiesBuilder.append("<ns2:Property name=\"ConversationId\">" + convId + "</ns2:Property>")
    if (submissionData.refToMessageId != null) {
      propertiesBuilder.append("<ns2:Property name=\"RefToMessageId\">" + submissionData.refToMessageId + "</ns2:Property>")
    }

    propertiesBuilder.append("<ns2:Property name=\"ToPartyId\">" + submissionData.to + "</ns2:Property>")
    propertiesBuilder.append("<ns2:Property name=\"ToPartyRole\">" + submissionData.partyRole + "</ns2:Property>")
    propertiesBuilder.append("<ns2:Property name=\"Service\">" + Global.serviceProperties.getProperty(submissionData.pModeId) + "</ns2:Property>")
    propertiesBuilder.append("<ns2:Property name=\"Action\">" + Global.actionProperties.getProperty(submissionData.pModeId) + "</ns2:Property>")
    propertiesBuilder.append("<ns2:Property name=\"originalSender\">" + submissionData.originalSender + "</ns2:Property>")
    propertiesBuilder.append("<ns2:Property name=\"finalRecipient\">" + submissionData.finalRecipient + "</ns2:Property>")


    propertiesBuilder.toString
  }

  def generatePartInfo(submissionData: SubmissionData): String = {
    val partInfoBuilder = new StringBuilder

    submissionData.getPayloads.foreach(payload => {
      partInfoBuilder
        .append("<ns2:PartInfo href=\"")
        .append(payload.payloadId)
        .append("\"><ns2:PartProperties>\n<ns2:Property name=\"MimeType\">")
        .append(payload.mimeType)
        .append("</ns2:Property>\n");

      if (payload.characterSet != null) {
        partInfoBuilder.append("<ns2:Property name=\"CharacterSet\">")
          .append(payload.characterSet)
          .append("</ns2:Property>\n");
      }
      partInfoBuilder.append("</ns2:PartProperties></ns2:PartInfo>\n")
    })

    partInfoBuilder.toString()
  }

  /**
   * <table><tr><th>Property name	     </th><th>Required?</th></tr>
   * <tr><td>MessageId	         </td><td>N</td></tr>
   * <tr><td>ConversationId	   </td><td>Y</td></tr>
   * <tr><td>RefToMessageId	   </td><td>N</td></tr>
   * <tr><td>ToPartyId	         </td><td>Y</td></tr>
   * <tr><td>ToPartyRole	       </td><td>Y</td></tr>
   * <tr><td>Service	           </td><td>Y</td></tr>
   * <tr><td>ServiceType	       </td><td>N // not used</td></tr>
   * <tr><td>Action	           </td><td>Y</td></tr>
   * <tr><td>originalSender	   </td><td>Y</td></tr>
   * <tr><td>finalRecipient	   </td><td>Y</td></tr></table>
   */
  def convert2SubmissionData(message: SOAPMessage): SubmissionData = {
    Logger.debug("Convert message to submission data")
    Logger.debug(AS4Utils.prettyPrint(message.getSOAPPart))
    val data = new SubmissionData
    val properties: Element = findSingleNode(message.getSOAPHeader, "//:MessageProperties")

    var node: Node = findSingleNode(properties, ".//:Property[@name='ToPartyId']/text()")
    data.to = node.getNodeValue
    Try {
      node = findSingleNode(properties, ".//:Property[@name='MessageId']/text()")
      data.messageId = node.getNodeValue
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='ConversationId']/text()")
      data.conversationId = node.getNodeValue
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='RefToMessageId']/text()")
      data.refToMessageId = node.getNodeValue
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='ToPartyRole']/text()")
      data.partyRole = node.getNodeValue
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='originalSender']/text()")
      data.originalSender = node.getNodeValue
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='finalRecipient']/text()")
      data.finalRecipient = node.getNodeValue
    }

    //parse part properties and validate consistency with Attachments

    message.getAttachments.toSeq.foreach(attObj => {
      val payload = new Payload
      val att: AttachmentPart = attObj.asInstanceOf[AttachmentPart];

      val href = att.getContentId;

      //throws exception if part info does not exist
      val partInfo: Node = findSingleNode(message.getSOAPHeader, "//:PayloadInfo/:PartInfo[@href='" + href + "']")
      val mimeType: Node = findSingleNode(partInfo, ".//:PartProperties/:Property[@name='MimeType']/text()")

      if (mimeType.getNodeValue != att.getContentType)
        throw new RuntimeException("Content type missmatch [" + mimeType.getNodeValue + " != " +
          att.getContentType + "]")


      payload.mimeType = mimeType.getNodeValue
      payload.payloadId = href;
      payload.data = att.getRawContentBytes
      Logger.debug("\tpayload.payloadId: " + payload.payloadId)
      Logger.debug("\tpayload.mimeType: " + payload.mimeType)
      Logger.debug("\tpayload.data: " + payload.data.length)

      Try {
        val charSet: Node = findSingleNode(partInfo, ".//:PartProperties/:Property[@name='CharacterSet']/text()")
        payload.characterSet = charSet.getNodeValue
        Logger.debug("\tpayload.characterSet: " + payload.characterSet)
      }
      data.add(payload)
    })
    data
  }
}
