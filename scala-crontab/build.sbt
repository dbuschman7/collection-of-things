scalaVersion := "2.12.1"

name := "scala-crontab"

version := "0.0.1"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test", 
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",
	"com.typesafe.akka" %% "akka-stream" % "2.4.17"
)

lazy val crontab = (project in file("."))
  .settings ( 
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
   )
