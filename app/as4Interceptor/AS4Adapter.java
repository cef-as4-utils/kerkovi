package as4Interceptor;

import controllers.Databeyz;
import gov.tubitak.minder.client.MinderClient;
import minder.as4Utils.SWA12Util;
import minderengine.Signal;
import minderengine.SignalFailedException;
import minderengine.Slot;
import minderengine.Wrapper;
import org.w3c.dom.Element;
import play.Logger;
import utils.Tic;

import javax.xml.soap.SOAPMessage;
import java.net.URL;

/**
 * This class is the bridge between "Minder test engine" and the system under
 * test. It performs a remote communication with the test server and joins the
 * game as a wrapper.
 * <p>
 * This class is abstract (because of abstarct signals), we instantiate it in
 * the lower level using CGI.
 * <p>
 * <p>
 * A sample ONE WAY PUSH flow is as follows (the steps might or might not take place WRT the
 * scenario extracted from the test assertion):
 * <p>
 * <ol>
 * <li>The C1 backend submits a message to C2.
 * <li>C2 gateway produces an AS4 user message and sends it to C2Endpoint</li>
 * <li>C23Wrapper forwards AS4 user message to Minder using <code>c2SendAS4Message</code><br>
 * <li>Minder receives the AS4 user message, does the necessary checks and
 * forwards it to C23Wrapper back using <code>c3ReceiveAS4Message</code></li>
 * <li>C23Wrapper takes the AS4 user message and sends it to C3Endpoint via an HTTP client connection.
 * <li>C3 sends AS4 Signal message via <code>sendAS4Message</code></li>
 * <li>Minder takes the AS4 signal message,
 * perform necessary checks, and sends it to C2 wrapper via <code>receiveAS4Message</code> slot</li>
 * <li>C2 Wrapper sends the AS4 signal message to C2 via HTTP connection</li>
 * <li>C3 sends the user message to C4 (backend)</li>
 * <p>
 * </ol>
 *
 * @author yerlibilgin
 */
public abstract class AS4Adapter extends Wrapper {
  private MinderClient minderClient;
  private Object replyLock = new Object();
  private SOAPMessage reply = null;

  public AS4Adapter() {
    Logger.info("Initiate AS4 Adapter");
  }

  /**
   */
  public boolean isRunning = false;

  /**
   * Called by the server when a test case that contains this wrapper is about
   * to be run. Perform any initialization here
   */
  @Override
  public void startTest() {
    finishTest();
    Logger.info("AS4Adapter Start Test");
    isRunning = true;
  }

  /**
   * Called by the server when a test case that conatins this wrapper is
   * finished. Perform your own resource deallocation here.
   */
  @Override
  public void finishTest() {
    Logger.info("AS4Adapter Finish Test");
    isRunning = false;
  }

  /**
   * Added in the third version.
   *
   * @param soapMessage
   */
  @Signal
  public abstract void messageReceived(byte[] soapMessage);

  @Slot
  public void sendResponse(byte[] receipt) {
    Logger.info("Send Response Slot Called");

    //put the message into the queue.
    if (receipt == null) {
      reply = null;
      Logger.debug("Receipt is null");
    } else {
      try {
        reply = SWA12Util.deserializeSOAPMessage(receipt);
      } catch (Exception ex) {
        Logger.error(ex.getMessage(), ex);
      }
    }

    synchronized (replyLock) {
      replyLock.notifyAll();
    }
  }

  @Slot
  public void sendMessage(byte[] message) {
    Logger.info("Send message slot called");
    //send message to c2 and signal the receipt.
    SOAPMessage soapMessage = SWA12Util.deserializeSOAPMessage(message);
    SOAPMessage receipt = null;

    //get the backendAddress from party id.
    try {
      Element tmp = SWA12Util.findSingleNode(soapMessage.getSOAPHeader(), "//:PartyInfo/:To/:PartyId");
      final String toPartyId = tmp.getTextContent();
      String targetUrlString = Databeyz.findByPartyId(toPartyId).mshAddress;
      Logger.debug("Send message to [" + toPartyId + "] with address " + targetUrlString);
      Tic.tic();
      receipt = sendMessage(soapMessage, targetUrlString);
      Logger.debug(">>>>>> Time between send-receive [" + toPartyId + "-targetUrlString] " + Tic.toc());
      if (receipt == null) {
        Logger.warn(toPartyId + " sent back NULL receipt, report error");
        beginReportSignalError(new SignalFailedException(toPartyId + " sent back NULL response"));
        replyReceived(null);
      } else {
        Logger.info(toPartyId + " sent back Receipt, Call Signal");
        Logger.debug(SWA12Util.describe(receipt));
        //final String[] contentTypeHeader = receipt.getMimeHeaders().getHeader("content-type");
        //receipt.getMimeHeaders().removeAllHeaders();
        //if (co ntentTypeHeader != null) {
          //receipt.getMimeHeaders().addHeader("content-type", contentTypeHeader[0]);
        //}
        replyReceived(SWA12Util.serializeSOAPMessage(receipt.getMimeHeaders(), receipt));
      }
    } catch (Exception e) {
      Logger.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @Signal
  public abstract void replyReceived(byte[] reply);

  /**
   * Blocks the caller until a receipt is available from minder
   * and returns that receipt.
   *
   * @return
   */
  public SOAPMessage consumeReceipt() {
    //blocks on the queue until receiving a receipt.
    try {
      Logger.debug("Wait for reply");
      synchronized (replyLock) {
        replyLock.wait();
      }
      return reply;
    } catch (InterruptedException e) {
      Logger.warn("Wait on the queue interrupted");
      return null;
    } catch (Exception ex) {
      Logger.error(ex.getMessage(), ex);
    } finally {
      reply = null;
      Logger.debug("Wait on the queue finished");
    }

    return null;
  }

  @Override
  public String getSUTName() {
    return "";
  }

  private SOAPMessage sendMessage(SOAPMessage soapMessage, String targetUrlString) {
    SOAPMessage receipt;
    try {
      receipt = SWA12Util.sendSOAPMessage(soapMessage, new URL(targetUrlString));
    } catch (Exception e) {
      Logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    return receipt;
  }

  public MinderClient getMinderClient() {
    return minderClient;
  }

  public void setMinderClient(MinderClient minderClient) {
    this.minderClient = minderClient;
  }
}
