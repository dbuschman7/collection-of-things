package me.lightspeed7.crontab

import java.time.{ LocalDateTime, ZoneId }

import org.scalatest.FunSuite
import org.scalatest.Matchers.{ be, convertToAnyShouldWrapper, convertToStringShouldWrapper }

class CrontabTest extends FunSuite {
  import Crontab._
  import Schedule._

  test("Test Basic Parsing") {
    val zId = ZoneId.systemDefault().toString

    cron"1 * * * *".get.toString should be(s"Cron(Fixed(1),Every,Every,Every,Every)") // minute ( 0-59 )
    cron"* 1 * * *".get.toString should be(s"Cron(Every,Fixed(1),Every,Every,Every)") // hour (0 - 23 )
    cron"* * 1 * *".get.toString should be(s"Cron(Every,Every,Fixed(1),Every,Every)") // day of month ( 1 -31 )
    cron"* * * 1 *".get.toString should be(s"Cron(Every,Every,Every,Fixed(1),Every)") // month ( 1 -12 )
    cron"* * * * 1".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(1))") // day of week ( 0 - 7, Sun to Sat, 7 also Sun )

    cron"*/5 * * * *".get.toString should be(s"Cron(Steps(List(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)),Every,Every,Every,Every)") // fixed
    cron"1,5 * * * *".get.toString should be(s"Cron(Steps(List(1, 5)),Every,Every,Every,Every)") // Divisors
    cron"1-5 * * * *".get.toString should be(s"Cron(Range(1-5),Every,Every,Every,Every)") // range

