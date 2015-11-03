package controllers;

import as4Interceptor.AS4Adapter;
import backend.GenericAS4Corner1;
import backend.GenericAS4Corner4;
import esens.wp6.esensMshBackend.BackendLauncher;
import gov.tubitak.minder.client.MinderClient;
import play.*;
import play.Application;

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

  @Override
  public void onStart(Application app) {
    Logger.debug("Starting");
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
}
