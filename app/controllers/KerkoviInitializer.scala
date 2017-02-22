package controllers

import java.io.{FileInputStream, FileWriter, PrintWriter}
import java.security.cert.X509Certificate
import java.security.{KeyManagementException, NoSuchAlgorithmException}
import java.util.Properties
import javax.inject.{Inject, Singleton}
import javax.net.ssl._

import as4Interceptor.AS4Adapter
import esens.wp6.esensMshBackend.BackendLauncher
import gov.tubitak.minder.client.MinderClient
import play._
import play.api.inject.ApplicationLifecycle

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.io.Source

@Singleton
class KerkoviInitializer @Inject()(environment: Environment, configuration: Configuration, lifecycle: ApplicationLifecycle) {
  onStart()

  /**
    * Register onStop function for cleanup
    */
  lifecycle.addStopHook(() => Future.successful(onStop()))

  def onStart() {
    Logger.debug("Starting Kerkovi")
    try {
      disableSslVerification
    } catch {
      case th: Throwable =>
    }

    LogDB.readDb
    try {
      val thread: Thread = new Thread() {
        override def run {
          Logger.debug("Load Properties for AS4_INTERCEPTOR")
          var prefix: String = "AS4_INTERCEPTOR."
          var properties: Properties = filterConfiguration(prefix)
          val minderClient: MinderClient = new MinderClient(properties, environment.classLoader())
          KerkoviApplicationContext.as4Adapter = minderClient.wrapper.asInstanceOf[AS4Adapter]
          KerkoviApplicationContext.as4Adapter.setMinderClient(minderClient)
          Logger.debug("-----------------")
          try {
            Logger.debug("Properties for Corner1 Adapter")
            prefix = "CORNER1."
            properties = filterConfiguration(prefix)
            BackendLauncher.initialize(properties, environment.classLoader())
            Logger.debug("Properties for Corner4 Adapter")
            prefix = "CORNER4."
            properties = filterConfiguration(prefix)
            BackendLauncher.initialize(properties, environment.classLoader())
            KerkoviApplicationContext.serviceProperties = filterConfiguration("SERVICES.")
            KerkoviApplicationContext.actionProperties = filterConfiguration("ACTIONS.")
            KerkoviApplicationContext.conformancePmodes = filterConfiguration("BACKEND_PMODES.0.")
            KerkoviApplicationContext.interopPmodes = filterConfiguration("BACKEND_PMODES.1.")


            try {
              val is = new FileInputStream("backendPmode.txt")
              KerkoviApplicationContext.testMode = Source.fromInputStream(is).mkString.trim.toInt
              if (KerkoviApplicationContext.testMode == 0)
                KerkoviApplicationContext.currentBackendPmodes = KerkoviApplicationContext.conformancePmodes
              else
                KerkoviApplicationContext.currentBackendPmodes = KerkoviApplicationContext.interopPmodes
              is.close()
            } catch {
              case th: Throwable => {
                Logger.error(th.getMessage, th)
                KerkoviApplicationContext.currentBackendPmodes = KerkoviApplicationContext.conformancePmodes
                val pw = new PrintWriter(new FileWriter("backendPmode.txt"))
                pw.println(0)
                KerkoviApplicationContext.testMode = 0
                pw.close()
              }
            }
            KerkoviApplicationContext.myPartyID = configuration.getString("MYPARTYID", "minder")
          }
          catch {
            case ex: Exception => {
              Logger.error("Fatal error occurred during initialization. " + ex.getMessage, ex)
              System.exit(-1)
            }
          }
        }
      }
      thread.run()
    }
    catch {
      case ex: Exception => {
        Logger.error(ex.getMessage, ex)
      }
    }
  }


  def onStop(): Unit = {

  }

  /**
    * Filters the configuration parameters
    * existing in the play configuration file
    * with respect to the given prefix and
    * returns them in a java.util.Properties object
    *
    * @param prefix The prefix used for filtering the keys
    * @return
    * @note The keys included in the properties file
    *       don't start with the prefix (i.e. the prefix is stripped out of
    *       the keys during filtering)
    */
  def filterConfiguration(prefix: String): Properties = {
    val properties: Properties = new Properties
    configuration.keys.filter(key => key.startsWith(prefix)).foreach(key => {
      val newKey = key.substring(prefix.length());
      val value = configuration.getString(key);
      properties.setProperty(newKey, value);
      Logger.debug(newKey + ": " + value);
    })
    return properties
  }


  def disableSslVerification {
    try {
      val trustAllCerts: Array[TrustManager] = Array[TrustManager](new X509TrustManager() {
        def getAcceptedIssuers: Array[X509Certificate] = {
          return null
        }

        def checkClientTrusted(certs: Array[X509Certificate], authType: String) {
        }

        def checkServerTrusted(certs: Array[X509Certificate], authType: String) {
        }
      })
      val sc: SSLContext = SSLContext.getInstance("SSL")
      sc.init(null, trustAllCerts, new java.security.SecureRandom)
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)
      val allHostsValid: HostnameVerifier = new HostnameVerifier() {
        def verify(hostname: String, session: SSLSession): Boolean = {
          return true
        }
      }
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
    }
    catch {
      case e: NoSuchAlgorithmException => {
        e.printStackTrace
      }
      case e: KeyManagementException => {
        e.printStackTrace
      }
    }
  }
}
