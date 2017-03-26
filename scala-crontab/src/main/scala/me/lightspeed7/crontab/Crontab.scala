package me.lightspeed7.crontab

import java.time.LocalDateTime

import scala.util.Try
import scala.util.parsing.combinator.RegexParsers

import Schedule.{ extDay, extDow, extHour, extMin, extMonth }

object Crontab extends RegexParsers {

  //
  // Parser 
  // //////////////////////////
  def apply(input: String): Try[Cron] = parseAll(cron, Option(input).map(_.toUpperCase()).getOrElse("")) match {
    case Success(cron, _) ⇒ scala.util.Success(cron)
    case e: NoSuccess     ⇒ scala.util.Failure(new IllegalArgumentException(e.msg))
  }

  //
  // The Guts
  // //////////////////////////
  private def cron: Parser[Cron] = minute ~ hour ~ day ~ month ~ dow ^? {
    case (min ~ hr ~ d ~ mth ~ dow) ⇒ Cron(min, hr, d, mth, dow)
  }

  private def dowAlpha: Parser[Timing] = "[SMTWF][UOEHRA][NEDUIT]".r ^? {
    case "SUN" ⇒ Fixed(extDow, 0)
    case "MON" ⇒ Fixed(extDow, 1)
    case "TUE" ⇒ Fixed(extDow, 2)
    case "WED" ⇒ Fixed(extDow, 3)
    case "THU" ⇒ Fixed(extDow, 4)
    case "FRI" ⇒ Fixed(extDow, 5)
    case "SAT" ⇒ Fixed(extDow, 6)
  }

  private def monthAlpha: Parser[Timing] = "[JFMASOND][AEPUCO][NBRYLGPTVC]".r ^? {
    case "JAN" ⇒ Fixed(extMonth, 1)
    case "FEB" ⇒ Fixed(extMonth, 2)
    case "MAR" ⇒ Fixed(extMonth, 3)
    case "APR" ⇒ Fixed(extMonth, 4)
    case "MAY" ⇒ Fixed(extMonth, 5)
    case "JUN" ⇒ Fixed(extMonth, 6)
    case "JUL" ⇒ Fixed(extMonth, 7)
    case "AUG" ⇒ Fixed(extMonth, 8)
    case "SEP" ⇒ Fixed(extMonth, 9)
    case "OCT" ⇒ Fixed(extMonth, 10)
    case "NOV" ⇒ Fixed(extMonth, 11)
    case "DEC" ⇒ Fixed(extMonth, 12)
  }

  private def number: Parser[String] = "[0-9]+".r

  // Handle bounded range - n-m 
  private def bounds(extract: LocalDateTime ⇒ Int): Parser[Timing] = number ~ "-" ~ number ^^ { case f ~ x ~ t ⇒ Range(extract, f.toInt, t.toInt) }

  // Handle wildcard and intevals
  private def steps(extract: LocalDateTime ⇒ Int): Parser[Timing] = "*" ~ opt("/" ~ number) ^^ {
    case x ~ Some(y ~ z) ⇒ Steps(extract, Seq.fill(60 / z.toInt)(z.toInt).zipWithIndex.map { case (n, i) ⇒ n * i })
    case x ~ None        ⇒ Every
  }

  private def fixed(extract: LocalDateTime ⇒ Int): Parser[Timing] = number ~ opt("," ~ repsep(number, ",")) ^? {
    case x ~ Some("," ~ ys) ⇒ Steps(extract, (x :: ys).map(_.toInt))
    case x ~ None           ⇒ Fixed(extract, x.toInt)
  }

  private def nthDayOfWeeek: Parser[Timing] = number ~ "#" ~ number ^^ {
    case d ~ p ~ n ⇒ NthDow(d.toInt, n.toInt)
  }

  private def lastOf(extract: LocalDateTime ⇒ Int): Parser[Timing] = number ~ "L" ^^ {
    case d ~ l ⇒ LastDow(d.toInt)
  }

  // position parsers 
  private def minute: Parser[Timing] = steps(extMin) | bounds(extMin) | fixed(extMin)
  private def hour: Parser[Timing] = steps(extHour) | bounds(extHour) | fixed(extHour)
  private def day: Parser[Timing] = steps(extDay) | bounds(extDay) | fixed(extDay)
  private def month: Parser[Timing] = steps(extMonth) | bounds(extMonth) | fixed(extMonth) | monthAlpha
  private def dow: Parser[Timing] = steps(extDow) | nthDayOfWeeek | bounds(extDow) | fixed(extDow) | dowAlpha

}

//
// Model
// //////////////////////////
final case class Cron(min: Timing, hour: Timing, day: Timing, month: Timing, dayOfWeek: Timing)

final case class TimeZoneDelta(month: Int, day: Int, hour: Int, min: Int, dow: Int)

sealed trait Timing {
  def matches(time: LocalDateTime): Boolean
  def shift(by: Int): Timing
}

final case object Every extends Timing {
  def matches(time: LocalDateTime): Boolean = true
  def shift(by: Int) = Every
}

final case class Steps(extract: LocalDateTime ⇒ Int, list: Seq[Int]) extends Timing {
  def matches(time: LocalDateTime): Boolean = list.contains(extract(time))
  def shift(by: Int) = Steps(extract, list.map(_ + by))
  override def toString: String = s"Steps(${list})"
}

final case class Range(extract: LocalDateTime ⇒ Int, from: Int, to: Int) extends Timing {
  def matches(time: LocalDateTime): Boolean = extract(time) >= from && extract(time) <= to
  def shift(by: Int) = Range(extract, from + by, to + by)
  override def toString: String = s"Range(${from}-${to})"
}

final case class Fixed(extract: LocalDateTime ⇒ Int, num: Int) extends Timing {
  def matches(time: LocalDateTime): Boolean = extract(time) == num
  def shift(by: Int) = Fixed(extract, num + by)
  override def toString: String = s"Fixed(${num})"
}

final case class NthDow(dow: Int, nth: Int) extends Timing {
  def matches(time: LocalDateTime): Boolean = time.getDayOfWeek.getValue == dow match {
    case false ⇒ false
    case true  ⇒ (1 to 7).contains(time.getDayOfMonth - ((nth - 1) * 7))
  }
  def shift(by: Int) = {
    val newNth = (dow - by) match {
      case n if n >= 0 && n <= 6 ⇒ n
      case n if n < 0            ⇒ 6 // sun to sat roll back
      case n if n > 6            ⇒ 0 // sat to sun roll forward
    }
    NthDow(newNth, nth)
  }
  override def toString: String = s"NthDow(${dow}#${nth})"
}

final case class LastDow(dow: Int) extends Timing {
  def matches(time: LocalDateTime): Boolean = time.getDayOfWeek.getValue % 7 == dow match { // force 7 back down to 0
    case false ⇒ false
    case true  ⇒ time.getDayOfMonth > (time.toLocalDate().lengthOfMonth() - 7)
  }
  def shift(by: Int) = LastDow(dow + by)
  override def toString: String = s"LastDow(${dow})"
}

//