package chapter3.exercises.ex1

import chapter3.List
import chapter3.Nil
import chapter3.Cons
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec

// tag::init[]
fun <A> tail(xs: List<A>): List<A> = when(xs) {
        is Cons -> xs.tail
        is Nil -> throw IllegalStateException()
    }


// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise1 : WordSpec({
    "list tail" should {
        "return the the tail when present" {
            tail(List.of(1, 2, 3, 4, 5)) shouldBe
                List.of(2, 3, 4, 5)
        }

        "throw an illegal state exception when no tail is present" {
            shouldThrow<IllegalStateException> {
                tail(Nil)
            }
        }
    }
})




package chapter3.exercises.ex2

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec

// tag::init[]
fun <A> setHead(xs: List<A>, x: A): List<A> =
    when(xs) {
        is Nil -> throw IllegalStateException()
        is Cons -> Cons(x, xs.tail)
    }
// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise2 : WordSpec({
    "list setHead" should {
        "return a new List with a replaced head" {
            setHead(List.of(1, 2, 3, 4, 5), 6) shouldBe
                List.of(6, 2, 3, 4, 5)
        }

        "throw an illegal state exception when no head is present" {
            shouldThrow<IllegalStateException> {
                setHead(Nil, 6)
            }
        }
    }
})




package chapter3.exercises.ex4

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import chapter3.exercises.ex3.drop
import chapter7.sec1.l
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

// tag::init[]
fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> = when(l) {
    is Nil -> l
    is Cons -> when(f(l.head)) {
        true -> {
            dropWhile(l.tail, f)
        }
        false -> l
    }
}




// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise4 : WordSpec({

    "list dropWhile" should {
        "drop elements until predicate is no longer satisfied" {
            dropWhile(
                List.of(1, 2, 3, 4, 5)
            ) { it < 4 } shouldBe List.of(4, 5)
        }

        "drop no elements if predicate never satisfied" {
            dropWhile(
                List.of(1, 2, 3, 4, 5)
            ) { it == 100 } shouldBe List.of(1, 2, 3, 4, 5)
        }

        "drop all elements if predicate always satisfied" {
            dropWhile(
                List.of(1, 2, 3, 4, 5)
            ) { it < 100 } shouldBe List.of()
        }

        "return Nil if input is empty" {
            dropWhile(List.empty<Int>()) { it < 100 } shouldBe Nil
        }

        // 이건 아니다. 처음에 dropWhile이 false면 dropWhile은 동작을 멈춤.
        // "drop no elements if predicate equals one" {
        //     dropWhile(
        //         List.of(1, 2, 3, 4, 5)
        //     ) { it == 2 } shouldBe List.of(1, 3, 4, 5)
        // }
    }
})






package chapter3.exercises.ex5

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A> init(l: List<A>): List<A> = when(l) {
    is Nil -> throw IllegalStateException()
    is Cons -> {
        // val head = l.head
        // val tail = l.tail
        // if(tail is Nil) {
        //     Nil
        // } else {
        //     Cons(head, init(tail))
        // }
        when(l.tail) {
            is Nil -> Nil
            is Cons -> Cons(l.head, init(l.tail))
        }
    }
}

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise5 : WordSpec({

    "list init" should {
        "return all but the last element" {
            init(List.of(1, 2, 3, 4, 5)) shouldBe
                List.of(1, 2, 3, 4)
        }

        "return Nil if only one element exists" {
            init(List.of(1)) shouldBe Nil
        }

        "throw an exception if no elements exist" {
            shouldThrow<IllegalStateException> {
                init(List.empty<Int>())
            }
        }
    }
})






package chapter3.exercises.ex6

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import chapter3.foldRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec


fun <A, B> foldRight(xs: List<A>, z: B, f: (a: A, b: B) -> B): B  = when(xs) {
    is Nil -> z
    is Cons -> f(xs.head, foldRight(xs.tail, z, f))
}
// tag::init[]
fun <A> product(xs: List<Double>) = foldRight(xs, 1.0) { a, b -> a * b }
fun <A> product2(xs: List<Double>): Double = foldRight(xs, 1.0) { a, b -> if( b == 0.0) {a} else {a*b} }

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise5 : WordSpec({

    "list product" should {
        "return all elements by product" { product<Double>(List.of(1.0, 2.0, 3.0, 4.0, 5.0)) shouldBe 120.0 }
        "return stoped product when meet 0.0" { product2<Double>(List.of(1.0, 2.0, 0.0, 3.0, 4.0)) shouldBe 2.0 }
    }
})









package chapter3.exercises.ex7

import chapter3.Cons
import chapter3.List
import chapter3.Nil

fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
    when (xs) {
        is Nil -> z
        is Cons -> f(xs.head, foldRight(xs.tail, z, f))
    }

val f = { x: Int, y: List<Int> -> Cons(x, y) }
val z = Nil as List<Int>

val trace = {
    foldRight(List.of(1, 2, 3), z, f)
    Cons(1, foldRight(List.of(2, 3), z, f))
    Cons(1, Cons(2, foldRight(List.of(3), z, f)))
    Cons(1, Cons(2, Cons(3, foldRight(List.empty(), z, f))))
    Cons(1, Cons(2, Cons(3, Nil)))
}

fun main() {
    // tag::init[]
    foldRight(
        Cons(1, Cons(2, Cons(3, Nil))),
        Nil as List<Int>,
        { x, y -> Cons(x, y) }
    )
    // end::init[]
}





package chapter3.exercises.ex8

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import chapter3.foldRight
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

// tag::init[]
fun <A> length(xs: List<A>): Int =foldRight(xs, 0,  {a, b-> 1+b})
// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise8 : WordSpec({
    "list length" should {
        "calculate the length" {
            length(List.of(1, 2, 3, 4, 5)) shouldBe 5
        }

        "calculate zero for an empty list" {
            length(Nil) shouldBe 0
        }
    }
})






package chapter3.exercises.ex9

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

// tag::init[]
// tailrec fun foldLeft(xs: List<Int>, z: Int, f: (Int, Int) -> Int): Int =
//     when(xs) {
//         is Nil -> z
//         is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
//     }

tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B {
    return when(xs) {
        is Nil -> {
            println("> $z\t")
            z
        }
        is Cons -> {
            val r = foldLeft(xs.tail, f(z, xs.head), f)
            println("> ${f(z, xs.head)}: $r\t")
            r
        }
    }
}

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise9 : WordSpec({
    "list foldLeft" should {
        """apply a function f providing a zero accumulator from tail
            recursive position""" {
            // val arr: Array<Int> = (0..9999).toList().toIntArray().toTypedArray()
            val xs = List.of(0, 1, 2, 3,  4, 5)
            foldLeft(
                xs,
                0
            )
            // { x, y -> x + y }) shouldBe 20
            { x, y -> x + y } shouldBe 15
        }
    }
})







package chapter3.exercises.ex10

import chapter3.List
import chapter3.foldLeft
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

// tag::init[]
fun sumL(xs: List<Int>): Int =
    foldLeft(xs, 0, {a, b -> a + b})


fun productL(xs: List<Double>): Double =
    foldLeft(xs, 1.0, {a, b -> a * b})


fun <A> lengthL(xs: List<A>): Int =
    foldLeft(xs, 0, {b, a -> b + 1 })

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise10 : WordSpec({
    "list sumL" should {
        "add all integers" {
            sumL(List.of(1, 2, 3, 4, 5)) shouldBe 15
        }
    }

    "list productL" should {
        "multiply all doubles" {
            productL(List.of(1.0, 2.0, 3.0, 4.0, 5.0)) shouldBe
                120.0
        }
    }

    "list lengthL" should {
        "count the list elements" {
            lengthL(List.of(1, 2, 3, 4, 5)) shouldBe 5
        }
    }
})












package chapter3.exercises.ex11

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import chapter3.foldLeft
import chapter3.foldRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A> reverse(xs: List<A>): List<A> =
    foldLeft(xs, List.empty()) { a, b -> Cons(b, a) }
// foldRight(xs, List.empty()) { a, b -> Cons(a, b) }

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise11 : WordSpec({
    "list reverse" should {
        "reverse list elements" {
            reverse(List.of(1, 2, 3, 4, 5)) shouldBe
                List.of(5, 4, 3, 2, 1)
        }
    }
})




package chapter3.exercises.ex12

import chapter3.List
import chapter3.foldLeft
import chapter3.foldRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A, B> foldLeftR(xs: List<A>, z: B, f: (B, A) -> B): B =
    foldRight(xs, z, {a, b -> f(b, a)})

fun <A, B> foldRightL(xs: List<A>, z: B, f: (A, B) -> B): B =
    foldLeft(xs, z, {a, b -> f(b, a)})

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise12 : WordSpec({
    "list foldLeftR" should {
        "implement foldLeft functionality using foldRight" {
            foldLeftR(
                List.of(1, 2, 3, 4, 5),
                0,
                { x, y -> x + y }) shouldBe 15
        }
    }

    "list foldRightL" should {
        "implement foldRight functionality using foldLeft" {
            foldRightL(
                List.of(1, 2, 3, 4, 5),
                0,
                { x, y -> x + y }) shouldBe 15
        }
    }
})




