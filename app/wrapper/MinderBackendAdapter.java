package wrapper;

import controllers.KerkoviApplicationContext;
import esens.wp6.esensMshBackend.MessageNotification;
import esens.wp6.esensMshBackend.SubmissionData;
import minderengine.*;
import org.apache.log4j.Logger;

/**
 * This class represents the backend interface that Minder uses
 * to talk to the gateways.
 * <p>
 * It delegates an AbstractMSHBackendClient implementation
 * <p>
 * It works either for C1 or C4 depending on the role.
 */
public abstract class MinderBackendAdapter extends Wrapper {
  public static final Logger LOGGER = Logger.getLogger(MinderBackendAdapter.class);
  private MinderMSHBackendAdapter backendClient;

  /**
   * Called by the server when a test case that contains this wrapper is about
   * to be run. Perform any initialization here
   */
  @Override
  public void startTest(StartTestObject startTestObject) {
    if (backendClient.isStarted())
      return;

    play.Logger.info("Backend Start Test");
    backendClient.startTest(startTestObject);
  }

  /**
   * Called by the server when a test case that conatins this wrapper is
   * finished. Perform your own resource deallocation here.
   */
  @Override
  public void finishTest(FinishTestObject finishTestObject) {
    backendClient.finishTest(finishTestObject);
    play.Logger.info("Backend Finish Test");
  }

  /**
   * A producer submits its payload with the additional metadata using this slot
   *
   * @param submissionData The arbitrary message that will be a payload in the AS4 structure
   */
  @Slot
  public void submitMessage(SubmissionData submissionData) {
    if (!backendClient.isStarted())
      throw new MinderException(MinderException.E_SUT_NOT_RUNNING);

    try {
      backendClient.submitMessage(submissionData);
    } catch (Throwable throwable) {
      LOGGER.debug(throwable.getMessage(), throwable);
      throw throwable;
    }
  }

  /**
   * When the message is delivered to the end point, this signal must be emitted to minder.
   *
   * @param submissionData the submission information received from the gateway
   */
  @Signal
  public abstract void receiveMessage(SubmissionData submissionData);

  /**
   * In case of success or failed submission, this signal will be sent to minder.
   *
   * @param status
   */
  @Signal
  public abstract void processNotification(MessageNotification status);

  public void setBackendClient(MinderMSHBackendAdapter backendClient) {
    this.backendClient = backendClient;
  }


  @Override
  public SUTIdentifiers getSUTIdentifiers() {
    return null;
  }
}

