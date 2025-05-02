//> using scala "3.6.4"
//> using dep "org.typelevel::cats-core:2.13.0"
//> using dep "org.typelevel::cats-effect:3.6.1"
//> using dep "com.lihaoyi::upickle:4.1.0"
package io.github.jangawronski.trafficlightsimulator

import cats.effect._
import cats.syntax.either._
import java.nio.file.Path
import java.nio.file.Files
import scala.collection.mutable.ListBuffer

import model._
import simulation._
import simulation.schedulers._
import io._

object Main extends IOApp {

  private val DefaultConfig = Path.of("configs/simple.json")

  def run(args: List[String]): IO[ExitCode] = args match {
    case input :: output :: config :: Nil =>
      simulateAll(Path.of(input), Path.of(output), Path.of(config))
        .as(ExitCode.Success)

    case input :: output :: Nil =>
      simulateAll(Path.of(input), Path.of(output), DefaultConfig)
        .as(ExitCode.Success)

    case _ =>
      IO.println(
        "Usage: run <commands.json> <output.json> [<config.json>]\n" +
        "  If you omit <config.json>, default config will be used"
      ) *> IO.pure(ExitCode(2))
  }

  private def simulateAll(
    inPath:  Path,
    outPath: Path,
    cfgPath: Path
  ): IO[Unit] = for {
    cfgJson <- IO.blocking(Files.readString(cfgPath))
    cfgDto = upickle.default.read[ConfigDto](cfgJson)
    (config, scheduler, lightDuration) <- IO.fromEither(
                  ConfigParser.toDomain(cfgDto)
                    .leftMap(err => new RuntimeException("Config error: " + err.msg))
                )

    cmdJson <- IO.blocking(Files.readString(inPath))
    cmdDto = upickle.default.read[CommandsDto](cmdJson)
    commands <- IO.fromEither(
                  CommandsParser.toDomain(cmdDto)
                    .leftMap(err => new RuntimeException("Commands error: " + err.msg))
                )

    intersection = new Intersection(config)
    results = simulate(intersection, scheduler, commands, lightDuration)

    outputDto = OutputDto(results.map(ss => StepStatusDto(ss.leftVehicles)))

    _ <- IO.blocking {
          val json = upickle.default.write(outputDto, indent = 2)
          Files.writeString(outPath, json)
        }
  } yield ()

  def simulate(
    intersection: Intersection,
    scheduler:    Scheduler,
    commands:     Seq[Command],
    lightDuration: Int
  ): Seq[StepStatus] = {
    val results = ListBuffer.empty[StepStatus]
    var history: Seq[PhasePlan] = Nil

    var currentPlan: PhasePlan = PhasePlan(Map.empty) // or pick an initial plan
    var stepsLeftInPhase: Int = 0

    commands.foreach {
      case AddVehicleCmd(vehicleId, start, end) =>
        intersection.enqueue(Vehicle(vehicleId, start, end, history.size))

    case StepCmd =>
      if (stepsLeftInPhase <= 0) {
        currentPlan = scheduler.nextPlan(
                                  history,
                                  intersection.config,
                                  intersection.laneVehicles
                                )
        stepsLeftInPhase = lightDuration
      }

      results += intersection.applyPlan(currentPlan)

      history = history :+ currentPlan
      stepsLeftInPhase -= 1
  }

    results.toList
  }
}