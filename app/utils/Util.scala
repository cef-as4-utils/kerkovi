package utils

import java.io.{FileInputStream, ByteArrayInputStream, ByteArrayOutputStream}
import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID
import javax.xml.namespace.{QName, NamespaceContext}
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.soap.{AttachmentPart, MimeHeaders, SOAPMessage}
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.{StreamResult, StreamSource}
import javax.xml.xpath.XPathConstants._
import javax.xml.xpath._

import esens.wp6.esensMshBackend._
import minder.as4Utils.{SWA12Util, SAAJUtil}
import minder.as4Utils.SWA12Util._
import org.w3c.dom.{Document, Element, Node}
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
    val stylesource = new StreamSource(this.getClass.getResourceAsStream("/receipt-generator.xslt"));
    val transformer = TransformerFactory.newInstance().newTransformer(stylesource);
    transformer.setParameter("messageid", genereateEbmsMessageId("mindertestbed.org"));
    transformer.setParameter("timestamp", ZonedDateTime.now(ZoneOffset.UTC).toString);
    val baos = new ByteArrayOutputStream()
    transformer.transform(new DOMSource(soapMessage.getSOAPPart), new StreamResult(baos))
    val array: Array[Byte] = baos.toByteArray
    val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(new ByteArrayInputStream(array))
    Ok.chunked(dataContent).as("application/soap+xml;charset=UTF-8")
  }

  /**
    * See
    * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/os/AS4-profile-v1.0-os.html#__RefHeading__26454_1909778835
    * @param soapMessage
    * @return
    */
  def createSuccessReceipt(soapMessage: SOAPMessage) = {
    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val stylesource = new StreamSource(this.getClass.getResourceAsStream("/receipt-generator.xslt"));
    val transformer = TransformerFactory.newInstance().newTransformer(stylesource);
    transformer.setParameter("messageid", genereateEbmsMessageId("mindertestbed.org"));
    transformer.setParameter("timestamp", ZonedDateTime.now(ZoneOffset.UTC).toString);
    val baos = new ByteArrayOutputStream()
    transformer.transform(new DOMSource(soapMessage.getSOAPPart), new StreamResult(baos))
    baos.toByteArray
  }


  /**
    * Create a fault message based on the infro provided
    * and return it
    * @param soapMessage
    * @param faultMessage
    * @return
    */
  def sendFault(soapMessage: SOAPMessage, faultMessage: String): Result = {
    var xml = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/fault-template.xml")).mkString

    val refToMessageInError = if (soapMessage != null) {
      getElementText(findSingleNode(soapMessage.getSOAPHeader, "//:MessageInfo/:MessageId"))
    } else {
      ""
    }

    val fm = if (faultMessage == null) "UNKNOWN ERROR" else faultMessage

    val ebmsMessageId = genereateEbmsMessageId("mindertestbed.org")
    val category = "CONTENT"
    val errorCode = "EBMS:0004"
    val origin = "ebms"
    val severity = "failure"
    val shortDescription = "Error"
    val description = fm
    val errorDetail = fm
    val reason = fm
    val faultCode = "env:Receiver"

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

      // Logger.debug("SOAP Message")
      Logger.debug(prettyPrint(soapMessage.getSOAPHeader()));
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
  def convert2Soap(submissionData: SubmissionData, from: String, to: String, action: String,
                   targetService: String, targetAction: String): SOAPMessage = {
    Logger.debug("Convert submission data to SOAP Message")
    var xml = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/as4template.xml")).mkString

    val keyTimeStamp = "${timeStamp}"
    val keyMessageId = "${ebmsMessageID}"
    val keyFrom = "${from}"
    val keyTo = "${to}"
    val keyAction = "${action}"
    val keyMessageProps = "${messageProperties}"
    val keyPartInfo = "${partInfo}"
    val keyConversationId = "${conversationId}"

    val ebmsMessageId = genereateEbmsMessageId("mindertestbed.org")

    xml = xml
      .replace(keyTimeStamp, ZonedDateTime.now(ZoneOffset.UTC).toString)
      .replace(keyMessageId, ebmsMessageId)
      .replace(keyFrom, from)
      //why from instead of to?
      // because, we are submitting the message to the guy who has the equal to FROM.
      // He then, will send it to the TO
      .replace(keyTo, to)
      .replace(keyConversationId, if(submissionData.conversationId!=null){submissionData.conversationId}else{"1"})
      .replace(keyAction, action)
      .replace(keyMessageProps, generateMessageProperties(submissionData, targetService, targetAction))
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
      att.setContentId('<' + p.payloadId + '>')
      att.setRawContentBytes(p.data, 0, p.data.length, p.mimeType)
      message.addAttachmentPart(att)
    })

    if (message.saveRequired())
      message.saveChanges()

    Logger.debug(describe(message))
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
  def generateMessageProperties(submissionData: SubmissionData, targetService: String, targetAction: String): String = {
    val propertiesBuilder = new StringBuilder
    if (submissionData.messageId != null) {
      propertiesBuilder.append("<ns2:Property name=\"MessageId\">" + submissionData.messageId + "</ns2:Property>\n")
    }

    val convId = if (submissionData.conversationId != null) {
      submissionData.conversationId
    } else {
      "1"
    }
    propertiesBuilder.append("<ns2:Property name=\"ConversationId\">" + convId + "</ns2:Property>\n")
    if (submissionData.refToMessageId != null) {
      propertiesBuilder.append("<ns2:Property name=\"RefToMessageId\">" + submissionData.refToMessageId + "</ns2:Property>\n")
    }

    propertiesBuilder.append("<ns2:Property name=\"ToPartyId\">" + submissionData.to + "</ns2:Property>\n")
    propertiesBuilder.append("<ns2:Property name=\"ToPartyRole\">" + submissionData.toPartyRole + "</ns2:Property>\n")
    propertiesBuilder.append("<ns2:Property name=\"FromPartyId\">" + submissionData.from + "</ns2:Property>\n")
    propertiesBuilder.append("<ns2:Property name=\"FromPartyRole\">" + submissionData.fromPartyRole + "</ns2:Property>\n")
    propertiesBuilder.append("<ns2:Property name=\"Service\">" + targetService + "</ns2:Property>\n")
    propertiesBuilder.append("<ns2:Property name=\"Action\">" + targetAction + "</ns2:Property>\n")
    propertiesBuilder.append("<ns2:Property name=\"originalSender\">" + submissionData.originalSender + "</ns2:Property>\n")
    propertiesBuilder.append("<ns2:Property name=\"finalRecipient\">" + submissionData.finalRecipient + "</ns2:Property>")


    propertiesBuilder.toString
  }

  def generatePartInfo(submissionData: SubmissionData): String = {
    val partInfoBuilder = new StringBuilder

    submissionData.getPayloads.foreach(payload => {
      partInfoBuilder
        .append("<ns2:PartInfo href=\"cid:")
        .append(payload.payloadId)
        .append("\">\n<ns2:PartProperties><ns2:Property name=\"MimeType\">")
        .append(payload.mimeType)
        .append("</ns2:Property>\n");

      if (payload.characterSet != null) {
        partInfoBuilder.append("<ns2:Property name=\"CharacterSet\">")
          .append(payload.characterSet)
          .append("</ns2:Property>\n");
      }
      partInfoBuilder.append("</ns2:PartProperties>\n</ns2:PartInfo>\n")
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
    Logger.debug(prettyPrint(message.getSOAPPart))
    val data = new SubmissionData
    val properties: Element = findSingleNode(message.getSOAPHeader, "//:MessageProperties")

    var node: Node = findSingleNode(properties, ".//:Property[@name='ToPartyId']/text()")
    data.to = node.getNodeValue
    Logger.debug("Data.to " + data.to)
    Try {
      node = findSingleNode(properties, ".//:Property[@name='MessageId']/text()")
      data.messageId = node.getNodeValue
      Logger.debug("Data.messageId " + data.messageId)
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='ConversationId']/text()")
      data.conversationId = node.getNodeValue
      Logger.debug("data.conversationId: " + data.conversationId)
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='RefToMessageId']/text()")
      data.refToMessageId = node.getNodeValue
      Logger.debug("data.conversationId: " + data.conversationId)
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='ToPartyRole']/text()")
      data.toPartyRole = node.getNodeValue
      Logger.debug("data.partyRole: " + data.toPartyRole)
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='FromPartyRole']/text()")
      data.fromPartyRole = node.getNodeValue
      Logger.debug("data.partyRole: " + data.fromPartyRole)
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='originalSender']/text()")
      data.originalSender = node.getNodeValue
      Logger.debug("data.originalSender: " + data.originalSender)
    }
    Try {
      node = findSingleNode(properties, ".//:Property[@name='finalRecipient']/text()")
      data.finalRecipient = node.getNodeValue
      Logger.debug("data.finalRecipient: " + data.finalRecipient)
    }

    Try {
      node = findSingleNode(properties, ".//:Property[@name='Service']/text()")
      data.service = node.getNodeValue
      Logger.debug("data.service: " + data.service)
    }

    Try {
      node = findSingleNode(properties, ".//:Property[@name='Action']/text()")
      data.action = node.getNodeValue
      Logger.debug("data.action: " + data.action)
    }
    //parse part properties and validate consistency with Attachments

    message.getAttachments.toSeq.foreach(attObj => {
      val payload = new Payload
      val att: AttachmentPart = attObj.asInstanceOf[AttachmentPart];

      val href = att.getContentId.replaceAll("<|>", "");

      //throws exception if part info does not exist
      val partInfo: Node = {
        Try {
          findSingleNode[Node](message.getSOAPHeader, "//:PayloadInfo/:PartInfo[@href='" + href + "']")
        } orElse Try {
          findSingleNode[Node](message.getSOAPHeader, "//:PayloadInfo/:PartInfo[@href='cid:" + href + "']")
        }
      }.get

      if (partInfo == null) throw new RuntimeException("ContentId: " + href + " was not found in PartInfo")

      var mimeType = findSingleNode[Node](partInfo, ".//:PartProperties/:Property[@name='MimeType']/text()").getNodeValue
      if (mimeType.startsWith("cid"))
        mimeType = mimeType.substring(4);

      val contentType = att.getContentType

      //if content type is application/gzip, we must decompress it
      if (contentType == "application/gzip") {
        payload.data = SWA12Util.gunzip(att.getRawContentBytes)
      } else {
        payload.data = att.getRawContentBytes
      }

      payload.mimeType = mimeType
      payload.payloadId = href;
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


  def generateMessageProperties(messageNotification: MessageNotification) = {
    /**
      * Property name	Required?
       RefToMessageId	  Y
       SignalType	      Y
       ErrorCode	      N
       ShortDescription	N
       Description	    N
      */
    val propertiesBuilder = new StringBuilder
    propertiesBuilder.append("<ns2:Property name=\"RefToMessageId\">" + messageNotification.messageId + "</ns2:Property>")
    propertiesBuilder.append("<ns2:Property name=\"SignalType\">" + messageNotification.status.name() + "</ns2:Property>")

    if (messageNotification.errorCode != null) {
      propertiesBuilder.append("<ns2:Property name=\"ErrorCode\">" + messageNotification.errorCode + "</ns2:Property>")
    }
    if (messageNotification.shortDescription != null) {
      propertiesBuilder.append("<ns2:Property name=\"ShortDescription\">" + messageNotification.shortDescription + "</ns2:Property>")
    }

    if (messageNotification.description != null) {
      propertiesBuilder.append("<ns2:Property name=\"Description\">" + messageNotification.description + "</ns2:Property>")
    }

    propertiesBuilder.toString
  }

  def convert2Soap(messageNotification: MessageNotification, from: String, to: String, action: String): SOAPMessage = {
    /**
      * Property name	Required?
       RefToMessageId	  Y
       SignalType	      Y
       Error Code	      N
       ShortDescription	N
       Description	    N
      */
    var xml = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/as4template.xml")).mkString

    val keyTimeStamp = "${timeStamp}"
    val keyMessageId = "${ebmsMessageID}"
    val keyFrom = "${from}"
    val keyTo = "${to}"
    val keyAction = "${action}"
    val keyMessageProps = "${messageProperties}"
    val keyPartInfo = "${partInfo}"

    val ebmsMessageId = genereateEbmsMessageId("mindertestbed.org")

    xml = xml
      .replace(keyTimeStamp, ZonedDateTime.now(ZoneOffset.UTC).toString)
      .replace(keyMessageId, ebmsMessageId)
      .replace(keyFrom, from)
      .replace(keyAction, action)
      .replace(keyTo, to)
      .replace(keyMessageProps, generateMessageProperties(messageNotification))
      .replace(keyPartInfo, "")

    val instance: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    instance.setNamespaceAware(true);
    val document = instance.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("utf-8")))

    //create a soap message based on this XML
    val message = createMessage();
    val element: Element = document.getDocumentElement
    val importNode = message.getSOAPHeader.getOwnerDocument.importNode(element, true)
    message.getSOAPHeader.appendChild(importNode)

    if (message.saveRequired())
      message.saveChanges()

    return message
  }

  def readFile(fileName: String): Array[Byte] = {
    val fis = new FileInputStream(fileName)
    val all = new Array[Byte](fis.available())
    fis.read(all, 0, all.length)
    all
  }

  val xpath: XPath = {
    val xPath: XPath = XPathFactory.newInstance.newXPath
    xPath.setNamespaceContext(new AnyNamespaceContext)
    xPath
  }

  def evaluateXpath(expression: String, document: Node): Node = {
    return evaluateXpath(expression, document, XPathConstants.NODE).asInstanceOf[Node]
  }

  def evaluateXpath(expression: String, document: Node, qname: QName): AnyRef = {
    xpath.evaluate(expression, document, qname)
  }
}

class AnyNamespaceContext extends NamespaceContext {
  override def getNamespaceURI(prefix: String) = "*"

  override def getPrefix(namespace: String) = null

  override def getPrefixes(namespace: String) = null
}