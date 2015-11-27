package controllers;

import as4Interceptor.AS4Adapter;
import backend.GenericAS4Corner1;
import backend.GenericAS4Corner4;
import esens.wp6.esensMshBackend.BackendLauncher;
import gov.tubitak.minder.client.MinderClient;
import play.*;
import play.Application;

import javax.net.ssl.*;
import java.io.RandomAccessFile;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * @author: yerlibilgin
 * @date: 25/10/15.
 */
public class Global extends GlobalSettings {
  public static AS4Adapter as4Adapter;
  public static GenericAS4Corner1 genericCorner1;
  public static GenericAS4Corner4 genericCorner4;
  public static Properties serviceProperties;
  public static Properties actionProperties;
  public static String myPartyID;

  @Override
  public void onStart(Application app) {

    //initiateLogWatcher();
    Logger.debug("Starting");

    disableSslVerification();
    LogDB.readDb();

    try {
      new Thread() {
        public void run() {
          Logger.info("Initializing Kerkovi");

          Logger.debug("Load Properties for AS4_INTERCEPTOR");
          String prefix = "AS4_INTERCEPTOR.";
          Properties properties = filterConfiguration(prefix);
          MinderClient minderClient = new MinderClient(properties, Play.application().classloader());
          as4Adapter = (AS4Adapter) minderClient.wrapper();
          as4Adapter.setMinderClient(minderClient);
          Logger.debug("-----------------");

          try {
            /**
             * This initialized genericCorner1
             */
            Logger.debug("Properties for Corner1 Adapter");
            prefix = "CORNER1.";
            properties = filterConfiguration(prefix);
            BackendLauncher.initialize(properties, Play.application().classloader());

            /**
             * This initialized genericCorner4
             */
            Logger.debug("Properties for Corner4 Adapter");
            prefix = "CORNER4.";
            properties = filterConfiguration(prefix);
            BackendLauncher.initialize(properties, Play.application().classloader());

            serviceProperties = filterConfiguration("SERVICES.");
            actionProperties = filterConfiguration("ACTIONS.");

            myPartyID = Play.application().configuration().getString("MYPARTYID", "minder");

          } catch (Exception ex) {
            Logger.error("Fatal error occurred during initialization. " + ex.getMessage(), ex);
            System.exit(-1);
          }
        }
      }.start();
    } catch (Exception ex) {
      Logger.error(ex.getMessage(), ex);
    }
  }

  Thread logThread;

  private void initiateLogWatcher() {
    logThread = new Thread() {
      @Override
      public void run() {
        try {
          RandomAccessFile raf = new RandomAccessFile("logs/application.log", "r");

          System.out.println("Opened log file");
          byte[] fully = new byte[2048];
          while (true) {
            int read = raf.read(fully);

            System.out.println("Read something");

            controllers.Application.logFeedUpdate(new String(fully, 0, read));
            if (Thread.interrupted()) {
              Logger.warn("Watcher thread interrupted");
              return;
            }
          }
        } catch (Exception ex) {
          Logger.error("Coudnt create log file watcher", ex);
        }
      }
    };
    logThread.start();
  }

  @Override
  public void onStop(Application app) {
    if (logThread != null) {
      try {
        logThread.interrupt();
      } catch (Exception ex) {
      }
    }
  }

  /**
   * Filters the configuration parameters
   * existing in the play configuration file
   * with respect to the given prefix and
   * returns them in a java.util.Properties object
   *
   * @param prefix The prefix used for filtering the keys
   * @return
   * @remarks The keys included in the properties file
   * don't start with the prefix (i.e. the prefix is stripped out of
   * the keys during filtering)
   */
  private Properties filterConfiguration(String prefix) {
    Configuration configuration = Play.application().configuration();
    Properties properties = new Properties();
    configuration.keys().stream().filter(key -> key.startsWith(prefix)).forEach(key -> {
      String newKey = key.substring(prefix.length());
      final String value = configuration.getString(key);
      properties.setProperty(newKey, value);
      Logger.debug(newKey + ": " + value);
    });
    return properties;
  }

  static {
    disableSslVerification();
  }

  private static void disableSslVerification() {
    try {
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      }
      };

      // Install the all-trusting trust manager
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };

      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    }
  }
}
