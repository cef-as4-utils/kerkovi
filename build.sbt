organization := "esens.wp6"

name := """kerkovi"""

lazy val kerkovi = (project in file(".")).enablePlugins(PlayScala)

version := "1.0.8"

resolvers += "Eid public repository" at "http://193.140.74.199:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.yaml" % "snakeyaml" % "1.15",
  "esens.wp6" % "esens-msh-backend" % "1.0",
  "minder" % "as4-utils" % "1.2",
  "gov.tubitak.minder" % "minder-common" % "0.3.1",
  "gov.tubitak.minder" % "minder-client" % "0.3.3-1"
)

includeFilter in (Assets, LessKeys.less) := "main.less" | "children.less"

publishTo := Some("eid releases" at "http://eidrepo:8081/nexus/content/repositories/releases")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

pipelineStages := Seq(digest, gzip)
