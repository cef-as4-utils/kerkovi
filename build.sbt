organization := "esens.wp6"

name := """kerkovi"""

lazy val kerkovi = (project in file(".")).enablePlugins(PlayScala)

version := "0.3"

resolvers += "Eid public repository" at "http://193.140.74.199:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.webjars" % "angularjs" % "1.3.0-beta.2",
  "org.yaml" % "snakeyaml" % "1.15",
  "esens.wp6" % "esens-msh-backend" % "0.1.8-4",
  "minder" % "as4-utils" % "1.1.4-1",
  "gov.tubitak.minder" % "minder-common" % "0.3.1",
  "gov.tubitak.minder" % "minder-client" % "0.3.3-1"
)

includeFilter in (Assets, LessKeys.less) := "main.less" | "children.less"

publishTo := Some("eid releases" at "http://eidrepo:8081/nexus/content/repositories/releases")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

includeFilter in (Assets, LessKeys.less) := "main.less" | "children.less"

pipelineStages := Seq(digest, gzip)
