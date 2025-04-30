package io.github.jangawronski.trafficlightsimulator.simulation

import io.github.jangawronski.trafficlightsimulator.model._
import scala.collection.mutable.Queue

class Intersection(config: IntersectionConfig) {
  private val roads: Map[Road, Seq[Lane]] =
    config.lanes.map { case (road, lanes) =>
      road -> lanes.map { case LaneConfig(id, movements) =>
        Lane(id, movements, Queue.empty)
      }
    }

  def enqueue(v: Vehicle): Unit = {
    val candidateLanes = roads(v.startRoad).filter(_.movements.contains(v.movement))
    require(candidateLanes.nonEmpty, s"No lane on ${v.startRoad} for ${v.movement}")

    val best = candidateLanes.minBy { lane =>
      (lane.numberOfVehicles, lane.movements.size)
    }
    best.queue.enqueue(v)
  }

  def queueLengths: Map[Road, Seq[Int]] =
    roads.view.mapValues(_.map(_.numberOfVehicles)).toMap

}
