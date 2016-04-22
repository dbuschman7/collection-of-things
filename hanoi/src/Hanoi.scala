

object Hanoi {

  val pegs = Array(List(1, 2, 3), List[Int](), List[Int]())

  def moveDisk(from: Int, to: Int) {
    require(pegs(to).isEmpty || pegs(from).head < pegs(to).head)
    pegs(to) ::= pegs(from).head
    pegs(from) = pegs(from).tail
  }

  def moveStack(n: Int, from: Int, to: Int) {
    if (n == 1) moveDisk(from, to)
    else {
      val other = 3 - from - to
      moveStack(n - 1, from, other)
      moveDisk(from, to)
      moveStack(n - 1, other, to)
    }
  }

  def main(args: Array[String]): Unit = {

    println(pegs.mkString("   "))
    moveStack(3, 0, 2)
    println(pegs.mkString("   "))
  }
}