package io.github.jangawronski.trafficlightsimulator.model

case class PhaseGroup(startRoad: Road, endRoads: Set[Road])