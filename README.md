# Q.scala <a href="http://thoughtworks.com/"><img align="right" src="https://www.thoughtworks.com/imgs/tw-logo.png" title="ThoughtWorks" height="15"/></a>

[![Build Status](https://travis-ci.org/ThoughtWorksInc/Q.scala.svg?branch=master)](https://travis-ci.org/ThoughtWorksInc/Q.scala)
[![Latest version](https://index.scala-lang.org/thoughtworksinc/q.scala/q/latest.svg)](https://index.scala-lang.org/thoughtworksinc/q.scala/q)
[![Scaladoc](https://javadoc.io/badge/com.thoughtworks.q/q_2.11.svg?label=scaladoc)](https://javadoc.io/page/com.thoughtworks.q/q_2.11/latest/com/thoughtworks/Q$.html)

**Q.scala** is a library to convert any value to parsable code.

## Usage

``` sbt
libraryDependencies += "com.thoughtworks.q" %% "q" % "latest.release"
```

``` scala
import com.thoughtworks.Q._
import scala.reflect.runtime.universe._

val data: Seq[Either[Double, String]] = Seq(Left(math.random), Right("string data")) 

// Output: _root_.scala.Seq(_root_.scala.util.Left(0.6437966035784641), _root_.scala.util.Right("string data"))
println(q"$data")
```
