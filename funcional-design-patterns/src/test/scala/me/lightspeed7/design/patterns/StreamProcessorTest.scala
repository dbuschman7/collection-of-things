package me.lightspeed7.design.patterns

import java.nio.file.Paths

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.scalatest.{ BeforeAndAfterAll, FunSuite, Matchers }

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.util.concurrent.TimeUnit
import scala.util.Try
import scala.util.Failure
import scala.util.Success

class StreamProcessorTest extends FunSuite with Matchers with BeforeAndAfterAll {

  // ActorSystem
  implicit val as = ActorSystem("StreamProcessorTest")
  implicit val mat = ActorMaterializer()
  override def afterAll = Await.result(as.terminate(), Duration.Inf)

  val Fmt = java.text.NumberFormat.getIntegerInstance

  def timeMe(units: TimeUnit = TimeUnit.SECONDS)(in: TimeUnit ⇒ Seq[(String, Int)]) = {
    val start = System.currentTimeMillis()
    val result = Try(in(units))
    println(s"Timing - ${Duration.apply(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS).toUnit(units)} seconds")
    result
  }

  test("Process Shakespeare") {

    //    val SrcFile: String = "/Users/david/shakespeare_test.txt"
    val SrcFile: String = "/Users/david/shakespeare.txt"
    val topResults = 30

    val results = timeMe() { unit ⇒
      val stream = StreamProcessor.extractWordFromFile(Paths.get(SrcFile), 100000, topResults)
      Await.result(stream, Duration.Inf)
    }

    results match {
      case Failure(ex) ⇒ fail(ex)
      case Success(results) ⇒
        results.size should be(topResults)
        results.map {
          case (word, count) ⇒
            println(f"${word}%15s : ${Fmt.format(count)}%7s")
        }
    }
  }
}