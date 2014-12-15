scalaVersion := "2.11.4"

val sprayVersion = "1.3.2"

libraryDependencies ++= Seq("can", "routing") map { a => "io.spray" %% s"spray-$a" % sprayVersion }

libraryDependencies += "io.spray" %% "spray-json" % "1.3.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.7"

libraryDependencies += "io.spray" %% "spray-testkit" % sprayVersion % Test

Revolver.settings

assemblySettings

scalacOptions += "-feature"
