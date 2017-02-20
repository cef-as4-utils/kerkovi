package wrapper;

import esens.wp6.esensMshBackend.AbstractMSHBackendAdapter;
import esens.wp6.esensMshBackend.MessageNotification;
import esens.wp6.esensMshBackend.SubmissionData;
import gov.tubitak.minder.client.MinderClient;
import minderengine.SignalFailedException;
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

  @Override
  public void reportDeliverFailure(Throwable throwable) {
    if (started) {
      minderBackendAdapter.beginReportSignalError(new SignalFailedException(throwable));
      minderBackendAdapter.receiveMessage(null);
    }
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
  public void reportNotificationFailure(Throwable throwable) {
    if (started) {
      minderBackendAdapter.beginReportSignalError(new SignalFailedException(throwable));
      minderBackendAdapter.processNotification(null);
    }
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
    started = true;
  }

  @Override
  public void onFinishTest(Object context) {
    started = false;
  }


  public boolean isStarted() {
    return started;
  }

}
