package io.github.jangawronski.trafficlightsimulator.model

import munit.FunSuite

class RoadTest extends FunSuite {

  test("straight should return the opposite road") {
    assertEquals(Road.North.straight, Road.South)
    assertEquals(Road.South.straight, Road.North)
    assertEquals(Road.East.straight, Road.West)
    assertEquals(Road.West.straight, Road.East)
  }

  test("left should return the correct left turn") {
    assertEquals(Road.North.left, Road.East)
    assertEquals(Road.East.left, Road.South)
    assertEquals(Road.South.left, Road.West)
    assertEquals(Road.West.left, Road.North)
  }

  test("right should return the correct right turn") {
    assertEquals(Road.North.right, Road.West)
    assertEquals(Road.West.right, Road.South)
    assertEquals(Road.South.right, Road.East)
    assertEquals(Road.East.right, Road.North)
  }

  test("fromString should parse valid names (case‐insensitive, trimmed)") {
    assertEquals(Road.fromString("north"), Some(Road.North))
    assertEquals(Road.fromString("  SoUtH  "), Some(Road.South))
    assertEquals(Road.fromString("EAST"), Some(Road.East))
    assertEquals(Road.fromString("west"), Some(Road.West))
  }

  test("fromString should return None on invalid names") {
    assertEquals(Road.fromString("northeast"), None)
    assertEquals(Road.fromString(""), None)
    assertEquals(Road.fromString(" up "), None)
  }

  test("navigate should delegate to straight/left/right") {
    assertEquals(Road.North.navigate(MovementType.Straight), Road.South)
    assertEquals(Road.North.navigate(MovementType.Left), Road.East)
    assertEquals(Road.North.navigate(MovementType.Right), Road.West)
    val r = Road.East.navigate(MovementType.Right)
    assertEquals(r, Road.North)
  }
}
