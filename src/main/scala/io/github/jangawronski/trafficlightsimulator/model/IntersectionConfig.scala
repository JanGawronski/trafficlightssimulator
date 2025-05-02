package io.github.jangawronski.trafficlightsimulator.model

case class IntersectionConfig(
  lanes: Map[Road, Seq[Lane]],
) {
  /**
    * Checks if two lanes from opposite roads conflict with each other.
    * The only conflict is if one lane is going straight and the other is only able to turn left.
    */
  private def oppositeLanesConflict(lane1: Lane, lane2: Lane): Boolean = {
    val movements1 = lane1.movements
    val movements2 = lane2.movements
    movements1 == Set(MovementType.Left) && movements2.contains(MovementType.Straight)
    || movements2 == Set(MovementType.Left) && movements1.contains(MovementType.Straight)
  }

  /**
    * Checks if two lanes conflict with each other.
    * Meant for lanes from roads such as turning from road1 to road2 is a right turn.
    */
  private def turnRightConflict(lane1: Lane, lane2: Lane): Boolean = {
    val movements1 = lane1.movements
    val movements2 = lane2.movements
    movements1.contains(MovementType.Straight) && (movements2.contains(MovementType.Straight) || movements2.contains(MovementType.Left))
    || movements1.contains(MovementType.Left) && movements2.contains(MovementType.Left)
  }

  /**
    * Checks if two lanes conflict with each other.
    */
  def laneConflicts(lane1: Lane, lane2: Lane): Boolean = {
    if (lane1.road == lane2.road)
      return false
    MovementType.derive(lane1.road, lane2.road) match {
      case MovementType.Straight => {
        oppositeLanesConflict(lane1, lane2)
      }
      case MovementType.Right => {
        turnRightConflict(lane1, lane2)
      }
      case MovementType.Left => {
        turnRightConflict(lane2, lane1)
      }
    }
  }
}