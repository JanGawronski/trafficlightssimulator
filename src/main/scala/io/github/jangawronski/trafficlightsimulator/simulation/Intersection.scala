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
        if (queue.isEmpty || (goesStaight(road.straight) && queue.head.movement == MovementType.Left)) {
          Seq.empty
        }
        else {
          Seq(queue.dequeue.id)
        }
      }
    }.toSeq

    StepStatus(leftVehicles)
  }


  def laneVehicles: Map[Lane, Seq[Vehicle]] = {
    queues.map { case (lane, queue) => lane -> queue.toSeq }
  }

}
