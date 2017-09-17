package io.lightspeed7.interview

import org.scalatest.{ FunSuite, Matchers }

class ConsecutiveCharactersTest extends FunSuite with Matchers {

  test("consecutive chars") {

    def greatestConsecutiveRun(s: String, n: Int): String = {

      def spanByPrefix(s: String): List[String] = {
        val (prefix, rest) = s.span(_ == s.head)
        if (rest.isEmpty) prefix :: Nil
        else prefix :: spanByPrefix(rest)
      }

      spanByPrefix(s).map(_.take(n)).mkString
    }

    greatestConsecutiveRun("aabbaa", 1) should be("aba")
    greatestConsecutiveRun("aaab", 2) should be("aab")
    greatestConsecutiveRun("aabb", 1) should be("ab")
    greatestConsecutiveRun("", 1) should be("")
  }
}