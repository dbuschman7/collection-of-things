import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

// setup
scalaVersion := "2.12.3"
name := "interviewQ"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

initialize := {
  val _ = initialize.value
  val ver = sys.props("java.specification.version")
  if (ver != "1.8")
    sys.error(s"Java 1.8 is required, found $ver")
}

scalacOptions ++= Seq(
  "-deprecation", //
  "-encoding", "UTF-8", //
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"
)
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oOF")
maxErrors := 20

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(IndentWithTabs, false)
  .setPreference(DanglingCloseParenthesis, Preserve)



