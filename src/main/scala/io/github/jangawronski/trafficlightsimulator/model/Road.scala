package io.github.jangawronski.trafficlightsimulator.model

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
        case MovementType.Right | MovementType.RightArrow => right
    
object Road:
    def fromString(str: String): Option[Road] = str.toLowerCase.strip match
        case "north" => Some(Road.North)
        case "east"  => Some(Road.East)
        case "south" => Some(Road.South)
        case "west"  => Some(Road.West)
        case _       => None


enum MovementType:
    case Straight, Left, Right, RightArrow
    
object MovementType:
    def conflicts(leftLane: MovementType, RightLane: MovementType): Boolean = 
        (leftLane, RightLane) match
            case (Straight, Left) => true
            case (Right, Straight) => true
            case (Right, Left) => true
            case _ => false


    def fromString(str: String): Option[MovementType] = str.toLowerCase.strip match
        case "straight" => Some(MovementType.Straight)
        case "left"     => Some(MovementType.Left)
        case "right"    => Some(MovementType.Right)
        case "rightarrow" => Some(MovementType.RightArrow)
        case _          => None