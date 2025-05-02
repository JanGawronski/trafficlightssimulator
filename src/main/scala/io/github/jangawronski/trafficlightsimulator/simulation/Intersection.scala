package io.github.jangawronski.trafficlightsimulator.simulation

import io.github.jangawronski.trafficlightsimulator.model._
import scala.collection.mutable.Queue

class Intersection(config: IntersectionConfig) {
  private val roads: Map[Road, Seq[Lane]] = config.lanes
  private val queues: Map[Lane, Queue[Vehicle]] = 
    roads.values.flatten.map { lane => lane -> Queue.empty }.toMap


  def enqueue(v: Vehicle): Unit = {
    val candidateLanes = roads(v.startRoad).filter(_.movements.contains(v.movement))
    require(candidateLanes.nonEmpty, s"No lane on ${v.startRoad} for ${v.movement}")

    val best = candidateLanes.minBy { lane =>
      (queues(lane).size, lane.movements.size)
    }
    queues(best).enqueue(v)
  }

  def queueLengths: Map[Road, Seq[Int]] =
    roads.view.mapValues(_.map(_.numberOfVehicles)).toMap

}
