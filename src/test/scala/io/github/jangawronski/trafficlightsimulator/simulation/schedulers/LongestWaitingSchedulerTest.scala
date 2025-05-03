package io.github.jangawronski.trafficlightsimulator.simulation.schedulers

import munit.FunSuite
import scala.collection.mutable
import io.github.jangawronski.trafficlightsimulator.model._
import io.github.jangawronski.trafficlightsimulator.simulation.PhasePlan

class LongestWaitingSchedulerTest extends FunSuite {

  private val scheduler = new LongestWaitingScheduler()
  private def mkLane(id: Int, road: Road, moves: MovementType*): Lane =
    Lane(id, road, moves.toSet)

  private def mkConfig(lanes: Lane*): IntersectionConfig =
    IntersectionConfig(
      lanes.groupBy(_.road).view.mapValues(_.toSeq).toMap
    )

  private def mkVehicle(id: String, start: Road, end: Road, arrival: Int): Vehicle =
    Vehicle(id, start, end, arrival)

  test("opposite roads both straight: both lanes go green, oldest first") {
    val nLane = mkLane(0, Road.North, MovementType.Straight)
    val sLane = mkLane(1, Road.South, MovementType.Straight)

    val config = mkConfig(nLane, sLane)

    val vehicles = Map(
      nLane -> Seq(mkVehicle("v1", Road.North, Road.South, 1)),
      sLane -> Seq(mkVehicle("v2", Road.South, Road.North, 5))
    )

    val plan = scheduler.nextPlan(Nil, config, vehicles)
    assertEquals(plan.greens.keySet, Set(Road.North, Road.South))
  }

  test("perpendicular straight‐straight: only the oldest lane goes") {
    val nLane = mkLane(0, Road.North, MovementType.Straight)
    val wLane = mkLane(1, Road.West,  MovementType.Straight)

    val config = mkConfig(nLane, wLane)

    val vehicles = Map(
      nLane -> Seq(mkVehicle("a", Road.North, Road.South, 2)),
      wLane -> Seq(mkVehicle("b", Road.West,  Road.East,  10))
    )

    val plan = scheduler.nextPlan(Nil, config, vehicles)
    assertEquals(plan.greens.keySet, Set(Road.North))
    assertEquals(plan.greens(Road.North), Set(nLane))
  }

  test("left‐only vs straight on opposite: only oldest left‐only goes") {
    val nLane = mkLane(0, Road.North, MovementType.Left)
    val sLane = mkLane(1, Road.South, MovementType.Straight)

    val config = mkConfig(nLane, sLane)

    val vehicles = Map(
      nLane -> Seq(mkVehicle("L", Road.North, Road.West, 0)),
      sLane -> Seq(mkVehicle("S", Road.South, Road.North, 100))
    )

    val plan = scheduler.nextPlan(Nil, config, vehicles)
    assertEquals(plan.greens.keySet, Set(Road.North))
  }

  test("lanes with no vehicles are treated as youngest (never chosen if any vehicle exists)") {
    val nLane = mkLane(0, Road.North, MovementType.Straight)
    val eLane = mkLane(1, Road.East,  MovementType.Straight)

    val config = mkConfig(nLane, eLane)

    val vehicles = Map(
      nLane -> Seq(mkVehicle("X", Road.North, Road.South, 5))
    )

    val plan = scheduler.nextPlan(Nil, config, vehicles)
    assertEquals(plan.greens.keySet, Set(Road.North))
  }

  test("opposite straight‐straight with equal arrivalTime → both go green") {
    val nLane = mkLane(0, Road.North, MovementType.Straight)
    val sLane = mkLane(1, Road.South, MovementType.Straight)

    val config = mkConfig(nLane, sLane)

    val vehicles = Map(
      nLane -> Seq(mkVehicle("A", Road.North, Road.South, 7)),
      sLane -> Seq(mkVehicle("B", Road.South, Road.North, 7))
    )

    val plan = scheduler.nextPlan(Nil, config, vehicles)
    assertEquals(plan.greens.keySet, Set(Road.North, Road.South))
  }
}