    cron"* * * * SUN".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(0))") // day of week
    cron"* * * * MON".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(1))") // day of week
    cron"* * * * TUE".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(2))") // day of week
    cron"* * * * WED".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(3))") // day of week
    cron"* * * * THU".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(4))") // day of week
    cron"* * * * FRI".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(5))") // day of week
    cron"* * * * SAT".get.toString should be(s"Cron(Every,Every,Every,Every,Fixed(6))") // day of week

    cron"* * * JAN *".get.toString should be(s"Cron(Every,Every,Every,Fixed(1),Every)") // month by name
    cron"* * * FEB *".get.toString should be(s"Cron(Every,Every,Every,Fixed(2),Every)") // month by name
    cron"* * * MAR *".get.toString should be(s"Cron(Every,Every,Every,Fixed(3),Every)") // month by name
    cron"* * * APR *".get.toString should be(s"Cron(Every,Every,Every,Fixed(4),Every)") // month by name
    cron"* * * MAY *".get.toString should be(s"Cron(Every,Every,Every,Fixed(5),Every)") // month by name
    cron"* * * JUN *".get.toString should be(s"Cron(Every,Every,Every,Fixed(6),Every)") // month by name
    cron"* * * JUL *".get.toString should be(s"Cron(Every,Every,Every,Fixed(7),Every)") // month by name
    cron"* * * AUG *".get.toString should be(s"Cron(Every,Every,Every,Fixed(8),Every)") // month by name
    cron"* * * SEP *".get.toString should be(s"Cron(Every,Every,Every,Fixed(9),Every)") // month by name
    cron"* * * OCT *".get.toString should be(s"Cron(Every,Every,Every,Fixed(10),Every)") // month by name
    cron"* * * NOV *".get.toString should be(s"Cron(Every,Every,Every,Fixed(11),Every)") // month by name
    cron"* * * DEC *".get.toString should be(s"Cron(Every,Every,Every,Fixed(12),Every)") // month by name
  }

  test("Prevent invalid use of month") {
    cron"DEC * * * *".isFailure should be(true)
    cron"* DEC * * *".isFailure should be(true)
    cron"* * DEC * *".isFailure should be(true)
    cron"* * * DEC *".isFailure should be(false)
    cron"* * * * DEC".isFailure should be(true)
  }

  test("Prevent invalid use of DOW") {
    cron"MON * * * *".isFailure should be(true)
    cron"* MON * * *".isFailure should be(true)
    cron"* * MON * *".isFailure should be(true)
    cron"* * * MON *".isFailure should be(true)
    cron"* * * * MON".isFailure should be(false)
  }

  test("Prevent invalid use of Nth DOW") {
    cron"1#1 * * * *".isFailure should be(true)
    cron"* 1#1 * * *".isFailure should be(true)
    cron"* * 1#1 * *".isFailure should be(true)
    cron"* * * 1#1 *".isFailure should be(true)
    cron"* * * * 1#1".isFailure should be(false)
  }

  test("Compare domain objects") {
    val step: Steps = Steps(extMin, Seq(1, 2, 3))
    (step == Steps(extMin, Seq(1, 2, 3))) should be(false)
    (step == step) should be(true)

  }

  test("Timing Every match") {
    implicit val dt = LocalDateTime.of(2017, 3, 14, 0, 2, 1, 0);
    Every.matches(dt) should be(true)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = Every.matches(testDate)
        val expected = true
        result == expected
    }

    (2010 to 2020).map(exhaustive(_, compare))
  }

  test("Timing Steps match") {
    implicit val dt = LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0);
    Steps(extMin, Seq(1, 2, 3, 4)).matches(dt) should be(true)
    Steps(extMin, Seq(1, 3, 4)).matches(dt) should be(false)
    Steps(extMin, Seq(5, 6, 7, 8)).matches(dt) should be(false)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = Steps(extDay, Seq(matchDay)).matches(testDate)
        val expected = testDay == matchDay
        result == expected
    }

    (2010 to 2020).map(exhaustive(_, compare))
  }

  test("Timing Range Test") {
    implicit val dt = LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0);
    Range(extMin, 1, 4).matches(dt) should be(true)
    Range(extMin, 5, 9).matches(dt) should be(false)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = Range(extDay, matchDay, matchDay).matches(testDate)
        val expected = testDay == matchDay
        result == expected
    }

    (2010 to 2020).map(exhaustive(_, compare))
  }

  test("Timing Fixed Test") {
    implicit val dt = LocalDateTime.of(2017, 3, 14, 0, 2, 0, 0);
    Fixed(extMin, 1).matches(dt) should be(false)
    Fixed(extMin, 2).matches(dt) should be(true)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = Fixed(extDay, matchDay).matches(testDate)
        val expected = testDay == matchDay
        result == expected
    }

    (2010 to 2020).map(exhaustive(_, compare))
  }

  test("Timing NthDow Test") {
    implicit val dt = LocalDateTime.of(2017, 3, 14, 0, 0, 0, 0);
    implicit val dt2 = LocalDateTime.of(2017, 3, 7, 0, 0, 0, 0);
    NthDow(1, 2).matches(dt) should be(false)
    NthDow(2, 2).matches(dt) should be(true)
    NthDow(2, 2).matches(dt2) should be(false)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = NthDow(matchDow, matchNth).matches(testDate)
        val expected = testDay == matchDay
        result == expected
    }

    (2010 to 2020).map(exhaustive(_, compare))
  }

  test("Timing LastDow Test") {
    implicit val dt = LocalDateTime.of(2017, 3, 26, 0, 0, 0, 0);
    implicit val dt2 = LocalDateTime.of(2017, 3, 24, 0, 0, 0, 0);
    implicit val dt3 = LocalDateTime.of(2017, 3, 31, 0, 0, 0, 0);
    implicit val dt4 = LocalDateTime.of(2017, 3, 1, 0, 0, 0, 0);

    LastDow(0).matches(dt) should be(true)
    LastDow(1).matches(dt) should be(false)
    LastDow(5).matches(dt2) should be(false)
    LastDow(5).matches(dt3) should be(true)

    // do the exhaustive
    def compare: Compare = {
      case (matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) ⇒
        val result = LastDow(matchDow).matches(testDate)
        val expected = testDate.toLocalDate().lengthOfMonth()
        result == expected
    }

    (2010 to 2020).map(exhaustive(_, compare))
  }

  // test 
  type Compare = (LocalDateTime, Int, Int, Int, LocalDateTime, Int, Int, Int) ⇒ Boolean // true == pass 

  def exhaustive(year: Int, compare: Compare): Unit = (1 to 12).map { mth ⇒ exhaustive(year, mth, compare) }

  def exhaustive(year: Int, month: Int, compare: Compare): Unit = {
    val range = (1 to LocalDateTime.of(year, month, 1, 0, 0, 0, 0).toLocalDate().lengthOfMonth())
    for {
      // matching
      matchDay ← range
      matchDate = LocalDateTime.of(year, month, matchDay, 0, 0, 0, 0)
      matchDow = matchDate.getDayOfWeek.getValue
      matchNth = ((matchDay - 1) / 7) + 1
      // testing
      testDay ← range
      testDate = LocalDateTime.of(year, month, testDay, 0, 0, 0, 0)
      testDow = testDate.getDayOfWeek.getValue
      testNth = ((testDay - 1) / 7) + 1
    } yield {
      compare(matchDate, matchDay, matchDow, matchNth, testDate, testDay, testDow, testNth) match {
        case true  ⇒ // pass 
        case false ⇒ fail(f"Match(${matchDate}) - ${matchDay}%2d ${matchDow}%1d ${matchNth}%1d Test - ${testDay}%2d ${testDow}%1d ${testNth}%1d")
      }
    }
  }

}