package io.github.jangawronski.trafficlightsimulator.simulation.schedulers

import munit.FunSuite
import scala.collection.mutable
import io.github.jangawronski.trafficlightsimulator.model._
import io.github.jangawronski.trafficlightsimulator.simulation.PhasePlan

class VehicleCountSchedulerTest extends FunSuite {

  private def mkLane(id: Int, road: Road, moves: MovementType*): Lane =
    Lane(id, road, moves.toSet)

  private def mkConfig(lanes: Lane*): IntersectionConfig =
    IntersectionConfig(
      lanes.groupBy(_.road).view.mapValues(_.toSeq).toMap
    )

  private val scheduler = new VehicleCountScheduler()

  test("when opposite roads have vehicles, both lanes turn green") {
    val nLane = mkLane(0, Road.North, MovementType.Straight)
    val sLane = mkLane(1, Road.South, MovementType.Straight)

    val config   = mkConfig(nLane, sLane)
    val vehicles = Map(
      nLane -> Seq.fill(3)(Vehicle("v", Road.North, Road.South, 0)),
      sLane -> Seq.fill(1)(Vehicle("w", Road.South, Road.North, 0))
    )

    val plan = scheduler.nextPlan(Nil, config, vehicles)
    assertEquals(plan.greens.keySet, Set(Road.North, Road.South))
    assertEquals(plan.greens(Road.North), Set(nLane))
    assertEquals(plan.greens(Road.South), Set(sLane))
  }

  test("when perpendicular straight‐straight conflict, only busier lane turns green") {
    val nLane = mkLane(0, Road.North, MovementType.Straight)
    val wLane = mkLane(1, Road.West,  MovementType.Straight)

    val config = mkConfig(nLane, wLane)
    val vehicles = Map(
      nLane -> Seq.fill(5)(Vehicle("a", Road.North, Road.South, 0)),
      wLane -> Seq.fill(2)(Vehicle("b", Road.West,  Road.East,  0))
    )

    val plan = scheduler.nextPlan(Nil, config, vehicles)
    assertEquals(plan.greens.keySet, Set(Road.North))
    assertEquals(plan.greens(Road.North), Set(nLane))
  }

  test("tie‐break: if queue lengths equal, both opposite lanes still turn green") {
    val nLane = mkLane(0, Road.North, MovementType.Straight)
    val sLane = mkLane(1, Road.South, MovementType.Straight)

    val config = mkConfig(nLane, sLane)
    val vehicles = Map(
      nLane -> Seq.fill(2)(Vehicle("a", Road.North, Road.South, 0)),
      sLane -> Seq.fill(2)(Vehicle("b", Road.South, Road.North, 0))
    )

    val plan = scheduler.nextPlan(Nil, config, vehicles)
    assertEquals(plan.greens.keySet, Set(Road.North, Road.South))
  }
}
