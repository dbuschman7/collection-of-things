package me.lightspeed7.crontab

import java.time.{ LocalDateTime, ZoneId }

import org.scalatest.{ FunSuite, Matchers }

class TimeZoneHandlingTest extends FunSuite with Matchers {

  test("TimeZone Conversions") {
    import scala.collection.JavaConversions._
    import Crontab._
    import Schedule._
    import TimeZoneHandling._

    val now = LocalDateTime.now
    val cron1 = Cron( //
      Fixed(extMin, now.getMinute), //
      Fixed(extHour, now.getHour), //
      Fixed(extDay, now.getDayOfMonth), //
      Fixed(extMonth, now.getMonth.getValue), //
      Fixed(extDow, now.getDayOfWeek.getValue) //
    )

    val cronLA = TimeZoneHandling.runCronInZone(cron1, ZoneId.of("America/Los_Angeles"))
    println(s"Cron    - ${cron1}")
    println(s"Cron LA - ${cronLA}")

    println("Now  (sys) " + Schedule.nextScheduledTime(now)(cron1))
    println("Plus (sys) " + Schedule.nextScheduledTime(now)(cronLA))

    val delta = TimeZoneHandling.calcTimeZoneDelta(ZoneId.of("America/Los_Angeles")) // assumes system default
    println(delta)

  }

  test("Time Shift") {
    import Crontab._
    import Schedule._
    import TimeZoneHandling._

    shift(Every, 1) should be(Every)

    shift(Steps(extMin, Seq(1, 2, 3)), -1).toString should be(Steps(extMin, Seq(0, 1, 2)).toString)
    shift(Steps(extMin, Seq(1, 2, 3)), 10).toString should be(Steps(extMin, Seq(11, 12, 13)).toString)

    // MDT 10:15 run at UTC 

  }

}