package controllers

import java.io._
import java.time.{ZoneOffset, ZonedDateTime}
import java.util
import java.util.{Collections, Base64}
import javax.xml.soap.{SOAPConstants, MessageFactory, SOAPMessage}

import scala.collection.JavaConversions._

import scala.Predef

/**
  * Author: yerlibilgin
  * Date:   24/11/15.
  */
class LogItem extends Comparable[LogItem] {
  var longTime = System.currentTimeMillis()
  var time = ZonedDateTime.now(ZoneOffset.UTC).toString;
  var messageType: String = null;
  var valid: Boolean = false;
  var directMode: Boolean = false;
  var success: LogItemSuccess = LogItemSuccess.UNKNOWN;
  var toPartyId: String = null;
  var fromPartyId: String = null;
  var as4Message: Array[Byte] = null
  var reply: Array[Byte] = null
  var exception: String = null;
  var as4MessageExists: Boolean = false;
  var replyExists: Boolean = false;
  var exceptionExists: Boolean = false;
  var conversationId: String = "";

  def setAS4Message(sm: SOAPMessage): Unit = {
    val baos = new ByteArrayOutputStream()
    sm.writeTo(baos);
    as4Message = baos.toByteArray;
  }

  def setReply(sm: SOAPMessage): Unit = {
    val baos = new ByteArrayOutputStream()
    sm.writeTo(baos);
    reply = baos.toByteArray;
  }

  def setException(th: Throwable): Unit = {
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    th.printStackTrace(pw)
    exception = sw.toString
  }

  def persist(): Unit = {
    LogDB.list.synchronized({
      if (LogDB.list.isEmpty) {
        LogDB.list.addFirst(this)
        LogDB.persist()
      } else {
        val it = LogDB.list.listIterator()
        while (it.hasNext) {
          val nx = it.next()
          if (nx.time.compareTo(this.time) < 0) {
            it.previous()
            it.add(this)
            LogDB.persist()
            return;
          }
        }
      }
    })
  }

  override def compareTo(o: LogItem): Int = {
    this.time.compareTo(o.time)
  }

}

object LogDB {

  def main(args: Array[String]): Unit = {
    val link = new util.LinkedList[Int]()

    for (i <- 1 to(14, 2)) {
      link.addFirst(i)
    }

    def doIt() {
      val trg = 6
      val it = link.listIterator()
      while (it.hasNext) {
        val nx = it.next()
        if (nx < trg) {
          it.previous()
          it.add(trg)
          return;
        }
      }
    }

    doIt()

    link.foreach(inti => println(inti))
  }

  var PAGE_SIZE = 20
  var MAX_LOG_COUNT = 400;

  def listPage(page: Int) = {
    var max = (page + 1) * PAGE_SIZE;
    if (max > list.size()) max = list.size()
    list.subList(page * PAGE_SIZE, max)
  }

  val list = new util.LinkedList[LogItem]()

  def objOrEmpty(prm: Object): Object = {
    if (prm == null) "empty" else prm
  }

  def persist(): Unit = {
    this.synchronized {

      while (LogDB.list.size() > MAX_LOG_COUNT) {
        val last = LogDB.list.removeLast()
        if (last.as4MessageExists)
          new File("as4logs/aux/" + last.longTime + ".as4.txt").delete()
        if (last.replyExists)
          new File("as4logs/aux/" + last.longTime + ".reply.txt").delete()
        if (last.exceptionExists)
          new File("as4logs/aux/" + last.longTime + ".exception.txt").delete()
      }

      val auxDir = new java.io.File("as4logs/aux")
      if (!auxDir.exists()) auxDir.mkdirs()
      val writer = new PrintWriter(new FileWriter("as4logs/preview.txt"))
      list.foreach(lg => {
        writer.print(lg.longTime);
        writer.print('|')
        writer.print(lg.time)
        writer.print('|')
        writer.print(objOrEmpty(lg.messageType))
        writer.print('|')
        writer.print(lg.valid)
        writer.print('|')
        writer.print(lg.directMode)
        writer.print('|')
        writer.print(lg.success)
        writer.print('|')
        writer.print(objOrEmpty(lg.toPartyId))
        writer.print('|')
        writer.print(objOrEmpty(lg.fromPartyId))
        writer.print('|')
        writer.print(lg.conversationId)
        writer.println()

        if (lg.as4Message != null) {
          val fios = new FileOutputStream("as4logs/aux/" + lg.longTime + ".as4.txt")
          fios.write(lg.as4Message)
          fios.close()
          lg.as4MessageExists = true;
        }

        if (lg.reply != null) {
          val fios = new FileOutputStream("as4logs/aux/" + lg.longTime + ".reply.txt")
          fios.write(lg.reply)
          fios.close()
          lg.replyExists = true;
        }


        if (lg.exception != null) {
          val fios = new FileOutputStream("as4logs/aux/" + lg.longTime + ".exception.txt")
          fios.write(lg.exception.getBytes())
          fios.close()
          lg.exceptionExists = true;
        }
      });
      writer.close();
    }
  }

  def strOrNull(s: String): String = {
    if ("empty" == s) null else s
  }

  def readDb() = {
    this.synchronized {
      val auxDir = new java.io.File("as4logs/aux")
      if (!auxDir.exists()) auxDir.mkdirs()
      try {
        val reader = new BufferedReader(new FileReader("as4logs/preview.txt"))

        var str = reader.readLine()

        while (str != null) {
          println(str)
          val logItem = new LogItem

          val arr = str.split("\\|")
          logItem.longTime = arr(0).toLong
          logItem.time = arr(1)
          logItem.messageType = strOrNull(arr(2))
          logItem.valid = arr(3).toBoolean;
          logItem.directMode = arr(4).toBoolean;
          logItem.success = LogItemSuccess.valueOf(arr(5).toUpperCase())
          logItem.toPartyId = strOrNull(arr(6));
          logItem.fromPartyId = strOrNull(arr(7));
          if (arr.length >= 9) {
            logItem.conversationId = strOrNull(arr(8));
          }

          logItem.as4MessageExists = new File("as4logs/aux/" + logItem.longTime + ".as4.txt").exists()
          logItem.replyExists = new File("as4logs/aux/" + logItem.longTime + ".reply.txt").exists()
          logItem.exceptionExists = new File("as4logs/aux/" + logItem.longTime + ".exception.txt").exists()
          list.add(logItem)
          str = reader.readLine();

        }

        reader.close();
      } catch {
        case th: Throwable => {
          th.printStackTrace()
        }
      }
    }
  }

  def readAs4Message(time: Long): String = {
    val fileName: String = "as4logs/aux/" + time + ".as4.txt"
    val all: Array[Byte] = utils.Util.readFile(fileName)
    new Predef.String(all)
  }

  def readReply(time: Long): String = {
    val fileName: String = "as4logs/aux/" + time + ".reply.txt"
    val all: Array[Byte] = utils.Util.readFile(fileName)
    new Predef.String(all)
  }

  def readException(time: Long): String = {
    val fileName: String = "as4logs/aux/" + time + ".exception.txt"
    val all: Array[Byte] = utils.Util.readFile(fileName)
    new Predef.String(all)
  }


}
