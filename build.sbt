scalaVersion := "2.11.4"

libraryDependencies ++= Seq("can", "routing") map { a => "io.spray" %% s"spray-$a" % "1.3.2" }

libraryDependencies += "io.spray" %% "spray-json" % "1.3.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.6"

Revolver.settings

assemblySettings