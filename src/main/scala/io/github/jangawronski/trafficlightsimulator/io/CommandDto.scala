package io.github.jangawronski.trafficlightsimulator.io

import upickle.default.{ReadWriter, macroRW}

import upickle.default._
import upickle.implicits.key

@key("type")
sealed trait CommandDto
object CommandDto {
  implicit val rw: ReadWriter[CommandDto] = macroRW
}

@key("addVehicle")
case class AddVehicleDto(
  vehicleId: String,
  startRoad: String,
  endRoad:   String
) extends CommandDto
object AddVehicleDto { implicit val rw: ReadWriter[AddVehicleDto] = macroRW }

@key("step")  
case class StepDto() extends CommandDto
object StepDto { implicit val rw: ReadWriter[StepDto] = macroRW }

@key("commands")
case class CommandsDto(commands: Seq[CommandDto])
object CommandsDto { implicit val rw: ReadWriter[CommandsDto] = macroRW }
