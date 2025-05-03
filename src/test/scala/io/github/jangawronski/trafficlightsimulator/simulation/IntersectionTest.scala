package io.github.jangawronski.trafficlightsimulator.simulation

import munit.FunSuite
import io.github.jangawronski.trafficlightsimulator.model._

import scala.collection.mutable

class IntersectionTest extends FunSuite {

  val northLaneStraight = Lane(0, Road.North, Set(MovementType.Straight))
  val northLaneLeft = Lane(0, Road.North, Set(MovementType.Left))
  val eastLaneStraight = Lane(0, Road.East, Set(MovementType.Straight))
  val config = IntersectionConfig(
    Map(
      Road.North -> Seq(northLaneStraight, northLaneLeft),
      Road.East -> Seq(eastLaneStraight),
      Road.South -> Seq(),
      Road.West -> Seq()
    )
  )

  test("enqueue places vehicle in best matching lane") {
    val intersection = new Intersection(config)
    val v1 = Vehicle("v1", Road.North, Road.South, 0)
    intersection.enqueue(v1)
    val laneVehicles = intersection.laneVehicles
    assertEquals(laneVehicles(northLaneStraight), Seq(v1))
    assertEquals(laneVehicles(northLaneLeft), Seq())
  }

  test("enqueue throws for unsupported movement") {
    val intersection = new Intersection(config)
    val vInvalid = Vehicle("vX", Road.East, Road.North, 0)
    intercept[IllegalArgumentException] {
      intersection.enqueue(vInvalid)
    }
  }

  test("applyPlan dequeues valid vehicle") {
    val intersection = new Intersection(config)
    val v2 = Vehicle("v2", Road.North, Road.South, 1)
    intersection.enqueue(v2)

    val plan = PhasePlan(Map(
      Road.North -> Set(northLaneStraight)
    ))
    val status = intersection.applyPlan(plan)

    assert(status.leftVehicles.contains("v2"))
    assert(intersection.laneVehicles(northLaneStraight).isEmpty)
  }

  test("applyPlan does not dequeue if blocked by opposite straight") {
    val intersection = new Intersection(config)
    val v3 = Vehicle("v3", Road.North, Road.East, 2)
    val v4 = Vehicle("v4", Road.South, Road.North, 3)
    val southLaneStraight = Lane(0, Road.South, Set(MovementType.Straight))
    val northLeftLane = Lane(0, Road.North, Set(MovementType.Left))
    val extendedConfig = IntersectionConfig(
      Map(
        Road.North -> Seq(northLeftLane, northLaneStraight),
        Road.South -> Seq(southLaneStraight)
      )
    )
    val intersection2 = new Intersection(extendedConfig)

    intersection2.enqueue(v3)
    intersection2.enqueue(v4)

    val plan = PhasePlan(Map(
      Road.North -> Set(northLeftLane),
      Road.South -> Set(southLaneStraight)
    ))

    val status = intersection2.applyPlan(plan)

    assert(!status.leftVehicles.contains("v3"))
    assert(status.leftVehicles.contains("v4"))
  }
}
