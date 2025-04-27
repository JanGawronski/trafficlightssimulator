val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "trafficlightssimulator",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0",
  )
