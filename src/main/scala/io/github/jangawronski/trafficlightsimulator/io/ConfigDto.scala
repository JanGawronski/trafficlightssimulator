package io.github.jangawronski.trafficlightsimulator.io

import upickle.default.{ReadWriter, macroRW}


case class IntersectionConfigDto(
  phaseGroups: Map[String, Seq[Seq[String]]]
)
object IntersectionConfigDto {
  implicit val rw: ReadWriter[IntersectionConfigDto] = macroRW
}
