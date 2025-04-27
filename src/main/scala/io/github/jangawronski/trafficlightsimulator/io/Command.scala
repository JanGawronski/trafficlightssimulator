package io.github.jangawronski.trafficlightsimulator.io

import upickle.default.{ReadWriter, macroRW}

sealed trait CommandDto
object CommandDto {
  implicit val rw: ReadWriter[CommandDto] = macroRW
}

case class AddVehicleDto(
  `type`: String,
  vehicleId: String,
  startRoad: String,
  endRoad:   String
) extends CommandDto
object AddVehicleDto { implicit val rw: ReadWriter[AddVehicleDto] = macroRW }

case class StepDto(`type`: String = "step") extends CommandDto
object StepDto { implicit val rw: ReadWriter[StepDto] = macroRW }

case class CommandsDto(commands: Seq[CommandDto])
object CommandsDto { implicit val rw: ReadWriter[CommandsDto] = macroRW }
