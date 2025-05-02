package io.github.jangawronski.trafficlightsimulator.io

import upickle.default.{ReadWriter, macroRW}

case class StepStatusDto(leftVehicles: Seq[String])
object StepStatusDto {
  implicit val rw: ReadWriter[StepStatusDto] = macroRW
}


case class OutputDto(stepStatuses: Seq[StepStatusDto])
object OutputDto {
  implicit val rw: ReadWriter[OutputDto] = macroRW
}
