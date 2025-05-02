package io.github.jangawronski.trafficlightsimulator.model

case class IntersectionConfig(
  lanes: Map[Road, Seq[Lane]],
)