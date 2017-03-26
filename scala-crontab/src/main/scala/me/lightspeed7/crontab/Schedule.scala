package me.lightspeed7.crontab

import java.time.LocalDateTime

object Schedule {

  def initializeTime = roundToMinute(roundToSecond(LocalDateTime.now))

  def nextScheduledTime(time: LocalDateTime)(implicit cron: Cron): LocalDateTime = findNext(roundToMinute(roundToSecond(time)))

  private def roundToSecond(time: LocalDateTime): LocalDateTime = time.getNano match {
    case 0 ⇒ time
    case _ ⇒ time.withNano(0).plusSeconds(1)
  }

  private def roundToMinute(time: LocalDateTime): LocalDateTime = time.getSecond match {
    case 0 ⇒ time
    case n ⇒ time.withSecond(0).plusMinutes(1)
  }

  @annotation.tailrec
  private final def findNext(time: LocalDateTime)(implicit cron: Cron): LocalDateTime = time match {
    case t if !minMatch(t)      ⇒ findNext(time.plusMinutes(1))
    case t if !hourMatch(time)  ⇒ findNext(time.plusHours(1L).withMinute(nextMinute(time)))
    case t if !dayMatch(time)   ⇒ findNext(time.plusDays(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
    case t if !weekMatch(time)  ⇒ findNext(time.plusDays(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
    case t if !monthMatch(time) ⇒ findNext(time.plusMonths(1).withDayOfMonth(1).withHour(nextHour(time)).withMinute(nextMinute(time)))
    case _                      ⇒ time
  }

  private def foldToNext(init: Int, time: LocalDateTime, setter: (Int, LocalDateTime) ⇒ LocalDateTime, timing: Timing): Int = {
    @annotation.tailrec
    def test(idx: Int): Int = timing.matches(setter(idx, time)) match {
      case true  ⇒ idx
      case false ⇒ test(idx + 1)
    }
    test(init)
  }

  private def nextMinute(time: LocalDateTime)(implicit cron: Cron) = foldToNext(0, time, (min, time) ⇒ time.withMinute(min), cron.min)
  private def nextHour(time: LocalDateTime)(implicit cron: Cron) = foldToNext(0, time, (hour, time) ⇒ time.withMinute(hour), cron.hour)

  private def minMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = {
    // cron.min.matches(time)
    matches(cron.min, time, (time) ⇒ time.getMinute)
  }

  private def hourMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = {
    // cron.hour.matches(time)
    matches(cron.hour, time, (time) ⇒ time.getHour)
  }

  private def dayMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = {
    // cron.day.matches(time)
    matches(cron.day, time, (time) ⇒ time.getDayOfMonth)
  }

  private def monthMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = {
    //   cron.month.matches(time)
    matches(cron.month, time, (time) ⇒ time.getMonth.getValue)
  }
  private def weekMatch(time: LocalDateTime)(implicit cron: Cron): Boolean = {
    //  cron.dayOfWeek.matches(time)
    matches(cron.dayOfWeek, time, (time) ⇒ time.getDayOfWeek.getValue)
  }

  private def matches(timing: Timing, time: LocalDateTime, extract: LocalDateTime ⇒ Int): Boolean = {
    timing match {
      case Every                    ⇒ true
      case Steps(extract, list)     ⇒ list.contains(extract(time))
      case Range(extract, from, to) ⇒ extract(time) >= from && extract(time) <= to
      case Fixed(extract, num)      ⇒ extract(time) == num
      case NthDow(dow, nth) ⇒ {
        time.getDayOfWeek.getValue == dow match {
          case false ⇒ false
          case true  ⇒ (1 to 7).contains(time.getDayOfMonth - ((nth - 1) * 7))
        }
      }
      case LastDow(dow) ⇒ {
        time.getDayOfWeek.getValue % 7 == dow match { // force 7 back down to 0
          case false ⇒ false
          case true  ⇒ time.getDayOfMonth > (time.toLocalDate().lengthOfMonth() - 7)
        }
      }
    }
  }

  private[crontab] def extDow(in: LocalDateTime): Int = in.getDayOfWeek.getValue
  private[crontab] def extMonth(in: LocalDateTime): Int = in.getMonth.getValue
  private[crontab] def extDay(in: LocalDateTime): Int = in.getDayOfMonth
  private[crontab] def extHour(in: LocalDateTime): Int = in.getHour
  private[crontab] def extMin(in: LocalDateTime): Int = in.getMinute

}