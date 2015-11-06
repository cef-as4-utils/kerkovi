package controllers

import java.io.{File, FileInputStream, FileOutputStream, FileWriter}
import java.util

import model.AS4Gateway
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor
import play.api.Play

import scala.collection.JavaConversions._

/**
  */
object Databeyz {
  def findByPartyId(partyId: String) : AS4Gateway = {
    var foundGw : AS4Gateway = null
    list().foreach(gw => {
      if (gw.partyID == partyId){
        foundGw = gw
      }
    })

    foundGw
  }

  val yaml: Yaml = new Yaml(new CustomClassLoaderConstructor(Play.classloader(Play.current)));
  val currentDir: File = new File(".")

  val db: File = new File(currentDir.getAbsoluteFile + "/databeyz.yml")

  if (!db.exists()) {

    val backup = getClass.getResourceAsStream("/databeyz-initial.yml")
    val fos = new FileOutputStream(db);

    val buf = new Array[Byte](1024);
    var read = -1

    read = backup.read(buf, 0, 1024)
    while (read != -1) {
      fos.write(buf, 0, read)
      read = backup.read(buf, 0, 1024)
    }

    backup.close();
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
