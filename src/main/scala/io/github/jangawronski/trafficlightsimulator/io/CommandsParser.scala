package io.github.jangawronski.trafficlightsimulator.io

import io.github.jangawronski.trafficlightsimulator.model._

import cats.implicits._


sealed trait ParseError { def message: String }
case class UnknownCommandRoad(raw: String) extends ParseError { def message = s"Unknown road '$raw'" }
case class UnknownCommandType(raw: String) extends ParseError { def message = s"Unknown command type '$raw'" }

sealed trait Command
case class AddVehicleCmd(
  vehicleId: String,
  start: Road,
  end: Road
) extends Command

case object StepCmd extends Command

object CommandsParser {
  def toDomain(dto: CommandsDto): Either[ParseError, List[Command]] =
    dto.commands.toList.traverse {
      case a: AddVehicleDto =>
        for {
          start <- Road.fromString(a.startRoad)
                       .toRight(UnknownCommandRoad(a.startRoad))
          end   <- Road.fromString(a.endRoad)
                       .toRight(UnknownCommandRoad(a.endRoad))
        } yield AddVehicleCmd(a.vehicleId, start, end)
      case StepDto(_)      => Right(StepCmd)
    }
}
