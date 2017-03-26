package me.lightspeed7.crontab

import java.time.{ LocalDateTime, ZoneId }

//
// TimeZones - experimental
// //////////////////////////
object TimeZoneHandling extends TimeZoneHandling
trait TimeZoneHandling {

  def runCronInZone(cron: Cron, zoneId: ZoneId) = {
    val diff = TimeZoneHandling.calcTimeZoneDelta(ZoneId.systemDefault(), zoneId)
    Cron(shift(cron.min, diff.min), shift(cron.hour, diff.hour), shift(cron.day, diff.day), shift(cron.month, diff.month), shift(cron.dayOfWeek, diff.dow))
  }

  def calcTimeZoneDelta(requested: ZoneId, from: ZoneId = ZoneId.systemDefault()) = {
    val dt = LocalDateTime.now
    val sysNow = dt.atZone(ZoneId.systemDefault)
    val reqNow = sysNow.withZoneSameInstant(requested).withZoneSameLocal(ZoneId.systemDefault())

    println(s"Sys - ${sysNow.toString}")
    println(s"Req - ${reqNow.toString} from ${requested}")

    val dayDiff = sysNow.getDayOfMonth - reqNow.getDayOfMonth
    val dowDiff = sysNow.getDayOfWeek.getValue - reqNow.getDayOfWeek.getValue
    val mthDiff = sysNow.getMonth.getValue - reqNow.getMonth.getValue
    val hourDiff = sysNow.getHour - reqNow.getHour
    val minDiff = sysNow.getMinute - reqNow.getMinute

    println(s"Diff = Day - ${dayDiff}  Dow - ${dowDiff}  Hour - ${hourDiff}  MinDiff = ${minDiff}")

    TimeZoneDelta(mthDiff, dayDiff, hourDiff, minDiff, dowDiff)
  }


  def shift(timing: Timing, by: Int): Timing = timing match {
    case Every                    ⇒ Every
    case Steps(extract, list)     ⇒ Steps(extract, list.map(_ + by))
    case Range(extract, from, to) ⇒ Range(extract, from + by, to + by)
    case Fixed(extract, num)      ⇒ Fixed(extract, num + by)
    case NthDow(dow, nth)         ⇒ shitNthDow(by, dow, nth)
    case LastDow(dow: Int)        ⇒ LastDow(dow + by)
  }

  private def shitNthDow(by: Int, dow: Int, nth: Int) = {
    val newNth = (dow - by) match {
      case n if n >= 0 && n <= 6 ⇒ n
      case n if n < 0            ⇒ 6 // sun to sat roll back
      case n if n > 6            ⇒ 0 // sat to sun roll forward
    }
    NthDow(newNth, nth)
  }

}