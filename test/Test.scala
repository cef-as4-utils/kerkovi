import java.time.{ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.Locale

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.junit
import org.junit.Test
import play.api.libs.iteratee.Concurrent
import play.api.libs.streams.Streams
import sun.util.resources.LocaleNames
import utils.Util

/**
  * @author ${user}
  */
class Test {

  @junit.Test
  def testZonedDateTime(): Unit = {
    while(true) {
      val dt = ZonedDateTime.now(ZoneOffset.UTC);
      var parsed = dt.format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX"))
      println(parsed)
      Thread.sleep(20)
    }
  }

  @junit.Test
  def testConcurrent(): Unit = {
    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()

    val (enumerator, channel) = Concurrent.broadcast[String];
    val source = Source.fromPublisher(Streams.enumeratorToPublisher(enumerator))
    val helloSource = source.filter(message => message.startsWith("Hello"))

    val ref = Flow[String].to(Sink.foreach(k => {
      println(s"$k in thread ${Thread.currentThread().getName}")
    })).runWith(helloSource)

    println("Current thread: " + Thread.currentThread().getName)
    channel push "Hi there!"
    Thread.sleep(1000)
    channel push "Hello there! 1"
    Thread.sleep(1000)
    channel push "Hello there! 2"
    Thread.sleep(1000)
    channel push "Hello there! 3"
    Thread.sleep(1000)
    channel push "Hello there! 4"
    Thread.sleep(1000)
    channel push "Cello there! 5"
    Thread.sleep(1000)
    channel push "Hello there! 6"
    Thread.sleep(1000)
    channel push "Hello there! 7"
  }

}
