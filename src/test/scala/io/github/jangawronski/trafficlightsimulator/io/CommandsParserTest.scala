package io.github.jangawronski.trafficlightsimulator.io

import munit.FunSuite
import io.github.jangawronski.trafficlightsimulator.model._
import upickle.default._

class CommandsParserSpec extends FunSuite {

  test("valid addVehicle and step commands") {
    val json =
      """
      { "commands": [
          { "type":"addVehicle", "vehicleId":"v1", "startRoad":"north", "endRoad":"south" },
          { "type":"step" }
      ] }
      """
    val dto = read[CommandsDto](json)
    val result = CommandsParser.toDomain(dto)
    assertEquals(result, Right(List(
      AddVehicleCmd("v1", Road.North, Road.South),
      StepCmd
    )))
  }

  test("unknown road in addVehicle yields error") {
    val json =
      """
      { "commands": [
          { "type":"addVehicle", "vehicleId":"v2", "startRoad":"up", "endRoad":"down" }
      ] }
      """
    val dto = read[CommandsDto](json)
    val result = CommandsParser.toDomain(dto)
    assert(result.isLeft)
    val err = result.left.get
    assert(err.isInstanceOf[UnknownCommandRoad])
    assert(err.msg.contains("up"))
  }
}
