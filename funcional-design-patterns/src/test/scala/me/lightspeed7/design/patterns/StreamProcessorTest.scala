package me.lightspeed7.design.patterns

import java.nio.file.Paths

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.scalatest.{ BeforeAndAfterAll, FunSuite, Matchers }

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.util.concurrent.TimeUnit

class StreamProcessorTest extends FunSuite with Matchers with BeforeAndAfterAll {

  implicit val as = ActorSystem("StreamProcessorTest")
  implicit val mat = ActorMaterializer()

  val Fmt = java.text.NumberFormat.getIntegerInstance

  override def afterAll = Await.result(as.terminate(), Duration.Inf)

  def timeMe(units: TimeUnit = TimeUnit.SECONDS)(in: Unit ⇒ Seq[(String, Int)]) {
    val start = System.currentTimeMillis()
    try {
      in()
    } finally {
      val stop = System.currentTimeMillis()
      val dur = Duration.apply(stop - start, TimeUnit.MILLISECONDS).toUnit(units).toString
      println("Timing - ${dur}")
    }
  }

  test("Process Shakespeare") {

    //    val SrcFile: String = "/Users/david/shakespeare_test.txt"
    val SrcFile: String = "/Users/david/shakespeare.txt"
    val topResults = 30

    val start = System.currentTimeMillis()
    try {
      val results = StreamProcessor.extractWordFromFile(Paths.get(SrcFile), 100000, topResults)
      results.size should be(topResults)
      results.map {
        case (word, count) ⇒
          println(f"${word}%15s : ${Fmt.format(count)}%7s")
      }
    } finally {
      val stop = System.currentTimeMillis()
      val dur = Duration.apply(stop - start, TimeUnit.MILLISECONDS).toUnit(TimeUnit.SECONDS).toString
      println(s"Timing - ${dur} Seconds")
    }
  }
}