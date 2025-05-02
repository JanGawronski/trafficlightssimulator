package io.github.jangawronski.trafficlightsimulator.io

import munit.FunSuite
import upickle.default.read
import io.github.jangawronski.trafficlightsimulator.model._
import io.github.jangawronski.trafficlightsimulator.simulation.schedulers._

class ConfigParserTest extends FunSuite {

  private def parse(json: String) =
    ConfigParser.toDomain(read[ConfigDto](json))

  test("valid config with all 4 roads, one straight lane each") {
    val json =
      """
      { "lanes": {
          "north":[["straight"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        },
        "scheduler": "vehicleCount",
        "lightDuration": 3
      }
      """
    val expectedIntersection = IntersectionConfig(
        Map(
          Road.North -> Seq(Lane(0, Road.North, Set(MovementType.Straight))),
          Road.South -> Seq(Lane(0, Road.South, Set(MovementType.Straight))),
          Road.East  -> Seq(Lane(0, Road.East, Set(MovementType.Straight))),
          Road.West  -> Seq(Lane(0, Road.West, Set(MovementType.Straight)))
        ),
    )

    val (config, scheduler, lightDuration) = parse(json).getOrElse(throw new Exception("Should not fail"))
    assertEquals(config, expectedIntersection)
    assert(scheduler.isInstanceOf[VehicleCountScheduler])
    assertEquals(lightDuration, 3)
  }

  test("missing one road should fail") {
    val json =
      """
      { "lanes": {
          "north":[["straight"]],
          "south":[["straight"]],
          "east":[["straight"]]
        },
        "scheduler": "vehicleCount",
        "lightDuration": 3
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
      { "lanes": {
          "north":[["straight"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]],
          "northeast":[["straight"]]
        },
        "scheduler": "vehicleCount",
        "lightDuration": 3
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
      { "lanes": {
          "north":[["fly"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        },
        "scheduler": "vehicleCount",
        "lightDuration": 3
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
      { "lanes": {
          "north":[[]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        },
        "scheduler": "vehicleCount",
        "lightDuration": 3
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
      { "lanes": {
          "north":[["right"],["left"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        },
        "scheduler": "vehicleCount",
        "lightDuration": 3
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

  test("correct config with non-existing scheduler") {
    val json =
      """
      { "lanes": {
          "north":[["straight"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        },
        "scheduler": "non-existing",
        "lightDuration": 3
      }
      """
    val result = parse(json)
    assert(result.isLeft)
    val err = result.left.get
    assert(err.isInstanceOf[UnknownConfigScheduler])
    assert(err.msg.contains("non-existing"))
  }
  test("correct config with non-positive light duration") {
    val json =
      """
      { "lanes": {
          "north":[["straight"]],
          "south":[["straight"]],
          "east":[["straight"]],
          "west":[["straight"]]
        },
        "scheduler": "vehicleCount",
        "lightDuration": -1
      }
      """
    val result = parse(json)
    assert(result.isLeft)
    val err = result.left.get
    assert(err.isInstanceOf[NonPositiveLightDuration])
    assert(err.msg.contains("-1"))
  }
}
