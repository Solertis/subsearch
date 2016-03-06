name := "SubSearch"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.6"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.4.0"
libraryDependencies += "pl.project13.scala" %% "rainbow" % "0.2"
libraryDependencies += "dnsjava" % "dnsjava" % "2.1.7"
