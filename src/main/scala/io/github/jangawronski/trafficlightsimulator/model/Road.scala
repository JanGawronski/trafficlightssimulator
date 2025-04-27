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
