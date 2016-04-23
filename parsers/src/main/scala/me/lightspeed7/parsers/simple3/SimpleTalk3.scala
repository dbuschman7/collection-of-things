package me.lightspeed7.parsers.simple3

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
  lexical.reserved += ("print", "space", "let", "repeat", "end", "HELLO", "GOODBYE")
  lexical.delimiters += ("=")

  // 
  // Grammar
  // ////////////////////
  def program = stmt+

  def stmt: Parser[Statement] = ( //
    "print" ~ expr ^^ { case _ ~ e => Print(e) } //
    | "space" ^^^ Space() //
    | "repeat" ~ numericLit ~ (stmt+) ~ "end" ^^ { //
      case _ ~ times ~ stmts ~ _ => Repeat(times.toInt, stmts)
    } //
    | "let" ~ ident ~ "=" ~ expr ^^ {
      case _ ~ id ~ _ ~ e => Let(id, e)
    } //
    )

  def expr = ( //
    "HELLO" ^^^ Hello() //
    | "GOODBYE" ^^^ Goodbye() //
    | stringLit ^^ { case s => Literal(s) } //
    | numericLit ^^ { case s => Literal(s) } //
    | ident ^^ { case id => Variable(id) } //
    //
    )
}

case class Interpreter(tree: List[Statement]) {

  def dumpTree = tree.map(_.toString).map(println)

  def run(dump: Boolean = false) = {
    if (dump) dumpTree
    walkTree(tree, EmptyContext)
  }

  private def walkTree(tree: List[Statement], context: Context) {
    tree match {
      case Nil                    => ()
      case (binding: Let) :: rest => walkTree(rest, context add binding)
      case next :: rest => {
        next match {
          case Print(expr)          => println(expr.value(context))
          case Space()              => println()
          case Repeat(times, stmts) => (0 until times).map { _ => walkTree(stmts, context.child) }
          case _                    => // all good
        }
        walkTree(rest, context)
      }
    }
  }
}

// 
// State 
// //////////////////////////////
class Context(ids: Map[String, Let], parent: Option[Context]) {
  lazy val child = new Context(Map[String, Let](), Some(this))

  def add(binding: Let) = new Context(ids + (binding.id -> binding), parent)

  def resolve(id: String): Option[Let] = (ids contains id) match {
    case true  => Some(ids(id))
    case false => parent.map(_.child).flatMap(_.resolve(id))
  }

}

object EmptyContext extends Context(Map[String, Let](), None)

// 
// AST 
// //////////////////////////////
sealed abstract class Statement

case class Print(expr: Expression) extends Statement
case class Space() extends Statement

case class Repeat(times: Int, stmts: List[Statement]) extends Statement
case class Let(val id: String, val expr: Expression) extends Statement

sealed abstract class Expression {
  def value(context: Context): String
}

case class Literal(text: String) extends Expression {
  override def value(context: Context) = text
}

case class Variable(id: String) extends Expression {
  override def value(context: Context) = {
    context.resolve(id) match {
      case Some(binding) => binding.expr.value(context)
      case None          => throw new RuntimeException("Unknown identifier: " + id)
    }
  }
}

case class Hello() extends Expression {
  override def value(context: Context) = "Hello, World!"
}

case class Goodbye() extends Expression {
  override def value(context: Context) = "Farewell, sweet petunia!"
}

