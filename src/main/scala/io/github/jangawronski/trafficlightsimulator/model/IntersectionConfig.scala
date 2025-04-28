package io.github.jangawronski.trafficlightsimulator.model

case class PhaseGroup(
  road: Road,
  movements: Set[MovementType]
)

case class IntersectionConfig(
  roads: Map[Road, Int],
  supportedPhaseGroups: Set[PhaseGroup]
)