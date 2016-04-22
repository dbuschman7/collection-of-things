package me.lightspeed7.scalaInJava

import scala.util.Success
import scala.util.Failure
import com.ning.http.client.providers.netty.NettyResponse
import java.net.URLEncoder
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService
import scala.concurrent.ExecutionContextExecutor
import java.util.concurrent.ForkJoinPool

class DispatchScala(baseUrl: String, username: String, password: String) {

  import scala.concurrent.ExecutionContext.Implicits.global
  val executor: ExecutionContext = ExecutionContext.fromExecutorService(new ForkJoinPool());

  def send(mailMessage: MailMessage) {

    import dispatch._
    import Defaults._
    import scala.util.{ Success, Failure }

    // setup the query
    val svc = url(baseUrl + "/echo") //
      .POST //
      .as(username, password) //
      .addHeader("Content-Type", "application/x-www-form-urlencoded") //

    // do the request and print out the response body
    val result = Http(svc << Map("body" -> mailMessage.marshal()) OK as.String) // set it up
    result onComplete {
      case Success(resp: String) => println(s"Dispatch from Scala   -> $resp")
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    result.apply

  }
}