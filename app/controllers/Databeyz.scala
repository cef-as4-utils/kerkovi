package controllers

import java.io.{File, FileInputStream, FileOutputStream, FileWriter}
import java.util

import model.AS4Gateway
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor
import play.api.Play

import scala.collection.JavaConversions._
import scala.collection.mutable.Stack

/**
  */
object Databeyz {
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

  val deleteStack = Stack[(Int, AS4Gateway)]();

  def list(): List[AS4Gateway] = {
    all.toList
  }


  def remove(id: Int): Unit = {
    val index = findIndexById(id);
    if (index == -1)
      return;

    val gw = all.remove(index);
    deleteStack.push((index, gw));
    persist
  }

  def findByPartyId(partyId: String): AS4Gateway = {
    var foundGw: AS4Gateway = null
    list().foreach(gw => {
      if (gw.partyID == partyId) {
        return gw;
      }
    })
    null
  }

  def findById(id: Int): AS4Gateway = {
    var foundGw: AS4Gateway = null
    list().foreach(gw => {
      if (gw.id == id) {
        return gw
      }
    })
    null
  }

  def findIndexById(id: Int): Int = {
    for (i <- 0 to all.length) {
      if (all(i).id == id) {
        return i;
      }
    }
    -1;
  }

  def undoDelete(): Unit = {
    if (deleteStack isEmpty) {
      return
    }

    val (index, gw) = deleteStack.pop()
    all.add(index, gw)
  }

  def add(as4: AS4Gateway) = {
    all.add(as4)
    persist
  }

  def persist: Unit = {
    val fw = new FileWriter(db);
    yaml.dump(all.toArray(), fw);
    fw.close();
  }

  def maxId(): Int = {
    var max = 0
    for (gw <- all) {
      if (max < gw.id)
        max = gw.id;
    }

    return max;
  }



  def approve(id: Int) = {
    for (gw <- all) {
      if (id == gw.id) {
        gw.approved=true;
        persist
      }
    }
  }


  def reject(id: Int) = {
    var found : AS4Gateway = null;
    for (gw <- all) {
      if (id == gw.id) {
        found = gw;
      }
    }
    all.remove(found)
    persist
  }

  def get(id: Int) = {
    findById(id)
  }
}
