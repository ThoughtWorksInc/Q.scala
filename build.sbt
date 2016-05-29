organization in ThisBuild := "com.thoughtworks.q"

name := "q"

crossScalaVersions in ThisBuild := Seq("2.10.6", "2.11.8", "2.12.0-M4")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % Test

libraryDependencies += "com.lihaoyi" %% "utest" % "0.4.3" % Test

testFrameworks += new TestFramework("utest.runner.Framework")

libraryDependencies ++= {
  if (scalaBinaryVersion.value == "2.10") {
    Seq("org.scalamacros" %% "quasiquotes" % "2.1.0")
  } else {
    Seq()
  }
}
