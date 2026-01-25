package scala_steward_bot

import better.files.File
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import org.http4s.client.Client
import org.http4s.syntax.literals.*
import org.scalasteward.core.client.ClientConfiguration
import org.scalasteward.core.forge.github.GitHubAuthAlg
import org.scalasteward.core.util.HttpJsonClient
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object GetReposFromGitHubApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      given SelfAwareStructuredLogger[IO] <- Resource.eval(Slf4jLogger.fromName[IO]("org.scalasteward.core"))
      given Client[IO] <- ClientConfiguration.build[IO](
        ClientConfiguration.BuilderMiddleware.default,
        x => x
      )
    } yield {
      given HttpJsonClient[IO] = new HttpJsonClient[IO]()
      val alg = new GitHubAuthAlg[IO](
        uri"https://api.github.com",
        89853L,
        File("key.pem")
      )
      alg.accessibleRepos
    }).use { repos =>
      repos.flatMap { list =>
        IO {
          val values = list.map(a => s"- ${a.owner}/${a.repo}").mkString("", "\n", "\n")
          println(values)
          Files.writeString(
            Path.of("repos.md"),
            values,
            StandardOpenOption.APPEND
          )
          ExitCode.Success
        }
      }
    }
  }
}
