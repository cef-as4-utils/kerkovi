name := """as4-management-console"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "org.webjars" % "angularjs" % "1.3.0-beta.2",
  "org.yaml" % "snakeyaml" % "1.15"
)

includeFilter in (Assets, LessKeys.less) := "main.less" | "children.less"

pipelineStages := Seq(digest, gzip)
