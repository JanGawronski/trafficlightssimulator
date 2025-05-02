package io.github.jangawronski.trafficlightsimulator.simulation

import io.github.jangawronski.trafficlightsimulator.model._

/**
  * PhasePlan shouldn't allow for collisions, such as west and north both allowing
  * straight movements.
  * It can allow for collisions, containing of left turns, such as north allowing 
  * left turn and south allowing straight. In these cases, vehicle wanting to
  * turn left won't drive.
  * Situation such as north allowing for left turn and south allowing for
  * right turn and west allowing for straight is allowed and all three
  * vehicles will drive.
  * @param greens  the lanes that are allowed to turn green
  */
case class PhasePlan(greens: Map[Road, Set[Lane]])

/**
  * StepStatus contains the vehicles that left the intersection during step.
  * This is used for logging.
  * @param leftVehicles  the vehicles that left the intersection
  */
case class StepStatus(leftVehicles: Seq[String])

/**
  * Scheduler is responsible for deciding which lanes should be green at any given time.
  * It takes into account the history of PhasePlans, the current state of the intersection,
  * and the vehicles in the intersection.
  */
trait Scheduler:

  /**  
   * @param history        all prior PhasePlans (for fairness, aging, etc.)  
   * @param intersection   the current state of the intersection
   * @param vehicles       the current state of the vehicles in the intersection
   * @return               the next set of PhaseGroups to turn green  
   */
  def nextPlan(
    history:      Seq[PhasePlan],
    intersection: Map[Road, Set[Lane]],
    vehicles:     Map[Lane, Seq[Vehicle]]
  ): PhasePlan
