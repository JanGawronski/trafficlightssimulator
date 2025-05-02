package io.github.jangawronski.trafficlightsimulator.simulation.schedulers
import io.github.jangawronski.trafficlightsimulator.model._
import io.github.jangawronski.trafficlightsimulator.simulation.{PhasePlan, Scheduler}

/**
 * This scheduler prioritizes lanes where first vehicle waited longest.
 * It will greedily select lanes.
 */
class LongestWaitingScheduler extends Scheduler:
  /**
   * This scheduler prioritizes lanes where first vehicle waited longest.
   * It will greedily select lanes.
   * @param history              all prior PhasePlans
   * @param intersectionConfig   the configuration of the intersection, including lanes and roads
   * @param vehicles             the current state of the vehicles in the intersection
   * @return                     the next PhasePlan
   */
  def nextPlan(
    history:            Seq[PhasePlan],
    intersectionConfig: IntersectionConfig,
    vehicles:           Map[Lane, Seq[Vehicle]]
  ): PhasePlan = {
    val lanes = intersectionConfig.lanes.values.flatten.toSeq
    val sortedLanes = lanes.sortBy { lane =>
        vehicles.get(lane) match {
            case Some(vs) if vs.nonEmpty => vs.head.arrivalTime
            case _ => Int.MaxValue
        }
    }

    def takeGreedy(lanes: Seq[Lane], acc: Seq[Lane]): Seq[Lane] = {
      if (lanes.isEmpty) acc
      else {
        val lane = lanes.head
        val newLanes = lanes.tail.filterNot { l =>
          intersectionConfig.laneConflicts(lane, l)
        }
        takeGreedy(newLanes, acc :+ lane)
      }
    }
    val greens = takeGreedy(sortedLanes, Seq.empty)
    PhasePlan(greens.groupBy(_.road).map { case (road, lanes) => road -> lanes.toSet })
  }