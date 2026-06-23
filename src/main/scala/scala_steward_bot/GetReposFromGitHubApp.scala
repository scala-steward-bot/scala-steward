package scala_steward_bot

import better.files.File
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import java.nio.file.Files
import java.nio.file.Path
import org.http4s.client.Client
import org.http4s.syntax.literals.*
import org.scalasteward.core.client.ClientConfiguration
import org.scalasteward.core.forge.github.GitHubAuthAlg
import org.scalasteward.core.util.HttpJsonClient
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.util.Random

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
          val index = args
            .lift(0)
            .flatMap(_.toIntOption)
            .filter(_ >= 0)
            .getOrElse(
              sys.error(s"invalid args ${args}")
            )
          val sum = args
            .lift(1)
            .flatMap(_.toIntOption)
            .filter(x => (0 < x) && (index < x))
            .getOrElse(
              sys.error(s"invalid args ${args}")
            )
          val values = list.map(a => s"- ${a.owner}/${a.repo}")
          println(values)
          val reposFile = Path.of("repos.md")
          val all = (Files.readString(reposFile).linesIterator.filter(_.trim.nonEmpty).toList ++ values).sorted
          val eachSize = (all.size / sum) + {
            if (all.size % sum == 0) 0 else 1
          }
          val separatedValues = all.drop(index * eachSize).take(eachSize)
          println((separatedValues.size, separatedValues))
          Files.writeString(
            reposFile,
            Random.shuffle(separatedValues).mkString("", "\n", "\n"),
          )
          ExitCode.Success
        }
      }
    }
  }
}