package chapter3.exercises.ex13

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import chapter3.foldLeft
import chapter3.foldRight
import chapter3.reverse
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A> append(a1: List<A>, a2: List<A>): List<A> = foldRight(a1, a2) { a, b -> Cons(a, b) }
// end::init[]

fun <A> appendL(a1: List<A>, a2: List<A>): List<A> = foldLeft(reverse(a1), a2) {a, b -> Cons(b, a) }

//TODO: Enable tests by removing `!` prefix
class Exercise13 : WordSpec({
    "list append" should {
        "append two lists to each other using foldRight" {
            append(
                List.of(1, 2, 3),
                List.of(4, 5, 6)
            ) shouldBe List.of(1, 2, 3, 4, 5, 6)
        }
    }

    "list appendL" should {
        "append two lists to each other using foldLeft" {
            appendL(
                List.of(1, 2, 3),
                List.of(4, 5, 6)
            ) shouldBe List.of(1, 2, 3, 4, 5, 6)
        }
    }
})






package chapter3.exercises.ex14

import chapter3.Cons
import chapter3.List
import chapter3.List.Companion.empty
import chapter3.Nil
import chapter3.append
import chapter3.foldRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A> concat(lla: List<List<A>>): List<A> =
    foldRight(lla, empty(), {a1, b1 -> foldRight(a1, b1, {a2, b2 -> Cons(a2, b2)}) })

fun <A> concat2(lla: List<List<A>>): List<A> =
    foldRight(lla, empty(), {a1: List<A>, b1: List<A> -> append(a1, b1)})

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise14 : WordSpec({
    "list concat" should {
        "concatenate a list of lists into a single list" {
            concat(
                List.of(
                    List.of(1, 2, 3),
                    List.of(4, 5, 6)
                )
            ) shouldBe List.of(1, 2, 3, 4, 5, 6)

            concat2(
                List.of(
                    List.of(1, 2, 3),
                    List.of(4, 5, 6)
                )
            ) shouldBe List.of(1, 2, 3, 4, 5, 6)
        }
    }
})






package chapter3.exercises.ex15

import chapter3.Cons
import chapter3.List
import chapter3.List.Companion.empty
import chapter3.foldRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun increment(xs: List<Int>): List<Int> =
    foldRight(xs, empty(), {a, b -> Cons(a + 1, b) })
// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise15 : WordSpec({
    "list increment" should {
        "add 1 to every element" {
            increment(List.of(1, 2, 3, 4, 5)) shouldBe
                List.of(2, 3, 4, 5, 6)
        }
    }
})





package chapter3.exercises.ex16

import chapter3.Cons
import chapter3.List
import chapter3.List.Companion.empty
import chapter3.foldRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun doubleToString(xs: List<Double>): List<String> =
    foldRight(xs, empty(), {a, b -> Cons(a.toString(), b) })
// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise16 : WordSpec({
    "list doubleToString" should {
        "convert every double element to a string" {
            doubleToString(List.of(1.1, 1.2, 1.3, 1.4)) shouldBe
                List.of("1.1", "1.2", "1.3", "1.4")
        }
    }
})





package chapter3.exercises.ex17

import chapter3.Cons
import chapter3.List
import chapter3.List.Companion.empty
import chapter3.foldRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

// tag::init[]
fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
   foldRight(xs, empty()) { a, b -> Cons(f(a), b) }

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise17 : WordSpec({
    "list map" should {
        "apply a function to every list element" {
            map(List.of(1, 2, 3, 4, 5)) { it * 10 } shouldBe
                List.of(10, 20, 30, 40, 50)
        }
    }
})




package chapter3.exercises.ex18

import chapter3.Cons
import chapter3.List
import chapter3.List.Companion.empty
import chapter3.Nil
import chapter3.foldLeft
import chapter3.foldRight
import chapter3.reverse
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
    reverse(foldLeft(xs, empty(), {ls, a -> if(f(a)){Cons(a, ls)} else {ls} } ))
    // foldRight(xs, empty(), {a, ls -> if(f(a)){Cons(a, ls)} else {ls} } )

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise18 : WordSpec({
    "list filter" should {
        "filter out elements not compliant to predicate" {
            val xs = List.of(1, 2, 3, 4, 5)
            filter(xs) { it % 2 == 0 } shouldBe List.of(2, 4)
        }
    }
})

















package chapter3.exercises.ex19

