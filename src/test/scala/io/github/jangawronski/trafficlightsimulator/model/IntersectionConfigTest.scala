package io.github.jangawronski.trafficlightsimulator.model

import munit.FunSuite

class IntersectionConfigTest extends FunSuite {
  private def config: IntersectionConfig =
    IntersectionConfig(lanes = Map.empty)

  private def lane(id: Int, road: Road, moves: MovementType*): Lane =
    Lane(id, road, moves.toSet)

  test("opposite: left-only vs straight should conflict") {
    val l1 = lane(0, Road.North, MovementType.Left)
    val l2 = lane(0, Road.South, MovementType.Straight)
    assert(config.laneConflicts(l1, l2))
    assert(config.laneConflicts(l2, l1))
  }

  test("opposite: straight vs straight should not conflict") {
    val l1 = lane(0, Road.North, MovementType.Straight)
    val l2 = lane(0, Road.South, MovementType.Straight)
    assert(!config.laneConflicts(l1, l2))
    assert(!config.laneConflicts(l2, l1))
  }

  test("perpendicular: straight vs straight should conflict") {
    val l1 = lane(0, Road.North, MovementType.Straight)
    val l2 = lane(0, Road.West,  MovementType.Straight)
    assert(config.laneConflicts(l1, l2))
    assert(config.laneConflicts(l2, l1))
  }

  test("perpendicular: left-only vs straight should not conflict") {
    val l1 = lane(0, Road.North, MovementType.Left)
    val l2 = lane(0, Road.West,  MovementType.Straight)
    assert(!config.laneConflicts(l1, l2))
    assert(!config.laneConflicts(l2, l1))
  }

  test("perpendicular: left-only vs left-only should conflict") {
    val l1 = lane(0, Road.North, MovementType.Left)
    val l2 = lane(0, Road.East,  MovementType.Left)
    assert(config.laneConflicts(l1, l2))
    assert(config.laneConflicts(l2, l1))
  }

  test("non-adjacent but non-opposite should follow turnRightConflict") {
    val eastStraight  = lane(0, Road.East, MovementType.Straight)
    val southStraight = lane(0, Road.South, MovementType.Straight)
    assert(config.laneConflicts(eastStraight, southStraight))
    assert(config.laneConflicts(southStraight, eastStraight))
  }

  test("lanes must belong to their respective roads") {
    val invalidLane = lane(0, Road.South, MovementType.Straight)
    
    val exception = intercept[IllegalArgumentException] {
      IntersectionConfig(lanes = Map(Road.North -> List(invalidLane)))
    }

    assert(exception.getMessage.contains("All lanes must belong to their respective roads."))
  }
}
