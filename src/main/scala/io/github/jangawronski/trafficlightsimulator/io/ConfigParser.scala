package io.github.jangawronski.trafficlightsimulator.io

import cats.syntax.either._
import cats.syntax.traverse._
import cats.instances.list._
import cats.instances.either._

import io.github.jangawronski.trafficlightsimulator.model._
import io.github.jangawronski.trafficlightsimulator.simulation.Scheduler
import io.github.jangawronski.trafficlightsimulator.simulation.schedulers.VehicleCountScheduler

sealed trait ConfigError { def msg: String }
case class UnknownConfigRoad(name: String) extends ConfigError {
  def msg = s"Unknown road '$name'"
}
case class UnknownMovement(name: String) extends ConfigError {
  def msg = s"Unknown movement '$name'"
}
case class ConflictingMovements(road: Road, lane1: Lane, lane2: Lane) extends ConfigError {
  def msg = s"Road ${road}, movements ${lane1.movements} conflict with movements ${lane2.movements}"
}

case class EmptyLane(road: Road) extends ConfigError {
  def msg = s"Road $road has an empty lane"
}

case class MissingRoads(
  missing: Set[Road],
) extends ConfigError {
  def msg = if (missing.nonEmpty) s"Missing roads: ${missing.mkString(", ")}" else ""
}

case class UnknownConfigScheduler(name: String) extends ConfigError {
  def msg = s"Unknown scheduler '$name'"
}

case class NonPositiveLightDuration(value: Int) extends ConfigError {
  def msg = s"Light duration must be positive, but got $value"
}


object ConfigParser {
  val requiredRoads: Set[Road] = Set(Road.North, Road.South, Road.East, Road.West)

  def toDomain(dto: ConfigDto): Either[ConfigError, (IntersectionConfig, Scheduler, Int)] = {
    if (dto.lightDuration <= 0) {
      return Left(NonPositiveLightDuration(dto.lightDuration))
    }

    val scheduler = dto.scheduler match {
      case "vehicleCount" => Right(new VehicleCountScheduler())
      case "longestWaiting" => Right(new VehicleCountScheduler())
      case _ => Left(UnknownConfigScheduler(dto.scheduler))
    }

    val roadGroups: Either[ConfigError, Map[Road, Seq[Lane]]] =
      dto.lanes.toList.traverse { case (rawRoad, lanesLists) =>
        for {
          road <- Road.fromString(rawRoad).toRight(UnknownConfigRoad(rawRoad))
          lanes <- lanesLists.zipWithIndex.traverse { case (mvNames, index) =>
            val mvs: Either[ConfigError, List[MovementType]] = mvNames.toList.traverse { name =>
              MovementType.fromString(name)
                .toRight(UnknownMovement(name))
            }
              mvs.flatMap { list =>
                if (list.isEmpty) Left(EmptyLane(road))
                else Right(Lane(index, road, list.toSet))
            }
          }
        } yield road -> lanes
      }.map(_.toMap)

    val validated: Either[ConfigError, Map[Road, Seq[Lane]]] =
      roadGroups.flatMap { roadMap =>
        val presentRoads = roadMap.keySet

        if (presentRoads != requiredRoads) {
          val missing = requiredRoads -- presentRoads
          Left(MissingRoads(missing))
        } else {
          roadMap.toList.traverse { case (road, lanes) =>
            lanes.sliding(2).toList.traverse {
              case Seq(leftLane, rightLane) =>
                if (leftLane.movements.exists(l => rightLane.movements.exists(r => MovementType.conflicts(l, r))))
                  Left(ConflictingMovements(road, leftLane, rightLane))
                else
                  Right(())
              case _ => Right(())
            }
          }.map(_ => roadMap)
        }
      }
    
    validated.flatMap { roadMap =>
      scheduler.map { s =>
        (IntersectionConfig(roadMap), s, dto.lightDuration)
      }
    }
  }
}

