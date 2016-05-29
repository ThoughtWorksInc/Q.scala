organization := "com.thoughtworks.q"

name := "q"

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
