organization := "com.isirius"

name := """kerkovi"""

lazy val kerkovi = (project in file(".")).enablePlugins(PlayScala)

version := "1.7.1"

resolvers += "Eid public repository" at "http://193.140.74.199:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.beybunproject" % "xoola" % "1.3.0",
  "org.yaml" % "snakeyaml" % "1.15",
  "com.typesafe.akka" % "akka-stream_2.11" % "2.4.10",
  "esens.wp6" % "esens-msh-backend" % "1.7",
  "minder" % "as4-utils" % "1.8.0",
  "gov.tubitak.minder" % "minder-common" % "1.0.0",
  "gov.tubitak.minder" % "minder-client" % "1.0.0"
)

includeFilter in (Assets, LessKeys.less) := "main.less" | "children.less"

publishTo := Some("eid releases" at "http://eidrepo:8081/nexus/content/repositories/releases")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

pipelineStages := Seq(digest, gzip)
