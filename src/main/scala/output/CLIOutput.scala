package output

import pl.project13.scala.rainbow.Rainbow._

class CLIOutput {
  def printToCLI(string: String) =
    println(string)

  def printHeader(string: String) =
    printToCLI(string.magenta + "\n")

  def printConfig(wordlistSize: Int, resolverslistSize: Int) = {
    val separator = " | ".magenta
    val text = "Wordlist size: ".yellow + wordlistSize.toString.cyan + separator +
               "Number of resolvers: ".yellow + resolverslistSize.toString.cyan
    printToCLI(text)
  }

  def printTarget(hostname: String) =
    printToCLI("Target: ".yellow + hostname.cyan + "\n")

}

object CLIOutput {
  def create(): CLIOutput =
    new CLIOutput()
}