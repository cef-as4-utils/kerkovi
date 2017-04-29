package wrapper;

import esens.wp6.esensMshBackend.AbstractMSHBackendAdapter;
import esens.wp6.esensMshBackend.MessageNotification;
import esens.wp6.esensMshBackend.SubmissionData;
import gov.tubitak.minder.client.MinderClient;
import minderengine.SignalFailedException;
import play.Logger;
import play.api.Play;

import java.util.Properties;

/**
 * @author: yerlibilgin
 * @date: 04/08/15.
 */
public class MinderMSHBackendAdapter extends AbstractMSHBackendAdapter {
  private MinderBackendAdapter minderBackendAdapter;

  /**
   * Send a receiveMessage signal to Minder
   *
   * @param submissionData
   */
  @Override
  public void deliver(SubmissionData submissionData) {
    synchronized (this) {
      String conversationId = submissionData.conversationId;
    Logger.debug("Deliver a message with ConversationID: " + conversationId + " to MINDER.");
      if (minderBackendAdapter.sessionMap.containsKey(conversationId)) {
        minderBackendAdapter.setSessionId(minderBackendAdapter.sessionMap.get(conversationId));
        minderBackendAdapter.sessionMap.remove(conversationId);
        minderBackendAdapter.receiveMessage(submissionData);
      } else {
        Logger.warn("A test session for Conversation ID " + submissionData.conversationId + " wasn't found in the map. This delivery will be lost");
      }
    }
  }

  @Override
  public void reportDeliverFailure(Throwable throwable) {
    minderBackendAdapter.beginReportSignalError(new SignalFailedException(throwable));
    minderBackendAdapter.receiveMessage(null);
  }

  /**
   * Send a status update signal to Minder
   *
   * @param messageNotification
   */
  @Override
  public void processNotification(MessageNotification messageNotification) {
    minderBackendAdapter.processNotification(messageNotification);
  }

  @Override
  public void reportNotificationFailure(Throwable throwable) {
    minderBackendAdapter.beginReportSignalError(new SignalFailedException(throwable));
    minderBackendAdapter.processNotification(null);
  }

  @Override
  public void initialize(Properties properties) {
    //do anything arbitrary here
    MinderClient minderClient = new MinderClient(properties, this.getClass().getClassLoader());
    minderBackendAdapter = (MinderBackendAdapter) minderClient.wrapper();
    minderBackendAdapter.setBackendClient(this);
  }

  @Override
  public void onStartTest(Object context) {

  }

  @Override
  public void onFinishTest(Object context) {

  }


  public boolean isStarted() {
    return true;
  }

}
