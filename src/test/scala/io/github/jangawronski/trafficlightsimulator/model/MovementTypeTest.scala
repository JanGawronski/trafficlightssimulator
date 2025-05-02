package io.github.jangawronski.trafficlightsimulator.model

import munit.FunSuite

class MovementTypeTest extends FunSuite {

  test("fromString should parse valid names") {
    assertEquals(MovementType.fromString("straight"), Some(MovementType.Straight))
    assertEquals(MovementType.fromString("  Left "), Some(MovementType.Left))
    assertEquals(MovementType.fromString("RIGHT"), Some(MovementType.Right))
  }

  test("fromString should return None on invalid names") {
    assertEquals(MovementType.fromString("go"), None)
    assertEquals(MovementType.fromString(""), None)
    assertEquals(MovementType.fromString(" turn "),None)
  }

  test("conflicts should detect only the defined conflicting pairs") {
    assert(MovementType.conflicts(MovementType.Straight, MovementType.Left))
    assert(MovementType.conflicts(MovementType.Right, MovementType.Straight))
    assert(MovementType.conflicts(MovementType.Right, MovementType.Left))

    assert(!MovementType.conflicts(MovementType.Left, MovementType.Straight))
    assert(!MovementType.conflicts(MovementType.Left, MovementType.Right))
    assert(!MovementType.conflicts(MovementType.Straight, MovementType.Right))
    assert(!MovementType.conflicts(MovementType.Straight, MovementType.Straight))
  }

  test("derive should find the correct movement for each pair") {
    assertEquals(MovementType.derive(Road.North, Road.South), MovementType.Straight)
    assertEquals(MovementType.derive(Road.North, Road.East),  MovementType.Left)
    assertEquals(MovementType.derive(Road.North, Road.West),  MovementType.Right)

    for mv <- MovementType.values do
      val to = Road.North.navigate(mv)
      assertEquals(MovementType.derive(Road.North, to), mv)
  }

  test("derive should throw on invalid combinations") {
    val ex = interceptMessage[IllegalArgumentException]("Cannot go from North to North with any MovementType") {
      MovementType.derive(Road.North, Road.North)
    }
  }
}