import chapter3.List
import chapter3.List.Companion.empty
import chapter3.append
import chapter3.foldLeft
import chapter3.foldRight
import chapter5.sec1.a
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A, B> flatMap(xa: List<A>, f: (A) -> List<B>): List<B> =
    foldLeft(xa, empty(), {b: List<B>, a: A  ->  append(b, f(a)) })
    // foldRight(xa, empty(), {a: A, b: List<B> ->  append(f(a), b) })

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise19 : WordSpec({
    "list flatmap" should {
        "map and flatten a list" {
            val xs = List.of(1, 2, 3)
            flatMap(xs) { i -> List.of(i, i) } shouldBe
                List.of(1, 1, 2, 2, 3, 3)
        }
    }
})













package chapter3.exercises.ex20

import chapter3.List
import chapter3.List.Companion.empty
import chapter3.flatMap
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A> filter2(xa: List<A>, f: (A) -> Boolean): List<A> =
    flatMap(xa) {a -> if(f(a)) {List.of(a)} else {empty()}}
// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise20 : WordSpec({
    "list filter" should {
        "filter out elements not compliant to predicate" {
            filter2(
                List.of(1, 2, 3, 4, 5)
            ) { it % 2 == 0 } shouldBe List.of(2, 4)
        }
    }
})



package chapter3.exercises.ex21

import chapter10.List.Companion.empty
import chapter3.Cons
import chapter3.List
import chapter3.Nil
import chapter3.foldRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun add(xa: List<Int>, xb: List<Int>): List<Int> =
    when {
        xa is Cons && xb is Cons  -> {
            Cons(xa.head + xb.head, add(xa.tail, xb.tail))
        }
        else -> Nil
    }
    // List.of(5,7,9)

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise21 : WordSpec({
    "list add" should {
        "add elements of two corresponding lists" {
            add(List.of(1, 2, 3), List.of(4, 5, 6)) shouldBe
                List.of(5, 7, 9)
        }
    }
})





package chapter3.exercises.ex22

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A> zipWith(xa: List<A>, xb: List<A>, f: (A, A) -> A): List<A> =
    when {
        xa is Cons && xb is Cons -> {
            Cons(f(xa.head, xb.head), zipWith(xa.tail, xb.tail, f))
        }
        else -> Nil
    }

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise22 : WordSpec({
    "list zipWith" should {
        "apply a function to elements of two corresponding lists" {
            zipWith(
                List.of(1, 2, 3),
                List.of(4, 5, 6)
            ) { x, y -> x + y } shouldBe List.of(5, 7, 9)
        }
    }
})



package chapter3.exercises.ex23

import chapter3.Cons
import chapter3.List
import chapter3.Nil
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::startsWith[]
tailrec fun <A> startsWith(l1: List<A>, l2: List<A>): Boolean =
    when {
        l1 is Cons && l2 is Cons -> {
            if(l1.head == l2.head) {
                startsWith(l1.tail, l2.tail)
            } else {
                false
            }
        }
        l2 is Nil -> true
        else -> false
    }

// end::startsWith[]

// tag::init[]
tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean =
    when {
        xs is Cons && sub is Cons -> {
            if(startsWith(xs, sub)) {
                true
            } else {
                hasSubsequence(xs.tail, sub)
            }
        }
        else ->  false
    }


// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise23 : WordSpec({
    "list subsequence" should {
        "determine if a list starts with" {
            val xs = List.of(1, 2, 3)
            startsWith(xs, List.of(1)) shouldBe true
            startsWith(xs, List.of(1, 2)) shouldBe true
            startsWith(xs, xs) shouldBe true
            startsWith(xs, List.of(2, 3)) shouldBe false
            startsWith(xs, List.of(3)) shouldBe false
            startsWith(xs, List.of(6)) shouldBe false
        }

        "identify subsequences of a list" {
            val xs = List.of(1, 2, 3, 4, 5)
            hasSubsequence(xs, List.of(1)) shouldBe true
            hasSubsequence(xs, List.of(1, 2)) shouldBe true
            hasSubsequence(xs, List.of(2, 3)) shouldBe true
            hasSubsequence(xs, List.of(3, 4)) shouldBe true
            hasSubsequence(xs, List.of(3, 4, 5)) shouldBe true
            hasSubsequence(xs, List.of(4)) shouldBe true

            hasSubsequence(xs, List.of(1, 4)) shouldBe false
            hasSubsequence(xs, List.of(1, 3)) shouldBe false
            hasSubsequence(xs, List.of(2, 4)) shouldBe false
        }
    }
})



package chapter3.exercises.ex24

