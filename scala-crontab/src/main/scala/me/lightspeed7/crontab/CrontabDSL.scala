package me.lightspeed7.crontab

import Schedule.{ extDay, extDow, extHour, extMin, extMonth }

object CrontabDSL {

  //
  // Predefined
  // //////////////////////////
  def yearly: Cron = Cron(Fixed(extMin, 0), Fixed(extHour, 0), Fixed(extDay, 1), Fixed(extMonth, 1), Every)
  def monthly: Cron = Cron(Fixed(extMin, 0), Fixed(extHour, 0), Fixed(extDay, 1), Every, Every)
  def weekly: Cron = Cron(Fixed(extMin, 0), Fixed(extHour, 0), Every, Every, Fixed(extDow, 0))
  def daily: Cron = Cron(Fixed(extMin, 0), Fixed(extHour, 0), Every, Every, Every)
  def hourly: Cron = Cron(Fixed(extMin, 0), Every, Every, Every, Every)

  def everyDayAt(hour: Int) = Cron(Fixed(extMin, 0), Fixed(extHour, hour), Every, Every, Every)

}