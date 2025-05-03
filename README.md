## Traffic Light Simulation

This is a simple traffic light simulation written in Scala 3.

It supports the following features:
- multiple lanes for each of the four directions (north, south, east, west)
- lanes having its own traffic light and support for different directions
- two schedulers (one based on waiting time, one based on the number of vehicles)

### Usage

To run the simulation, you need to have Scala 3 installed. You can run simulation using `sbt`, but preferred way is by using `scala-cli` in root directory.

```bash
scala-cli run . -- input.json output.json [config.json]
```

#### Input

Input file is a JSON file containing commands for the simulation in similar manner as the following example:

```json
{
  "commands": [
    {
      "type": "addVehicle",
      "vehicleId": "vehicle1", // unique vehicle ID
      "startRoad": "north", // "south", "east", "west"
      "endRoad": "south" // "north", "east", "west"
    },
    {
        "type": "step"
    }
  ]
}
```

Two types of commands are supported:
- `addVehicle`: adds a vehicle to the simulation. The vehicle will be added to the start road and will try to reach the end road (startRoad and endRoad have to be different).
- `step`: advances the simulation by one step. The simulation will process all vehicles that are waiting at the traffic lights and will update the state of the traffic lights.

#### Output

Output file is a JSON file containing list of vehicles that left intersection in given step.
```json
{
  "stepStatuses": [
    {
      "leftVehicles": [
        "vehicle1"
      ]
    }
  ]
}
```

#### Config

Config file is a JSON file containing configuration for the simulation. It is optional and if not provided, `configs/simple.json` will be used. The configuration looks like this:
```json
{ "lanes": {
    "north":[["straight", "left", "right"]],
    "south":[["left"], ["straight"], ["right"]],
    "east":[["left"], ["straight", "right"]],
    "west":[["straight", "left", "right"]]
  },
  "scheduler": "vehicleCount", // "longestWaiting" or "vehicleCount"
  "lightDuration": 1
}
```

`lanes` has to be a map of directions (north, south, east, west) to a list of lanes. Each lane is a list of directions that are allowed to go from that lane. For example, `["straight", "left", "right"]` means that the lane can go straight, left or right. The order of the lanes is important, as it determines the order in which the lanes will be processed. For example, `[["right"], ["left"]]` is not allowed as it would mean unneessary conflict between left and right turns, but `[["left"], ["right"]]` is allowed.

The `scheduler` is the type of scheduler that will be used for the simulation. It can be either `longestWaiting` or `vehicleCount`. 

The `lightDuration` is the number of steps between getting next set of green lights from scheduler. It makes traffic lights more realistic, as they are not changing after only one vehicle.

### Example

Running:
```bash
scala-cli run . -- inputs/frommail.json output.json
```

should save following to `output.json`:
```json
{
  "stepStatuses": [
    {
      "leftVehicles": [
        "vehicle2",
        "vehicle1"
      ]
    },
    {
      "leftVehicles": []
    },
    {
      "leftVehicles": [
        "vehicle3"
      ]
    },
    {
      "leftVehicles": [
        "vehicle4"
      ]
    }
  ]
}
```

### How it works

Schedulers work by ordering the lanes according to the type of scheduler. The `longestWaiting` scheduler orders the lanes by the waiting time of the first vehicle in the lane. The `vehicleCount` scheduler orders the lanes by the number of vehicles in the lane. Then scheduler greedily chooses maximal weight independent set of lanes that can be green at the same time. The algorithm works by choosing the lane with the highest weight and removing all lanes that are in conflict with it. Then it repeats the process until no more lanes can be added. So the time complexity is O(n^2)

Problem of finding maximal weight independent set is generally NP-hard, but polynomial for some graphs (chordal, bipartite, planar, etc.). Unfortunately, the graph of lanes is not one of them. Adding a scheduler that would work like `vehicleCount` but would be interested in biggest sum of vehicles would make calculations be very slow for bigger intersections.

Conflicts between lanes are defined as two lanes that cannot be green at the same time. For example, if one lane is going straight and perpendicular lane is going straight or left, they are in conflict. Or if one lane is going left-only and opposite lane is going straight, they are in conflict. In situation where lane is going left and straight, and opposite lane is going straight are not in conflict, as it would terrible for small intersections. Instead if vehicle wants to go left and on opposite side is going straight, it will wait for the vehicle to pass (won't go in this step).
Situation where lane is going left and righthand-side lane is going straight brings no conflict, as they are not crossing each other. 

In case of many lanes vehicles go to the lane with smallest number of vehicles. If there are two or more lanes with the same number of vehicles, more specialized one is chosen.