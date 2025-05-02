package io.github.jangawronski.trafficlightsimulator.model

/**
 * Represents a vehicle in the intersection.
 * Each vehicle has an ID, a starting road, an ending road, and an arrival time.
 * The movement type is derived from the start and end roads.
 */
case class Vehicle(id: String, startRoad: Road, endRoad: Road, arrivalTime: Int):
    def movement: MovementType = MovementType.derive(startRoad, endRoad)