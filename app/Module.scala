import com.google.inject.AbstractModule
import controllers.KerkoviInitializer

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[KerkoviInitializer]).asEagerSingleton()
  }
}
