package io.github.jangawronski.trafficlightsimulator.model

case class Vehicle(id: String, startRoad: Road, endRoad: Road, arrivalTime: Int):
    def movement: MovementType = MovementType.derive(startRoad, endRoad)