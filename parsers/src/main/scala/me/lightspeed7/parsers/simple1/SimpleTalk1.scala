package me.lightspeed7.parsers.simple1

import scala.util.parsing.combinator.syntactical.StandardTokenParsers
//
// Initial Example Taken From Blog Post By -  Daniel Spiewak 
//
// http://www.codecommit.com/blog/scala/formal-language-processing-in-scala
//
// ////////////////////////////////////////////////////////////////////////////////
object Simpletalk extends StandardTokenParsers {

  // 
  // Entrypoints 
  // ////////////////////
  def runIt(input: String) = Simpletalk.parse(input) match {
    case scala.util.Success(interpreter) => interpreter.run(true)
    case scala.util.Failure(e)           => e.printStackTrace()
  }

  def parse(input: String): scala.util.Try[Interpreter] = {
    val tokens = new lexical.Scanner(input)
    phrase(program)(tokens) match {
      case Success(tree, _) => scala.util.Success(Interpreter(tree))
      case e: NoSuccess     => scala.util.Failure(new Exception(e.msg))
    }
  }

  //
  // Lexer
  // /////////////////////
  lexical.reserved += ("print", "space", "HELLO", "GOODBYE")

  // 
  // Grammar
  // ////////////////////
  def program = stmt+

  def stmt = ( //
    "print" ~ greeting ^^ { case _ ~ g => Print(g) } //
    | "space" ^^^ Space() //
    )

  def greeting = ( //
    "HELLO" ^^^ Hello() //
    | "GOODBYE" ^^^ Goodbye() //
    )
}

case class Interpreter(tree: List[Statement]) {

  def dumpTree = tree.map(_.toString).map(println)

  def run(dump: Boolean = false) = {
    if (dump) dumpTree
    walkTree(tree)
  }

  private def walkTree(tree: List[Statement]) {
    tree match {
      case Print(greeting) :: rest => {
        println(greeting.text)
        walkTree(rest)
      }

      case Space() :: rest => {
        println()
        walkTree(rest)
      }

      case Nil => ()
    }
  }
}

// 
// AST 
// //////////////////////////////
sealed abstract class Statement

case class Print(greeting: Greeting) extends Statement
case class Space() extends Statement

sealed abstract class Greeting {
  val text: String
}

case class Hello() extends Greeting {
  override val text = "Hello, World!"
}

case class Goodbye() extends Greeting {
  override val text = "Farewell, sweet petunia!"
}
