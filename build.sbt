import sbt.*
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "individuals-pensions-income-api"

lazy val ItTest = config("it") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    retrieveManaged                 := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(warnScalaVersionEviction = false),
    scalaVersion                    := "2.13.12",
    scalacOptions ++= List(
      "-language:higherKinds",
      "-Xlint:-byname-implicit",
      "-Xfatal-warnings",
      "-Wconf:src=routes/.*:silent",
      "-feature"
    )
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(majorVersion := 0)
  .settings(CodeCoverageSettings.settings *)
  .settings(defaultSettings() *)
  .configs(ItTest)
  .settings(inConfig(ItTest)(Defaults.itSettings) *)
  .settings(
    ItTest / fork                       := true,
    ItTest / unmanagedSourceDirectories := List((ItTest / baseDirectory).value / "it"),
    ItTest / unmanagedClasspath += baseDirectory.value / "resources",
    Runtime / unmanagedClasspath += baseDirectory.value / "resources",
    ItTest / javaOptions += "-Dlogger.resource=logback-test.xml",
    ItTest / parallelExecution := false,
    addTestReportOption(ItTest, "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(PlayKeys.playDefaultPort := 7762)

dependencyUpdatesFilter -= moduleFilter(organization = "org.playframework")
dependencyUpdatesFilter -= moduleFilter(name = "simple-reactivemongo")
dependencyUpdatesFilter -= moduleFilter(name = "reactivemongo-test")
dependencyUpdatesFilter -= moduleFilter(name = "scala-library")
dependencyUpdatesFilter -= moduleFilter(name = "flexmark-all")
dependencyUpdatesFilter -= moduleFilter(name = "scalatestplus-play")
dependencyUpdatesFilter -= moduleFilter(name = "scalatestplus-scalacheck")
dependencyUpdatesFailBuild := true