import chapter3.Branch
import chapter3.Leaf
import chapter3.Tree
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A> size(tree: Tree<A>): Int =
    when(tree) {
        is Branch -> {
            1 + size(tree.left) + size(tree.right)
        }
        is Leaf -> {
            1
        }
    }

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise24 : WordSpec({
    "tree size" should {
        "determine the total size of a tree" {
            val tree =
                Branch(
                    Branch(Leaf(1), Leaf(2)),
                    Branch(Leaf(3), Leaf(4))
                )
            size(tree) shouldBe 7
        }
    }
})


package chapter3.exercises.ex25

import chapter3.Branch
import chapter3.Leaf
import chapter3.Tree
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE
import kotlin.math.max

// tag::init[]
fun maximum(tree: Tree<Int>): Int =
    when(tree) {
        is Branch -> maxOf(maximum(tree.left), maximum(tree.right))
        is Leaf -> tree.value
    }
// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise25 : WordSpec({
    "tree maximum" should {
        "determine the maximum value held in a tree" {
            val tree = Branch(
                Branch(Leaf(1), Leaf(9)),
                Branch(Leaf(3), Leaf(4))
            )
            maximum(tree) shouldBe 9
        }
    }
})



package chapter3.exercises.ex26
import chapter3.Branch import chapter3.Leaf
import chapter3.Tree
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun depth(tree: Tree<Int>): Int =
    when(tree) {
        is Branch -> maxOf(depth(tree.left)+1, depth(tree.right)+1 )
        else -> 0
    }

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise26 : WordSpec({
    "tree depth" should {
        "determine the maximum depth from the root to any leaf" {
            val tree = Branch( //0
                Branch(Leaf(1), Leaf(2)), //2
                Branch(
                    Leaf(3), //2
                    Branch(
                        Branch(Leaf(4), Leaf(5)), //4
                        Branch(
                            Leaf(6), //4
                            Branch(Leaf(7), Leaf(8))
                        )
                    )
                )
            ) //5
            depth(tree) shouldBe 5
        }
    }
})






package chapter3.exercises.ex27

import chapter3.Branch
import chapter3.Leaf
import chapter3.Tree
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE

// tag::init[]
fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> =
    when(tree) {
        is Branch -> {
            Branch(map(tree.left, f), map(tree.right, f))
        }
        is Leaf -> {
            Leaf(f(tree.value))
        }
    }

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise27 : WordSpec({
    "tree map" should {
        "transform all leaves of a map" {
            val actual = Branch(
                Branch(Leaf(1), Leaf(2)),
                Branch(Leaf(3), Leaf(4))
            )
            val expected = Branch(
                Branch(Leaf(10), Leaf(20)),
                Branch(Leaf(30), Leaf(40))
            )
            map(actual) { it * 10 } shouldBe expected
        }
    }
})



package chapter3.exercises.ex28

import chapter3.Branch
import chapter3.Leaf
import chapter3.Tree
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import utils.SOLUTION_HERE
import kotlin.math.max

// tag::init[]
fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
    when(ta) {
        is Branch -> {
            b(fold(ta.left, l, b), fold(ta.right, l, b))
        }
        is Leaf -> {
            l(ta.value)
        }
    }

fun <A> sizeF(ta: Tree<A>): Int = fold(ta, {a: A -> 1}) {a, b-> a + b + 1 }

fun maximumF(ta: Tree<Int>): Int = fold(ta, {it}) {a, b-> max(a, b) }

fun <A> depthF(ta: Tree<A>): Int =  fold(ta, {a: A -> 0}) {a, b-> max(a+1, b+1)  }

fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> =
    fold(ta, { a: A -> Leaf(f(a)) },
        { b1: Tree<B>, b2: Tree<B> -> Branch(b1, b2) })

// end::init[]

//TODO: Enable tests by removing `!` prefix
class Exercise28 : WordSpec({
    "tree fold" should {

        val tree = Branch(
            Branch(Leaf(1), Leaf(2)),
            Branch(
                Leaf(3),
                Branch(
                    Branch(Leaf(4), Leaf(5)),
                    Branch(
                        Leaf(21),
                        Branch(Leaf(7), Leaf(8))
                    )
                )
            )
        )
        "generalise size" {
            sizeF(tree) shouldBe 15
        }

        "generalise maximum" {
            maximumF(tree) shouldBe 21
        }

        "generalise depth" {
            depthF(tree) shouldBe 5
        }

        "generalise map" {
            mapF(tree) { it * 10 } shouldBe
                Branch(
                    Branch(Leaf(10), Leaf(20)),
                    Branch(
                        Leaf(30),
                        Branch(
                            Branch(Leaf(40), Leaf(50)),
                            Branch(
                                Leaf(210),
                                Branch(Leaf(70), Leaf(80))
                            )
                        )
                    )
                )
        }
    }
})











