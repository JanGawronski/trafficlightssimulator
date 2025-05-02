package io.github.jangawronski.trafficlightsimulator.model

import scala.collection.mutable

/**
 * Represents a lane in the intersection.
 * Each lane is associated with a road and can have multiple movement types.
 * The movement types indicate the allowed movements from this lane.
 */
case class Lane(
  id: Int,
  road: Road,
  movements: Set[MovementType],
)
