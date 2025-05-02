package io.github.jangawronski.trafficlightsimulator.model

import scala.collection.mutable

case class Lane(
  id: Int,
  road: Road,
  movements: Set[MovementType],
)
