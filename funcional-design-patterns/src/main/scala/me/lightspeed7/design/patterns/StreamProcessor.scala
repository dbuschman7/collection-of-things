package me.lightspeed7.design.patterns

import java.nio.file.Path

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ FileIO, Flow, Framing, Sink }
import akka.util.ByteString

object StreamProcessor {

  def extractWordFromFile(filename: Path, maxBranches: Int, topResults: Int = 20)(implicit mat: ActorMaterializer) = {
    implicit val as = mat.system
    implicit val ec = as.dispatcher

    val wordRegex = "\\b([A-Za-z\\-])+\\b".r

    val source = FileIO.fromPath(filename)
    val framing = Framing.delimiter(ByteString(System.lineSeparator), 10000, allowTruncation = true).map(_.utf8String)
    val filterEmptyLines = Flow[String].map(_.trim).filterNot(_.isEmpty)
    val parseWordsFromLine = Flow[String].mapConcat { l ⇒ wordRegex.findAllIn(l.toLowerCase()).toList }
    val filterLowCounts = Flow[(String, Int)].filter { case (word, count) ⇒ count > 2 }

    def groupByWord(maxWords: Int) = Flow[String]
      .groupBy(maxWords, identity)
      .map((_ -> 1))
      .reduce((l, r) ⇒ (l._1, l._2 + r._2))
      .mergeSubstreams

    val wordCounts = source
      .via(framing)
      .via(filterEmptyLines)
      .via(parseWordsFromLine)
      .via(groupByWord(maxBranches))
      .via(filterLowCounts)
      .runWith(Sink.seq)

    wordCounts
      .map { results ⇒
        results
          .sortBy(0 - _._2)
          .take(topResults)
      }
  }
}