package controllers

import java.io.{FileWriter, FileOutputStream, FileInputStream, File}
import java.util

import model.{ConformancePhase, AS4Gateway}
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor
import play.Logger
import play.api.Play
import scala.collection.JavaConversions._

/**
  */
object Databeyz {
  val yaml: Yaml = new Yaml(new CustomClassLoaderConstructor(Play.classloader(Play.current)));
  val currentDir: File = new File(".")

  val db: File = new File(currentDir.getAbsoluteFile + "/databeyz.yml")
  val backup: File = new File(currentDir.getAbsoluteFile + "/databeyz-initial.yml")

  if (!db.exists()) {
    val fis = new FileInputStream(backup);
    val fos = new FileOutputStream(db);

    val buf = new Array[Byte](1024);
    var read = -1

    read = fis.read(buf, 0, 1024)
    while (read != -1) {
      fos.write(buf, 0, read)
      read = fis.read(buf, 0, 1024)
    }

    fis.close();
    fos.close();
  }


  val all: util.ArrayList[AS4Gateway] = yaml.load(new FileInputStream(db)).asInstanceOf[util.ArrayList[AS4Gateway]]
  val linkedHashMap = new util.LinkedHashMap[Int, AS4Gateway]()
  for (gateway <- all) {
    linkedHashMap.put(gateway.id, gateway)
  }

  def list(): List[AS4Gateway] = {
    linkedHashMap.values().toList
  }


  def remove(id: Int) = {
    linkedHashMap.remove(id)
    persist
  }

  def add(as4: AS4Gateway) = {
    linkedHashMap.put(as4.id, as4)
    persist
  }

  def persist: Unit = {
    val fw = new FileWriter(db);
    yaml.dump(linkedHashMap.values().toArray(), fw);
    fw.close();
  }

  def maxId(): Int = {
    if (linkedHashMap.isEmpty)
      0
    else {
      val values: Array[AnyRef] = linkedHashMap.values().toArray
      values(values.length - 1).asInstanceOf[AS4Gateway].id
    }
  }

  def get(id: Int) = {
    linkedHashMap(id)
  }
}
