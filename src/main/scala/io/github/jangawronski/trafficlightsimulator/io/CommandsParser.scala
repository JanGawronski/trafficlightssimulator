package io.github.jangawronski.trafficlightsimulator.io

import io.github.jangawronski.trafficlightsimulator.model._

import cats.implicits._


sealed trait ParseError { def msg: String }
case class UnknownCommandRoad(raw: String) extends ParseError { def msg = s"Unknown road '$raw'" }
case class UnknownCommandType(raw: String) extends ParseError { def msg = s"Unknown command type '$raw'" }

sealed trait Command
case class AddVehicleCmd(
  vehicleId: String,
  start: Road,
  end: Road
) extends Command

case object StepCmd extends Command

object CommandsParser {
  def toDomain(dto: CommandsDto): Either[ParseError, Seq[Command]] =
    dto.commands.toList.traverse {
      case AddVehicleDto(vehicleId, startRoad, endRoad) =>
        for {
          start <- Road.fromString(startRoad)
                       .toRight(UnknownCommandRoad(startRoad))
          end   <- Road.fromString(endRoad)
                       .toRight(UnknownCommandRoad(endRoad))
        } yield AddVehicleCmd(vehicleId, start, end)
      case StepDto() => Right(StepCmd)
    }
}
