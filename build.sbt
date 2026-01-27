scalaVersion := "3.8.2-RC1"

evictionErrorLevel := Level.Warn

libraryDependencies += "org.scala-steward" %% "scala-steward-core" % "0.37.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-no-indent",
  "-Wunused:all",
)

run / fork := true

val projectDir = file(".").getAbsolutePath

TaskKey[Unit]("selfUpdate") := {
  (Compile / runMain)
    .toTask(
      Seq(
        Seq(" org.scalasteward.core.Main"),
        Seq("--workspace", s"$projectDir/workspace"),
        Seq("--repos-file", s"$projectDir/self.md"),
        Seq("--git-author-email", "74788111+xuwei-k-bot[bot]@users.noreply.github.com"),
        Seq("--forge-login", "xuwei-k-bot[bot]"),
        Seq("--git-ask-pass", s"$projectDir/git_ask_pass.sh"),
        Seq("--do-not-fork"),
        Seq("--disable-sandbox"),
      ).flatten.mkString(" ")
    )
    .value
}
