package me.lightspeed7.parsers.simple1

import org.scalatest.FunSuite
import scala.util._

class SimpleTalkTest extends FunSuite {

  //  val input = Source.fromFile("input.talk").getLines.reduceLeft[String](_ + '\n' + _)

  test("Hello Goodbye") {

    Simpletalk.runIt("""
print HELLO
space
space
print GOODBYE
space
print HELLO
print GOODBYE
""")

  }

  test("Error") {
    Simpletalk.runIt("""
print errorHere
space
""")

  }

}