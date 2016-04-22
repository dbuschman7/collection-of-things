

object TowersOfHanoi {
  import scala.reflect.Manifest

  trait Nat
  final class _0 extends Nat
  final class Succ[Pre <: Nat] extends Nat

  type _1 = Succ[_0]
  type _2 = Succ[Succ[_0]]
  type _3 = Succ[_2]
  type _4 = Succ[Succ[Succ[Succ[_0]]]]

  case class Move[N <: Nat, A, B, C]()

  implicit def move0[A, B, C](implicit a: Manifest[A], b: Manifest[B]): Move[_0, A, B, C] = {
    def simpleName(m: Manifest[_]): String = m.toString.split("\\$").last
    println("move0 - from " + simpleName(a) + " to " + simpleName(b))
    null
  }

  implicit def moveN[P <: Nat, A, B, C](implicit m1: Move[P, A, C, B], m2: Move[_0, A, B, C], m3: Move[P, C, B, A]): Move[Succ[P], A, B, C] = {
    println(s"moveN")
    null
  }

  def run[N <: Nat, A, B, C](implicit m: Move[N, A, B, C]) = null

  case class Left()
  case class Center()
  case class Right()

  def main(args: Array[String]) {
    run[_3, Left, Right, Center]
  }
}

/**
Move from Left to Right
Move from Left to Center
Move from Right to Center
moveN - m1 = null null null
Move from Left to Right
Move from Center to Left
Move from Center to Right
Move from Left to Right
moveN - m1 = null null null
moveN - m1 = null null null
*/