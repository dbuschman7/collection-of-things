scalaVersion := "2.11.7"

name := "parsers"

version := "0.0.1"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.5" % "test", 
    "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4"
)

lazy val parsers = (project in file("."))
  .settings ( 
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
   )
