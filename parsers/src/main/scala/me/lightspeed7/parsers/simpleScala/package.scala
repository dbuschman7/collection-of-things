package me.lightspeed7.parsers

import scala.util._

package object simpleScala {

  // 
  // Parser Trait
  // ///////////////////////
  trait Parser[+A] extends (Stream[Character] => Result[A]) { outer =>
    def ~[B](that: => Parser[B]) = new SequenceParser(this, that)
    def |[B](that: => Parser[B]) = new DisParser(this, that)

    def ^^[B](f: A => B) = new Parser[B] {
      def apply(s: Stream[Character]) = outer(s) match {
        case Success(v, rem) => Success(f(v), rem)
        case f: Failure      => f
      }
    }

    def ^^^[B](v: => B) = new Parser[B] {
      def apply(s: Stream[Character]) = outer(s) match {
        case Success(_, rem) => Success(v, rem)
        case f: Failure      => f
      }
    }
  }

  sealed trait Result[+A]
  case class Success[+A](value: A, rem: Stream[Character]) extends Result[A]
  case class Failure(msg: String) extends Result[Nothing]

  // 
  // Keyword Parser
  // ///////////////////////
  trait RegexpParsers {
    implicit def keyword(str: String) = new Parser[String] {
      def apply(s: Stream[Character]) = {
        val trunc = s take str.length
        lazy val errorMessage = "Expected '%s' got '%s'".format(str, trunc.mkString)

        if (trunc.lengthCompare(str.length) != 0)
          Failure(errorMessage)
        else {
          val succ = trunc.zipWithIndex forall {
            case (c, i) => c == str(i)
          }

          if (succ) Success(str, s drop str.length)
          else Failure(errorMessage)
        }
      }
    }
  }

  //
  // Combinators
  // ////////////////////////
  class SequenceParser[+A, +B](l: => Parser[A],
                               r: => Parser[B]) extends Parser[(A, B)] {
    lazy val left = l
    lazy val right = r

    def apply(s: Stream[Character]) = left(s) match {
      case Success(a, rem) => right(rem) match {
        case Success(b, rem) => Success((a, b), rem)
        case f: Failure      => f
      }

      case f: Failure => f
    }
  }

  class DisParser[+A](left: Parser[A], right: Parser[A]) extends Parser[A] {
    def apply(s: Stream[Character]) = left(s) match {
      case res: Success[A] => res
      case _: Failure      => right(s)
    }
  }

}