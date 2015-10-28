package controllers;

import esens.wp6.esensMshBackend.AbstractMSHBackend;
import esens.wp6.esensMshBackend.SubmissionData;
import minder.as4Utils.AS4Utils;

import javax.xml.soap.SOAPMessage;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: yerlibilgin
 * @date: 04/08/15.
 */
public class GenericAS4Backend extends AbstractMSHBackend {

  private URL endPoint;
  public GenericAS4Backend(){
    //read the endpoint from system properties
    String host = System.getProperty("AS4backendUrl");
    try {
      endPoint = new URL(host);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Invalid URL [" + host +  "]");
    }
  }

  @Override
  public String getGatewayID() {
    //TODO: get gateway ID
    return "";
  }

  /**
   * Convert the submissiondata object into an AS4 object and send it to the MSH
   * @param submissionData
   * @return
   */
  @Override
  public String submitMessage(SubmissionData submissionData) {
    SOAPMessage message = convert2Soap(submissionData);
    AS4Utils.sendSOAPMessage(message, endPoint);
    return null;
  }

  /**
   * The conversion procedure goes here
   * @param submissionData
   * @return
   */
  private SOAPMessage convert2Soap(SubmissionData submissionData) {
    return null;
  }
}
