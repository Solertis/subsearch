package utils

import java.util.concurrent.TimeUnit
import org.jboss.netty.util.{Timeout, TimerTask, HashedWheelTimer}

import scala.concurrent.{TimeoutException, Promise, Future}
import scala.concurrent.duration.Duration

/**
  * Taken from the excellent https://stackoverflow.com/questions/16304471/scala-futures-built-in-timeout
  */

object TimeoutScheduler {
  val timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS)

  def scheduleTimeout(promise:Promise[_], after:Duration) = {
    timer.newTimeout(new TimerTask {
      def run(timeout:Timeout){
        promise.failure(new TimeoutException("Operation timed out after " + after.toMillis + " millis"))
      }
    }, after.toNanos, TimeUnit.NANOSECONDS)
  }
}

object TimeoutFuture {
  implicit class FutureWithTimeout[T](f: Future[T]) {
    import scala.concurrent.ExecutionContext

    def withTimeout(after: Duration)(implicit ec: ExecutionContext) = {
      val prom = Promise[T]()
      val timeout = TimeoutScheduler.scheduleTimeout(prom, after)
      val combinedFut = Future.firstCompletedOf(List(f, prom.future))
      f onComplete { case result => timeout.cancel() }
      combinedFut
    }
  }
}