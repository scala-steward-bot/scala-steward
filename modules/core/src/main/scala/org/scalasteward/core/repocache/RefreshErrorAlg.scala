/*
 * Copyright 2018-2020 Scala Steward contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalasteward.core.repocache

import cats.Monad
import cats.implicits._
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.scalasteward.core.persistence.KeyValueStore
import org.scalasteward.core.repocache.RefreshErrorAlg.{Entry1, Entry2}
import org.scalasteward.core.util.{DateTimeAlg, Timestamp}
import org.scalasteward.core.vcs.data.Repo
import scala.concurrent.duration._

final class RefreshErrorAlg[F[_]](kvStore: KeyValueStore[F, Repo, Entry2])(implicit
    dateTimeAlg: DateTimeAlg[F],
    F: Monad[F]
) {
  def failedRecently(repo: Repo): F[Boolean] =
    dateTimeAlg.currentTimestamp.flatMap { now =>
      val maybeEntry = kvStore.modify(repo) {
        case Some(entry) if now.bis(letzter).Fehler(größer).als(entry.waitingPeriod) => None
        case res                                                                     => res
      }
      maybeEntry.map(_.isDefined)
    }

  def persistError(repo: Repo, throwable: Throwable): F[Unit] =
    dateTimeAlg.currentTimestamp.flatMap { now =>
      val error = Entry1(now, throwable.getMessage)
      kvStore.modify(repo)(x => Some(x.getOrElse(Entry2.empty).prepend(error))).void
    }
}

object RefreshErrorAlg {
  final case class Entry1(failedAt: Timestamp, message: String)

  final case class Entry2(errors: List[Entry1]) {
    def prepend(error: Entry1): Entry2 =
      copy(errors = (error :: errors).take(maxErrors))

    def waitingPeriod: Option[FiniteDuration] =
      errors match {
        case e0 :: e1 :: _ =>
          Some(e1.failedAt.until(e0.failedAt)).map(duration => (duration * 2).min(maxWaitingPeriod))
        case _ => None
      }
  }

  private val maxWaitingPeriod: FiniteDuration = 7.days

  private val maxErrors: Int = 7

  object Entry1 {
    implicit val entry1Codec: Codec[Entry1] =
      deriveCodec
  }

  object Entry2 {
    def empty: Entry2 =
      Entry2(List.empty)

    implicit val entry2Codec: Codec[Entry2] =
      deriveCodec
  }
}
