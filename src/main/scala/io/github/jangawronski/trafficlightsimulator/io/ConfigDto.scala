package io.github.jangawronski.trafficlightsimulator.io

import upickle.default.{ReadWriter, macroRW}

case class ConfigDto(
  lanes: Map[String, Seq[Seq[String]]],
  scheduler: String,
  lightDuration: Int,
)
object ConfigDto {
  implicit val rw: ReadWriter[ConfigDto] = macroRW
}