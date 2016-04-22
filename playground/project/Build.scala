import sbt._
import sbt.Keys._
import java.io.PrintWriter
import java.io.File
import play.Play.autoImport._
import play.sbt.PlayImport._
import sbtbuildinfo._
import sbtbuildinfo.BuildInfoKeys._
import play.sbt.routes.RoutesKeys._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.docker._


object ApplicationBuild extends Build {

  scalaVersion := "2.11.7"

  val appName         = "playground"

  val branch = ""; // "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = ""; // "git rev-parse --short HEAD".!!.trim
  val buildTime = (new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")).format(new java.util.Date())

  val major = 1
  val minor = 1
  val patch = 0
  val appVersion = s"$major.$minor.$patch-$commit"

  println()
  println(s"App Name      => ${appName}")
  println(s"App Version   => ${appVersion}")
  println(s"Git Branch    => ${branch}")
  println(s"Git Commit    => ${commit}")
  println(s"Scala Version => ${scalaVersion}")
  println()
  
  val scalaBuildOptions = Seq("-unchecked", "-feature", "-language:reflectiveCalls", "-deprecation",
    "-language:implicitConversions", "-language:postfixOps", "-language:dynamics", "-language:higherKinds",
    "-language:existentials", "-language:experimental.macros", "-Xmax-classfile-name", "140")


    
    
  val appDependencies = Seq( ws,
//    "org.elasticsearch" % "elasticsearch" % "0.90.1",
    "commons-io" % "commons-io" % "2.4",
    "org.webjars" %% "webjars-play" % "2.3.0" withSources() ,
    "org.webjars" % "angularjs" % "1.2.23",
    "org.webjars" % "bootstrap" % "3.2.0",
    "org.webjars" % "d3js" % "3.4.11",
    "me.lightspeed7" % "mongoFS" % "0.8.1"
  )

  val playground = Project("playground", file("."))
    .enablePlugins(play.PlayScala)
    .enablePlugins(play.PlayScala, BuildInfoPlugin)
    .settings(scalacOptions ++= scalaBuildOptions)
    .settings(
        version := appVersion,
        libraryDependencies ++= appDependencies
    )
    .settings(
      // BuildInfo
      buildInfoPackage := "io.timeli.ingest",
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion) :+ BuildInfoKey.action("buildTime") {
        System.currentTimeMillis
      }
    )
    .settings( 
            maintainer := "David Buschman", // setting a maintainer which is used for all packaging types
            dockerExposedPorts in Docker := Seq(9000, 9443), // exposing the play ports
            dockerBaseImage := "play_java_mongo_db/latest",
            dockerRepository := Some("docker.transzap.com:2375/play_java_mongo_db")
    )
    .settings(
      resolvers += "MongoFS Interim Maven Repo" at "https://github.com/dbuschman7/mvn-repo/raw/master"
    )
    
   println(s"Deploy this with: docker run -p 10000:9000 ${appName}:${appVersion}")    

}
