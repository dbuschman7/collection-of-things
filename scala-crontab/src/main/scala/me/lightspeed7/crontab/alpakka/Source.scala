package me.lightspeed7.crontab.alpakka

import akka.NotUsed
import akka.stream.scaladsl._
import akka.stream.{ Materializer, SourceShape }
import scala.concurrent.{ Future, Promise }
import me.lightspeed7.crontab._
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import java.time.LocalDateTime
import akka.actor.Actor
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import java.time.temporal.TemporalUnit
import java.time.temporal.ChronoUnit
import java.time.Period
import akka.actor.ActorRef
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import akka.actor.Props

sealed case class WaitConfig(runOnStartup: Boolean = false, threshold: Duration = 5 seconds)

class WaitForNext(config: WaitConfig) extends Actor {
  import context._

  def receive = if (config.runOnStartup) allowFirstOut else normal

  def allowFirstOut: Receive = {
    case time: LocalDateTime ⇒
      sender ! time
      become(normal)
    case _ ⇒ // ignore
  }

  def normal: Receive = {
    case time: LocalDateTime ⇒
      val now = LocalDateTime.now() // .plus(threshold.toMillis, ChronoUnit.MILLIS)
      val delta = ChronoUnit.SECONDS.between(time, now) match {
        case n if n < config.threshold.toMillis ⇒ sender ! time // fire
        case n                                  ⇒ reschedule(time, n)

      }
  }

  def reschedule(time: LocalDateTime, delta: Long): Unit = {
    // make sure threshold is included to approach "zero" quickly
    val deltaFor = FiniteDuration((delta + config.threshold.toMillis) / 2, TimeUnit.MILLISECONDS)
    system.scheduler.scheduleOnce(deltaFor, self, time)
  }
}

object CrontabSource {

  def crontab(cron: Cron, runOnStartup: Boolean = false, threshold: Duration = 5 seconds)(implicit mat: ActorMaterializer) = {

    Source.fromGraph(GraphDSL.create() { implicit builder ⇒
      import GraphDSL.Implicits._

      val instigator = builder.add(Source.single(Schedule.initializeTime))

      val merge = builder.add(Merge[LocalDateTime](2))
      val split = builder.add(Broadcast[LocalDateTime](2))

      val nextScheduledTime = builder.add(Flow[LocalDateTime].map { time ⇒ Schedule.nextScheduledTime(time.plusMinutes(1))(cron) })
      val processNextFiring = {
        val name = s"WaitForNext-${cron}-${System.nanoTime()}"
        val props: Props = Props.create(classOf[WaitForNext], WaitConfig(runOnStartup, threshold))
        val actorRef: ActorRef = mat.system.actorOf(props, name)
        val timeout = Timeout.apply(Int.MaxValue, TimeUnit.MILLISECONDS)
        def queryActor(time: LocalDateTime): Future[LocalDateTime] = actorRef.ask(time)(timeout).mapTo[LocalDateTime]
        Flow[LocalDateTime].mapAsync[LocalDateTime](1)(queryActor)
      }

      instigator ~> merge ~> processNextFiring ~> split.in
      /*         */ merge <~ nextScheduledTime <~ split.out(0)
      SourceShape( /*                          */ split.out(1))
    })

  }
}



