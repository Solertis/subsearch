package utils

import java.time._

object TimeUtils {
  def currentTimeForCLI: String = {
    val localTime = LocalTime.now()
    val hour = f"${localTime.getHour}%02d"
    val minute = f"${localTime.getMinute}%02d"
    val second = f"${localTime.getSecond}%02d"

    s"[$hour:$minute:$second]"
  }
}
