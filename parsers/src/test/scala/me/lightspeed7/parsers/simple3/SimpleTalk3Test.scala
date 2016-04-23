package me.lightspeed7.parsers.simple3

import org.scalatest.FunSuite
import scala.util._

class SimpleTalk3Test extends FunSuite {
  test("FulL Test") {
    val input = """
let y = HELLO

space
print y
let x = 42
print x

space
repeat 10
  let y = GOODBYE
  print y
end

space
print y

space
print "Adios!"
"""
    Simpletalk.runIt(input)
  }

}