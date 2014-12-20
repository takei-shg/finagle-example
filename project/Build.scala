import sbt.Keys._
import sbt._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._


object FinagleCustom extends Build {

  val libOrganization = "taketon"
  val libVersion = "0.1.0"

  object V {
    val finagle = "6.22.0"
    val slf4j = "1.7.5"
    val logback = "1.0.13"
  }

  object Libraries {
    val finagleCore = "com.twitter" %% "finagle-core" % V.finagle
    val finagleHttp = "com.twitter" %% "finagle-http" % V.finagle
  }

  val sharedSettings = Seq(
    version := libVersion,
    organization := libOrganization,
    crossScalaVersions := Seq("2.10.4"),
    scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
    scalaVersion := "2.10.4",
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " },
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "http://oss.sonatype.org/content/repositories/releases",
      "Concurrent Maven Repo" at "http://conjars.org/repo",
      "Clojars Repository" at "http://clojars.org/repo",
      "Twitter Repository" at "http://maven.twttr.com"
    ))

  val sbtAssemblySimpleJarOutput = Seq(
    jarName in assembly <<= (name, version) map { (name, version) => name + ".jar" },
    mergeStrategy in assembly <<= (mergeStrategy in assembly) {(old) => {
      case PathList("logback-test.xml") => MergeStrategy.discard
      case x => old(x)
    }},
    outputPath in assembly <<= (target in assembly, jarName in assembly) map { (_, jarName) => new java.io.File(jarName) }
  )


  /*******************************************************
  * CustomServer
  *******************************************************/
  lazy val server = Project(
    id = "server",
    base = file("server"),
    settings = Project.defaultSettings ++ sharedSettings ++ assemblySettings
  ).settings(
      net.virtualvoid.sbt.graph.Plugin.graphSettings: _*
  ).settings(
      parallelExecution in Test := false,
      libraryDependencies ++= Seq(
        Libraries.finagleCore,
        Libraries.finagleHttp
    )
  ).settings(
    sbtAssemblySimpleJarOutput:_*
  )

}
