package com.thoughtworks

import java.io.File
import java.net.{URI, URL}
import java.util.UUID

import scala.reflect.api.{Symbols, TreeCreator, Universe}
import scala.reflect.internal.annotations.compileTimeOnly

/**
  * Serialize a runtime to a compiler-time Scala AST
  *
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
object Q {

  import scala.language.experimental.macros

  private[thoughtworks] def isRootPackage(symbol: Symbols#Symbol): Boolean = {
    if (symbol.name == scala.reflect.runtime.universe.nme.ROOTPKG) {
      true
    } else if (symbol.owner.owner == symbol.owner) {
      true
    } else {
      symbol match {
        case internalSymbol: scala.reflect.internal.Symbols#Symbol if internalSymbol.isRoot || internalSymbol.isRootPackage =>
          true
        case _ =>
          false
      }
    }
  }

  private[thoughtworks] final case class MacroBundle[U <: Universe](universe: U) {

    import universe._

    /**
      * Aims to disable the implicit value [[Q.implicitDynamicLift]] in this [[MacroBundle]] scope.
      */
    @compileTimeOnly("This method should never be called!")
    private def implicitDynamicLift = ???


    // TODO: import management
    private[thoughtworks] final def fullyQualifiedSymbolTree(symbol: Symbols#Symbol): Tree = {
      val owner = symbol.owner
      val name = newTermName(symbol.name.encodedName.toString)
      if (isRootPackage(owner)) {
        Ident(name)
      } else {
        val ownerTree = fullyQualifiedSymbolTree(owner)
        Select(ownerTree, name)
      }
    }

    private[thoughtworks] final def fullyQualifiedSymbolTreeWithRootPrefix(symbol: Symbols#Symbol): Tree = {
      def termTree(symbol: Symbols#Symbol): Tree = {
        if (isRootPackage(symbol)) {
          q"_root_"
        } else {
          val owner = symbol.owner
          val ownerTree = termTree(owner)
          val name = newTermName(symbol.name.encodedName.toString)
          Select(ownerTree, name)
        }
      }
      if (symbol.isType) {
        if (isRootPackage(symbol)) {
          tq"_root_"
        } else {
          val owner = symbol.owner
          val ownerTree = termTree(owner)
          val name = newTypeName(symbol.name.encodedName.toString)
          Select(ownerTree, name)
        }
      } else {
        if (isRootPackage(symbol)) {
          q"_root_"
        } else {
          val owner = symbol.owner
          val ownerTree = termTree(owner)
          val name = newTermName(symbol.name.encodedName.toString)
          Select(ownerTree, name)
        }
      }
    }

    private[Q] final def treeOf(value: Any): universe.Tree = {
      import universe._
      value match {
        case null => q"""null"""
        case typed: String => q"""$typed"""
        case typed: Unit => q"""$typed"""
        case typed: Char => q"""$typed"""
        case typed: Double => q"""$typed"""
        case typed: Float => q"""$typed"""
        case typed: Long => q"""$typed"""
        case typed: Int => q"""$typed"""
        case typed: Short => q"""$typed"""
        case typed: Byte => q"""$typed"""
        case symbol: scala.Symbol => q"""$symbol"""
        case file: File => q"""new _root_.java.io.File(${file.getPath})"""
        case url: URL => q"""new _root_.java.net.URL(${url.toString})"""
        case uri: URI => q"""new _root_.java.net.URI(${uri.toString})"""
        case uuid: UUID => q"""_root_.java.util.UUID.fromString(${uuid.toString})"""
        case array: Array[_] =>
          import scala.reflect.runtime.currentMirror
          val elementTypeTree = fullyQualifiedSymbolTreeWithRootPrefix(currentMirror.classSymbol(array.getClass.getComponentType))
          val elementTrees = (for {
            element <- array
          } yield {
            treeOf(element)
          }) (collection.breakOut(List.canBuildFrom))
          q"""_root_.scala.Array[$elementTypeTree](..$elementTrees)"""
        case set: Set[_] =>
          val elementTrees = (for {
            element <- set
          } yield {
            treeOf(element)
          }) (collection.breakOut(List.canBuildFrom))
          q"""_root_.scala.Predef.Set(..$elementTrees)"""
        case seq: Seq[_] =>
          val elementTrees = (for {
            element <- seq
          } yield {
            treeOf(element)
          }) (collection.breakOut(List.canBuildFrom))
          q"""_root_.scala.Seq(..$elementTrees)"""
        case map: Map[_, _] =>
          val keyValueTrees = (for {
            (key, value) <- map
          } yield {
            q"""${treeOf(key)} -> ${treeOf(value)}"""
          }) (collection.breakOut(List.canBuildFrom))
          q"""_root_.scala.Predef.Map(..$keyValueTrees)"""
        case _ =>
          val classSymbol = reflect.runtime.currentMirror.classSymbol(value.getClass)
          if (classSymbol.isModuleClass) {
            fullyQualifiedSymbolTreeWithRootPrefix(reflect.runtime.currentMirror.moduleSymbol(value.getClass))
          } else {
            value match {
              case product: Product =>
                val companionTree = fullyQualifiedSymbolTreeWithRootPrefix(classSymbol.companion)
                val parameterTreeIterator = for {
                  parameters <- product.productIterator
                } yield {
                  treeOf(parameters)
                }
                Apply(companionTree, parameterTreeIterator.toList)
              case _ =>
                throw new IllegalArgumentException(s"Cannot serialize $value")
            }
          }
      }

    }
  }


  implicit def implicitDynamicLift[U <: Universe, A](implicit tag: U#WeakTypeTag[A]): U#Liftable[A] = {
    dynamicLift[A](tag.mirror.universe)
  }


  def exprOf[U <: Universe, A](value: A)(implicit tag: U#WeakTypeTag[A]): U#Expr[A] = {
    import tag.mirror.universe
    import universe._
    val treeCreator = new TreeCreator {
      def apply[U <: Universe with Singleton](m: scala.reflect.api.Mirror[U]): U#Tree = {
        MacroBundle[m.universe.type](m.universe).treeOf(value)
      }
    }
    Expr[A](rootMirror, treeCreator)
  }

  def dynamicLift[A](universe: Universe): universe.Liftable[A] = {
    import universe._
    new Liftable[A] {
      override def apply(value: A): Tree = {
        MacroBundle[universe.type](universe).treeOf(value)
      }
    }
  }

}