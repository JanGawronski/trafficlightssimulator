package io.github.jangawronski.trafficlightsimulator.simulation

import io.github.jangawronski.trafficlightsimulator.model._
import scala.collection.mutable.Queue

/**
  * Represents an intersection with multiple roads and lanes.
  * Each lane can have multiple vehicles waiting to enter the intersection.
  * The intersection can apply a plan to allow vehicles to leave based on the current phase.
  */
class Intersection(val config: IntersectionConfig) {
  private val roads: Map[Road, Seq[Lane]] = config.lanes
  private val queues: Map[Lane, Queue[Vehicle]] = 
    roads.values.flatten.map { lane => lane -> Queue.empty }.toMap

  /**
    * Enqueues a vehicle into the intersection.
    * The vehicle is added to the queue of the lane that matches its movement type.
    * @param vehicle The vehicle to enqueue.
    */
  def enqueue(vehicle: Vehicle): Unit = {
    val candidateLanes = roads(vehicle.startRoad).filter(_.movements.contains(vehicle.movement))
    require(candidateLanes.nonEmpty, s"No lane on ${vehicle.startRoad} for ${vehicle.movement}")

    val best = candidateLanes.minBy { lane =>
      (queues(lane).size, lane.movements.size)
    }
    queues(best).enqueue(vehicle)
  }

  /**
    * Applies the given plan to the intersection, removing vehicles from the queues.
    * @param plan The plan to apply.
    * @return A StepStatus containing the vehicles that left the intersection.
    */
  def applyPlan(plan: PhasePlan): StepStatus = {
    val goesStaight = plan.greens.map { case (road, lanes) =>
      road -> lanes.exists(lane => queues(lane).nonEmpty && queues(lane).head.movement == MovementType.Straight)
    }
    val leftVehicles = plan.greens.flatMap { case (road, lanes) =>
      lanes.flatMap { lane =>
        val queue = queues(lane)
        if (queue.isEmpty || (
          goesStaight.getOrElse(road.straight, false)
          && queue.head.movement == MovementType.Left)) {
          Seq.empty
        }
        else {
          Seq(queue.dequeue.id)
        }
      }
    }.toSeq

    StepStatus(leftVehicles)
  }

  /**
    * Returns the vehicles in the intersection, grouped by lane.
    * @return A map of lanes to their respective vehicles.
    */
  def laneVehicles: Map[Lane, Seq[Vehicle]] = {
    queues.map { case (lane, queue) => lane -> queue.toSeq }
  }

}
