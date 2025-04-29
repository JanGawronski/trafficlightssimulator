package io.github.jangawronski.trafficlightsimulator.io

import io.github.jangawronski.trafficlightsimulator.model._
import cats.syntax.either._
import cats.syntax.traverse._
import cats.instances.list._
import cats.instances.either._

sealed trait ConfigError { def msg: String }
case class UnknownConfigRoad(name: String) extends ConfigError {
  def msg = s"Unknown road '$name'"
}
case class UnknownMovement(name: String) extends ConfigError {
  def msg = s"Unknown movement '$name'"
}
case class ConflictingMovements(road: Road, lane1: LaneConfig, lane2: LaneConfig) extends ConfigError {
  def msg = s"Road $road, phase group ${lane1.movements} conflicts with phase group ${lane2.movements}"
}

case class EmptyLane(road: Road) extends ConfigError {
  def msg = s"Road $road has an empty lane"
}

case class MissingRoads(
  missing: Set[Road],
) extends ConfigError {
  def msg = if (missing.nonEmpty) s"Missing roads: ${missing.mkString(", ")}" else ""
}

object ConfigParser {
  val requiredRoads: Set[Road] = Set(Road.North, Road.South, Road.East, Road.West)

  def toDomain(dto: IntersectionConfigDto): Either[ConfigError, IntersectionConfig] = {
    val roadGroups: Either[ConfigError, Map[Road, Seq[LaneConfig]]] =
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
                else Right(LaneConfig(index, list.toSet))
            }
          }
        } yield road -> lanes
      }.map(_.toMap)

    val validated: Either[ConfigError, Map[Road, Seq[LaneConfig]]] =
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

    validated.map { roadMap =>
        IntersectionConfig(roadMap)
    }
  }
}

