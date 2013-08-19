import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "Asteroid"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "se.radley" %% "play-plugins-salat" % "1.2"
    //jdbc,
    //anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here   
    routesImport += "se.radley.plugin.salat.Binders._",
    templatesImport += "org.bson.types.ObjectId",
    resolvers += Resolver.sonatypeRepo("snapshots")     
  )

}
