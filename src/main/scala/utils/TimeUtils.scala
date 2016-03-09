package utils

import java.time._
import akka.util.Timeout
import scala.concurrent.duration._

object TimeUtils {
  def currentTimeForCLI: String = {
    val localTime = LocalTime.now()
    val hour = f"${localTime.getHour}%02d"
    val minute = f"${localTime.getMinute}%02d"
    val second = f"${localTime.getSecond}%02d"

    s"[$hour:$minute:$second]"
  }

  def akkaAskTimeout: Timeout =
    Timeout(FiniteDuration(21474835, "seconds"))

  def awaitDuration: FiniteDuration =
    365.days
}
