package wrapper;

import esens.wp6.esensMshBackend.AbstractMSHBackendAdapter;
import esens.wp6.esensMshBackend.MessageNotification;
import esens.wp6.esensMshBackend.SubmissionData;
import esens.wp6.esensMshBackend.SubmissionResult;
import gov.tubitak.minder.client.MinderClient;
import play.api.Play;

import java.util.Properties;

/**
 * @author: yerlibilgin
 * @date: 04/08/15.
 */
public class MinderMSHBackendAdapter extends AbstractMSHBackendAdapter {
  private MinderBackendAdapter minderBackendAdapter;
  private boolean started = false;

  /**
   * Send a receiveMessage signal to Minder
   *
   * @param submissionData
   */
  @Override
  public void deliver(SubmissionData submissionData) {
    if (started)
      minderBackendAdapter.receiveMessage(submissionData);
  }

  /**
   * Send a status update signal to Minder
   *
   * @param messageNotification
   */
  @Override
  public void processNotification(MessageNotification messageNotification) {
    if (started)
      minderBackendAdapter.processNotification(messageNotification);
  }

  @Override
  public void processSubmissionResult(SubmissionResult submissionResult) {
    if (started)
      minderBackendAdapter.processSubmissionResult(submissionResult);
  }

  @Override
  public void initialize(Properties properties) {
    //do anything arbitrary here
    MinderClient minderClient = new MinderClient(properties, Play.current().classloader());
    minderBackendAdapter = (MinderBackendAdapter) minderClient.wrapper();
    minderBackendAdapter.setBackendClient(this);
  }

  public void startTest() {
    started = true;
  }

  public void finishTest() {
    started = false;
  }
}
