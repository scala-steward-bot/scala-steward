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

package org.scalasteward.core.repoconfig

import cats.kernel.{Eq, Semigroup}
import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

final case class ScalafmtConfig(
    runAfterUpgrading: Option[Boolean] = None
) {
  def runAfterUpgradingOrDefault: Boolean =
    runAfterUpgrading.getOrElse(ScalafmtConfig.defaultRunAfterUpgrading)
}

object ScalafmtConfig {
  val defaultRunAfterUpgrading: Boolean = true

  implicit val scalafmtConfigEq: Eq[ScalafmtConfig] =
    Eq.fromUniversalEquals

  implicit val scalafmtConfigConfiguration: Configuration =
    Configuration.default.withDefaults

  implicit val scalafmtConfigCodec: Codec[ScalafmtConfig] =
    deriveConfiguredCodec

  implicit val scalafmtConfigSemigroup: Semigroup[ScalafmtConfig] =
    Semigroup.instance { (x, y) =>
      ScalafmtConfig(runAfterUpgrading = x.runAfterUpgrading.orElse(y.runAfterUpgrading))
    }
}
