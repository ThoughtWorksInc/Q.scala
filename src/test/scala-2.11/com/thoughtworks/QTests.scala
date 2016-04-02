package com.thoughtworks

import java.io.File
import java.util.UUID

import com.thoughtworks.Q._
import utest._

import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox
import scala.util.Failure

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
object QTests extends TestSuite {

  private def toolbox = currentMirror.mkToolBox()

  override def tests = this {
    * - {
      val code = showCode(MacroBundle[universe.type](universe).fullyQualifiedTreeOfSymbol(definitions.NilModule))
      code ==> "_root_.scala.collection.immutable.Nil"
    }

    * - {
      val value: Any = Some(Some(None))
      val tree = q"$value"
      showCode(tree) ==> "_root_.scala.Some(_root_.scala.Some(_root_.scala.None))"
      toolbox.eval(tree) ==> value
    }

    * - {
      val value: Any = Map(1 -> 2, 4 -> 5, 14 -> 51)
      val tree = q"$value"
      showCode(tree) ==> "_root_.scala.Predef.Map((1).->(2), (4).->(5), (14).->(51))"
      toolbox.eval(tree) ==> value
    }

    * - {
      val value: Any = List(1 -> 2, 4 -> 5, 14 -> 51)
      val tree = q"$value"
      showCode(tree) ==> "_root_.scala.Seq(_root_.scala.Tuple2(1, 2), _root_.scala.Tuple2(4, 5), _root_.scala.Tuple2(14, 51))"
      toolbox.eval(tree) ==> value
    }

    * - {
      val value: Any = Set("aaa", "bbb", "ccc")
      val tree = q"$value"
      showCode(tree) ==> """_root_.scala.Predef.Set("aaa", "bbb", "ccc")"""
      toolbox.eval(tree) ==> value
    }

    * - {
      import Q._
      val value: AnyRef = Array("aaa", "bbb", "ccc")
      val tree = q"$value"
      showCode(tree) ==> """_root_.scala.Array("aaa", "bbb", "ccc")"""
      toolbox.eval(tree) ==> value
    }

    * - {
      val value = Array(UUID.fromString("03a2d643-2241-408f-a960-77cb74c30eee"))
      val tree = q"$value"
      showCode(tree) ==> """_root_.scala.Array(_root_.java.util.UUID.fromString("03a2d643-2241-408f-a960-77cb74c30eee"))"""
      toolbox.eval(tree) ==> value
    }

    * - {
      val value: Any = new File("my-file.txt")
      val tree = q"$value"
      showCode(tree) ==> """new _root_.java.io.File("my-file.txt")"""
      toolbox.eval(tree) ==> value
    }

    * - {
      import Q._

      //      implicit def a = Q.implicitAnyLiftable[universe.type]
      //      implicitly[WeakTypeTag[Any]]
      val value: Any = 1
      val tree = q"$value"
      showCode(tree) ==> "1"
      toolbox.eval(tree) ==> value

    }
  }

  def main(args: Array[String]) {
    implicit def ec = utest.framework.ExecutionContext.RunNow
    format(tests.run(
      onComplete = { (subpath, s) =>
        s.value match {
          case Failure(e) =>
            e.printStackTrace()
          case _ =>
        }
      },
      wrap = utestWrap)
    ).map(print)
  }

}
