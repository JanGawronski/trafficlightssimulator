package io.github.jangawronski.trafficlightsimulator.model

/**
  * Represents a road in the intersection.
  */
enum Road:
    case North, East, South, West

    def straight: Road = this match
        case North => South
        case South => North
        case East  => West
        case West  => East

    def right: Road = this match
        case North => West
        case South => East
        case East  => North
        case West  => South

    def left: Road = this match
        case North => East
        case South => West
        case East  => South
        case West  => North

    def navigate(movementType: MovementType): Road = movementType match
        case MovementType.Straight => straight
        case MovementType.Left     => left
        case MovementType.Right => right
    
object Road:
    def fromString(str: String): Option[Road] = str.toLowerCase.strip match
        case "north" => Some(Road.North)
        case "east"  => Some(Road.East)
        case "south" => Some(Road.South)
        case "west"  => Some(Road.West)
        case _       => None

/**
  * Represents the type of movement allowed from one road to another.
  */
enum MovementType:
    case Straight, Left, Right
    
object MovementType:
    /**
      * Checks if two movement types conflict with each other.
      * It's used to determine if two lanes exist in the same road in next to each other.
      */
    def conflicts(leftLane: MovementType, rightLane: MovementType): Boolean = 
        (leftLane, rightLane) match
            case (Straight, Left) => true
            case (Right, Straight) => true
            case (Right, Left) => true
            case _ => false

    /**
      * Derives the movement type from the start and end road.
      * Throws an exception if no valid movement type is found.
      */
    def derive(start: Road, end: Road): MovementType =
        values
        .find(mv => start.navigate(mv) == end)
        .getOrElse(
            throw new IllegalArgumentException(
            s"Cannot go from $start to $end with any MovementType"
            )
        )

    def fromString(str: String): Option[MovementType] = str.toLowerCase.strip match
        case "straight" => Some(MovementType.Straight)
        case "left" => Some(MovementType.Left)
        case "right" => Some(MovementType.Right)
        case _ => None