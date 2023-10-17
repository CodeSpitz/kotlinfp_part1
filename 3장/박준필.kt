package chapter03

// 데이터 구조를 정의
sealed class List<out A> {
    companion object {
        fun <A> empty(): List<A> = Nil

        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        fun sum(ints: List<Int>): Int = foldRight(ints, 0) { a, b -> a + b }

        fun product(xs: List<Double>): Double = foldRight(xs, 1.0) { a, b ->
            println("$a * $b = ${a*b}")
            a * b
        }

        fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> f(xs.head, foldRight(xs.tail, z, f))
            }



        fun <A> print(v: List<A>) {
            when (v) {
                is Nil -> println("Nil")
                is Cons -> {
                    print(v.head)
                    print(", ")
                    print(v.tail)
                }
            }
        }
    }
}

object Nil : List<Nothing>()
data class Cons<out A>(val head: A, val tail: List<A>) : List<A>()

sealed class Tree<out A>

data class Leaf<A>(val value: A): Tree<A>()

data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

// 연습문제 3.1
fun <A> tail(xs: List<A>): List<A> {
    return when (xs) {
        is Nil -> Nil
        is Cons -> xs.tail
    }
}

// 연습문제 3.2
fun <A> setHead(xs: List<A>, x: A): List<A> {
    return when(xs) {
        is Nil -> Nil
        is Cons -> Cons(x, xs.tail)
    }
}

// 연습문제 3.3
fun <A> drop(l: List<A>, n: Int): List<A> {
    return when(l) {
        is Nil -> Nil
        is Cons -> {
            if(n == 0) l
            else drop(l.tail, n - 1)
        }
    }
}

// 연습문제 3.4
fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> {
    return when(l) {
        is Nil -> Nil
        is Cons -> {
            return if(f(l.head)) {
                return when(l.tail) {
                    is Nil -> Nil
                    is Cons -> Cons(l.tail.head, dropWhile(l.tail.tail, f))
                }
            } else {
                Cons(l.head, dropWhile(l.tail, f))
            }
        }
    }
}

// 연습문제 3.5
fun <A> init(l: List<A>): List<A> =
    when(l) {
        is Nil -> Nil
        is Cons -> {
            when(l.tail) {
                is Nil -> Nil
                is Cons -> Cons(l.head, init(l.tail))
            }
        }
    }

// 연습문제 3.6
// foldRight로 구현된 product가 리스트 원소로 0.0을 만나면 재귀를 즉시 중단하고 결과를 돌려줄 수 있는가?
// 즉시 결과를 돌려줄 수 있거나 돌려줄 수 없는 이유는 무엇인가?
// 긴 리스트에 대해 쇼트 서킷을 제공할 수 있으면 어떤 장점이 있을지 생각해보라.
// 5장에서는 이 질문이 내포하고 있는 의미를 더 자세히 살펴본다.


// 내답: 현재 foldRight 로는 재귀를 중단할 수 없다. foldRight 의 f 함수는 호출만 될 뿐 중단 조건을 컨트롤 할 수 없기 때문이다.
// 책 해답: 불가능하지 않다! f 함수를 호출하기 전에 함수에 전달할 인자를 평가하는데,
// FoldRight의 경우 이 말은 리스트를 맨 마지막까지 순회한다는 뜻이다.
// 이른 중단을 지원하려면 엄격하 지 않은 평가가 필요하다. 5장에서 이에 대해 다룬다.

// 연습문제 3.7
// 다음과 같이 Nil과 Cons를 foldRight에 넘길 때 각각 어떤 일이 벌어지는지 살펴보라
// (여기서는 Nil as List <lnt>라고 타입을 명시해야한다. 그렇지 않으면 코틀린이 foldRight의 B 타입 파라미터를
// List<Nothing>으로 추론한다.)
//   foldRight(
//       Cons(1, Cons(2, Cons(3, Nil))),
//       Nil as List<Int>,
//           {x, y -> Cons(x,y)}
//   )
// 이 결과가 foldRight와 List 데이터 생성자 사이에 존재하는 관계를 어떻게 보여주는지와 관련해 여러분의 생각을 말하라.
//val trace = {
//    foldRight (List.oF(1, 2, 3), 2, f)
//    Cons(1, foldRIght(List. of(2, 3), 2, f))
//    Cons(1, Cons(2, foldRight(List.of(3), 2, f)))
//    Cons(1, Cons(2, Cons(3, foldRight (List.empty(), 2, f))\)
//    Cons(1, Cons(2, Cons(3, NiD)))
//}

// 연습문제 3.8
fun <A> length(xs: List<A>): Int =
    List.foldRight<A, Int>(xs, 0) { _, b ->
        b + 1
    }

// 연습문제 3.9
tailrec fun <A, B> foldLeft (xs: List <A>, z: B, f: (B, A) -> B): B =
    when (xs) {
        is Nil -> z
        is Cons -> foldLeft (xs. tail, f (z, xs.head), f)
    }

// 연습문제 3.10
fun sumFoldLeft(ints: List<Int>): Int = foldLeft(ints, 0) { b, a -> b + a }
fun productFoldLeft(dbs: List<Double>): Double = foldLeft(dbs, 1.0) { b, a -> b * a }
fun <A> lengthFoldLeft(xs: List<A>): Int = foldLeft(xs, 0) { b, _ -> b + 1 }

