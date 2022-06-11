// Cross-project settings
ThisBuild / credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.topquadrant.net", "tq-public-ro", "Uwmtid9xk2ZH3Axjqy7h2JFx")
ThisBuild / resolvers += ("tq-maven-releases-public" at "https://nexus.topquadrant.net/repository/tq-maven-releases-public/")
ThisBuild / resolvers += ("tq-maven-snapshots-public" at "https://nexus.topquadrant.net/repository/tq-maven-snapshots-public/")
ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / scalaVersion := "2.11.12"
ThisBuild / scalacOptions ++= Seq("-feature", "-target:jvm-1.8")

ThisBuild / version := "1.0.1-SNAPSHOT"

lazy val rhapsody = (project in file("rhapsody"))
  .aggregate(rhapsodyApp, rhapsodyCli, rhapsodyLib)
  .settings(
    skip in publish := true
  )
lazy val rhapsodyApp = (project in file("rhapsody/module/app"))
  .dependsOn(rhapsodyLib)
  .enablePlugins(JettyPlugin)
  .settings(
    name := "rhapsody-app",
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.3",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test" artifacts Artifact("javax.servlet-api", "jar", "jar"),
      "org.eclipse.jetty" % "jetty-plus" % JettyVersion % "container;provided",
      "org.eclipse.jetty" % "jetty-webapp" % JettyVersion % "container",
      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
      "org.slf4j" % "slf4j-simple" % Slf4jVersion % "runtime",
      "org.topbraid" % "edg-af-api" % "1.0.1-SNAPSHOT",
      "org.topbraid" % "edg-client-java" % "1.0.2-SNAPSHOT"
    ),
    // Jetty settings
    containerArgs := Seq("--port", "8081"),
    containerShutdownOnExit := false, // Needed by script/server
  )
lazy val rhapsodyCli = (project in file("rhapsody/module/cli"))
  .dependsOn(rhapsodyLib)
  .enablePlugins(AssemblyPlugin)
  .settings(
    assembly / mainClass := Some("rhapsody.cli.Cli"),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    assembly / assemblyOutputPath := baseDirectory.value / "dist" / "rhapsody-cli.jar",
    libraryDependencies ++= Seq(
      "com.beust" % "jcommander" % "1.81",
      "org.slf4j" % "slf4j-simple" % Slf4jVersion % "runtime",
    ),
    name := "rhapsody-cli"
  )

lazy val rhapsodyLib = (project in file("rhapsody/module/lib"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := "rhapsody-lib",
    version := "1.0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.apache.jena" % "jena-core" % "3.16.0",
      "org.scalatest" %% "scalatest" % ScalatestVersion % Test,
      "org.slf4j" % "slf4j-simple" % Slf4jVersion % "runtime"
    ),
    // scalaxb dependencies
    libraryDependencies ++= Seq(
      "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.1", // https://github.com/eed3si9n/scalaxb/issues/481
      "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
    ),
    // The XSD files do not have different namespaces, so scala-sbt is not able to map namespaces to package names
    // Use a shell script instead and check the code in
    Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "gen" / "scala"
  )
// Dependency versions
val JettyVersion = "9.4.35.v20201120"
val ScalatraVersion = "2.7.1"
val ScalatestVersion = "3.2.3"
val Slf4jVersion = "1.7.30"