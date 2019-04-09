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

  val tests = Tests {
    "Nil" - {
      val code = showCode(MacroBundle[universe.type](universe).fullyQualifiedSymbolTreeWithRootPrefix(definitions.NilModule))
      code ==> "_root_.scala.collection.immutable.Nil"
    }

    "Some and None" - {
      val value: Any = Some(Some(None))
      val tree = q"$value"
      showCode(tree) ==> "_root_.scala.Some(_root_.scala.Some(_root_.scala.None))"
      toolbox.eval(tree) ==> value
    }

    "Map" - {
      val value: Any = Map(1 -> 2, 4 -> 5, 14 -> 51)
      val tree = q"$value"
      showCode(tree) ==> "_root_.scala.Predef.Map((1).->(2), (4).->(5), (14).->(51))"
      toolbox.eval(tree) ==> value
    }

    "List" - {
      val value: Any = List(1 -> 2, 4 -> 5, 14 -> 51)
      val tree = q"$value"
      showCode(tree) ==> "_root_.scala.Seq(_root_.scala.Tuple2(1, 2), _root_.scala.Tuple2(4, 5), _root_.scala.Tuple2(14, 51))"
      toolbox.eval(tree) ==> value
    }

    "Set" - {
      val value: Any = Set("aaa", "bbb", "ccc")
      val tree = q"$value"
      showCode(tree) ==> """_root_.scala.Predef.Set("aaa", "bbb", "ccc")"""
      toolbox.eval(tree) ==> value
    }

    "Array[Byte]" - {
      import Q._
      val value: Any = Array[Byte](1, 2, 100)
      val tree = q"$value"
      showCode(tree) ==> """_root_.scala.Array[_root_.scala.Byte](1, 2, 100)"""
      toolbox.eval(tree) ==> value
    }

    "Array[String]" - {
      import Q._
      val value: AnyRef = Array("aaa", "bbb", "ccc")
      val tree = q"$value"
      showCode(tree) ==> """_root_.scala.Array[_root_.java.lang.String]("aaa", "bbb", "ccc")"""
      toolbox.eval(tree) ==> value
    }

    "Array[UUID]" - {
      val value = Array(UUID.fromString("03a2d643-2241-408f-a960-77cb74c30eee"))
      val tree = q"$value"
      showCode(tree) ==> """_root_.scala.Array[_root_.java.util.UUID](_root_.java.util.UUID.fromString("03a2d643-2241-408f-a960-77cb74c30eee"))"""
      toolbox.eval(tree) ==> value
    }

    "File" - {
      val value: Any = new File("my-file.txt")
      val tree = q"$value"
      showCode(tree) ==> """new _root_.java.io.File("my-file.txt")"""
      toolbox.eval(tree) ==> value
    }

    "Int" - {
      import Q._

      //      implicit def a = Q.implicitAnyLiftable[universe.type]
      //      implicitly[WeakTypeTag[Any]]
      val value: Any = 1
      val tree = q"$value"
      showCode(tree) ==> "1"
      toolbox.eval(tree) ==> value

    }

    "Seq[Expr]" - {
      import Q._
      val value: AnyRef = Seq(reify(1), reify(math.abs(2.0)))
      val tree = q"$value"
      showCode(tree) ==> """_root_.scala.Seq(_root_.scala.reflect.runtime.universe.reify(1), _root_.scala.reflect.runtime.universe.reify(`package`.abs(2.0)))"""
      val Seq(expr0: Expr[_], expr1: Expr[_]) = toolbox.eval(tree)// ==> value
      toolbox.eval(expr0.tree) ==> 1
      assert(toolbox.eval(expr1.tree).isInstanceOf[Double])
    }
  }

}