// 연습문제 3.11
fun <A> reverse(xs: List<A>): List<A> = foldLeft(xs, Nil as List<A>) { b, a -> Cons(a, b) }

// 연습문제 3.12
fun <A, B> foldLeft2(xs: List<A>, z: B, f: (B, A) -> B): B =
    List.foldRight(reverse(xs), z) { a, b -> f(b, a) }

// 연습문제 3.13
fun <A> appendFoldRight(a1: List<A>, a2: List<A>): List<A> =
    List.foldRight(a1, a2) { a, b -> Cons(a, b) }
fun <A> appendFoldLeft(a1: List<A>, a2: List<A>): List<A> = foldLeft(reverse(a1), a2) { b, a -> Cons(a, b) }


// 연습문제 3.14
fun <A> concat(xs: List<List<A>>): List<A> =
    List.foldRight(xs, Nil as List<A>) { a, b -> appendFoldRight(a, b) }

// 연습문제 3.15
fun addOne(xs: List<Int>): List<Int> =
    List.foldRight(xs, Nil as List<Int>) { a, b -> Cons(a + 1, b) }

// 연습문제 3.16
fun doubleToString(xs: List<Double>): List<String> =
    List.foldRight(xs, Nil as List<String>) { a, b -> Cons(a.toString(), b) }

// 연습문제 3.17
fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
    List.foldRight(xs, Nil as List<B>) { a, b -> Cons(f(a), b) }

// 연습문제 3.18
fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
    List.foldRight(xs, Nil as List<A>) { a, b -> if (f(a)) Cons(a, b) else b }

// 연습문제 3.19
fun <A, B> flatMap(xs: List<A>, f: (A) -> List<B>): List<B> = concat(map(xs, f))


// 연습문제 3.20
fun <A> filterFlatMap(xs: List<A>, f: (A) -> Boolean): List<A> = flatMap(xs) { if (f(it)) List.of(it) else Nil }

// 연습문제 3.21
fun addSameIndex(a1: List<Int>, a2: List<Int>): List<Int> = when (a1) {
    Nil -> Nil
    is Cons -> when (a2) {
        Nil -> Nil
        is Cons -> Cons(a1.head + a2.head, addSameIndex(a1.tail, a2.tail))
    }
}

// 연습문제 3.22
fun <A> zipWith(xa: List<A>, xb: List<A>, f: (A, A) -> A): List<A> =
    when (xa) {
        is Nil -> Nil
        is Cons -> when (xb) {
            is Nil -> Nil
            is Cons -> Cons(f(xa.head, xb.head), zipWith(xa.tail, xb.tail, f))
        }
    }

// 연습문제 3.23
tailrec fun <A> startsWith(target: List<A>, sub: List<A>): Boolean =
    when (target) {
        is Nil -> sub == Nil
        is Cons -> when (sub) {
            is Nil -> true
            is Cons ->
                if (target.head == sub.head) startsWith(target.tail, sub.tail)
                else false
        }
    }

tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean =
    when (xs) {
        is Nil -> false
        is Cons -> {
            if (startsWith(xs, sub)) true
            else hasSubsequence(xs.tail, sub)
        }
    }

// 연습문제 3.24
fun <A> Tree<A>.size(): Int = when (this) {
    is Leaf -> 1
    is Branch -> 1 + this.left.size() + this.right.size()
}

// 연습문제 3.25
fun Tree<Int>.maximum(): Int = when (this) {
    is Leaf -> value
    is Branch -> left.maxOf(right)
}

fun Tree<Int>.maxOf(other: Tree<Int>): Int = when (this@maxOf) {
    is Leaf -> when (other) {
        is Leaf -> maxOf(this@maxOf.value, other.value)
        is Branch -> maxOf(
            other.left.maxOf(this@maxOf),
            other.right.maxOf(this@maxOf)
        )
    }
    is Branch -> maxOf(
        this@maxOf.left.maxOf(other),
        this@maxOf.right.maxOf(other)
    )
}

// 연습문제 3.26
fun <A> Tree<A>.depth(): Int = when (this) {
    is Leaf -> 1
    is Branch -> maxOf(this.left.size(), this.right.size())
}

// 연습문제 3.27
fun <A, B> Tree<A>.map(f: (A) -> B): Tree<B> = when(this) {
    is Leaf -> Leaf(f(value))
    is Branch -> Branch(
        left = left.map(f),
        right = right.map(f),
    )
}

// 연습문제 3.28
fun <A, B> Tree<A>.fold(f: (A) -> B, b: (B, B) -> B): B = when (this) {
    is Leaf -> f(value)
    is Branch -> b(left.fold(f, b), right.fold(f, b))
}

fun <A> Tree<A>.sizeF(): Int = fold(
    f = { 1 }
) { acc1, acc2 -> 1 + acc1 + acc2 }

fun Tree<Int>.maximumF(): Int = fold(
    f = { a -> a }
) { a, b -> maxOf(a, b) }

fun <A> Tree<A>.depthF(): Int = fold(
    f = { _ -> 1 }
) { a, b -> 1 + maxOf(a, b) }

fun <A, B> Tree<A>.mapF(f: (A) -> B): Tree<B> = fold(f = { Leaf(f(it)) } ) { lb: Tree<B>, rb: Tree<B> -> Branch(lb, rb) }
