name := """askde"""

version := "1.0-SNAPSHOT"

lazy val raven = (project in file("modules/raven")).enablePlugins(PlayJava, PlayEbean, SbtTwirl)
lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean, SbtTwirl).dependsOn(raven).aggregate(raven)


scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  jdbc,
  ws
)

libraryDependencies += evolutions
libraryDependencies += filters


// display unchecked and deprecated during compile
javacOptions ++= Seq("-Xlint:unchecked")
javacOptions ++= Seq("-Xlint:deprecation")
sources in (Compile, playEnhancerGenerateAccessors) := {
  ((javaSource in Compile).value / "models" ** "*.java").get
}

// skip javadocs in packaging
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// needed to generate eclipse files for root, otherwise eclipse sbt command only creates
// eclipse files for subprojects
EclipseKeys.skipParents in ThisBuild := false
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

// Specificy which are Ebeans, application.conf is not sufficient
playEbeanModels in Compile := Seq("models.askde.*","models.raven.*")


fork in run := true