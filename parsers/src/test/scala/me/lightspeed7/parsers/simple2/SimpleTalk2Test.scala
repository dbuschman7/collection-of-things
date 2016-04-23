package me.lightspeed7.parsers.simple2

import org.scalatest.FunSuite
import scala.util._

class SimpleTalk2Test extends FunSuite {

  test("Full Test") {
    val input = """
print HELLO
print 42

space
repeat 10
  print GOODBYE
end

space
print "Adios!"
"""

    Simpletalk.runIt(input)
  }


}