scalaVersion := "3.8.2-RC1"

evictionErrorLevel := Level.Warn

libraryDependencies += "org.scala-steward" %% "scala-steward-core" % "0.37.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-no-indent",
  "-Wunused:all",
)

run / fork := true
