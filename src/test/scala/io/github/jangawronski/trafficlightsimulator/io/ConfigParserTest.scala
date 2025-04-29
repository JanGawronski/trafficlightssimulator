package io.github.jangawronski.trafficlightsimulator.io

import munit.FunSuite
import upickle.default.read
import io.github.jangawronski.trafficlightsimulator.model._

class ConfigParserSpec extends FunSuite {

  private def parse(json: String) =
    ConfigParser.toDomain(read[IntersectionConfigDto](json))

  test("valid config with all 4 roads, one straight lane each") {
    val json =
      """
      { "phaseGroups": {
          "north":[["straight"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        }
      }
      """
    val expected = IntersectionConfig(
      roads = Map(
        Road.North -> 1,
        Road.South -> 1,
        Road.East  -> 1,
        Road.West  -> 1
      ),
      supportedPhaseGroups = Set(
        PhaseGroup(Road.North, Set(MovementType.Straight)),
        PhaseGroup(Road.South, Set(MovementType.Straight)),
        PhaseGroup(Road.East,  Set(MovementType.Straight)),
        PhaseGroup(Road.West,  Set(MovementType.Straight))
      )
    )

    assertEquals(parse(json), Right(expected))
  }

  test("missing one road should fail") {
    val json =
      """
      { "phaseGroups": {
          "north":[["straight"]],
          "south":[["straight"]],
          "east":[["straight"]]
        }
      }
      """
    val result = parse(json)
    assert(result.isLeft)
    val err = result.left.get
    assert(err.isInstanceOf[MissingRoads])
    assert(err.msg.contains("Missing roads: West"))
  }

  test("extra road should fail") {
    val json =
      """
      { "phaseGroups": {
          "north":[["straight"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]],
          "northeast":[["straight"]]
        }
      }
      """
    val result = parse(json)
    assert(result.isLeft)
    val err = result.left.get
    assert(err.isInstanceOf[UnknownConfigRoad])
    assert(err.msg.contains("northeast"))
  }

  test("unknown movement name should fail") {
    val json =
      """
      { "phaseGroups": {
          "north":[["fly"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        }
      }
      """
    val result = parse(json)
    assert(result.isLeft)
    val err = result.left.get
    assert(err.isInstanceOf[UnknownMovement])
    assert(err.msg.contains("fly"))
  }

  test("empty lane list should fail") {
    val json =
      """
      { "phaseGroups": {
          "north":[[]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        }
      }
      """
    val result = parse(json)
    assert(result.isLeft)
    val err = result.left.get
    assert(err.isInstanceOf[EmptyLane])
    assert(err.msg.contains("North"))
  }

  test("adjacent lane conflict (right vs left) should fail") {
    val json =
      """
      { "phaseGroups": {
          "north":[["right"],["left"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        }
      }
      """
    val result = parse(json)
    assert(result.isLeft)
    val err = result.left.get
    assert(err.isInstanceOf[ConflictingMovements])
    assert(err.msg.contains("North"))
    assert(err.msg.contains("Right"))
    assert(err.msg.contains("Left"))
  }
}
