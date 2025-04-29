package io.github.jangawronski.trafficlightsimulator.model

import scala.collection.mutable

case class LaneConfig(
  id: Int,
  movements: Set[MovementType],
)

case class Lane(
  id: Int,
  movements: Set[MovementType],
  queue: mutable.Queue[Vehicle] = mutable.Queue.empty
) {
  def size: Int = queue.size
}
