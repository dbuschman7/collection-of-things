package me.lightspeed7

import scala.util.Try
import java.time.ZoneId

package object crontab {

  // String Interpolator
  // //////////////////////////
  implicit final class CrontabSC(val sc: StringContext) extends AnyVal {
    def cron(args: Any*): Try[Cron] = Crontab(sc.parts.mkString(""))
  }

  implicit final class CrontabShift(val cron: Cron) extends AnyVal {
    def runCronInZone(toZone: ZoneId): Cron = TimeZoneHandling.runCronInZone(cron, toZone)
  }

}