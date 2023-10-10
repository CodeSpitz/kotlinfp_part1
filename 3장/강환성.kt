// 데이터 타입에 대한 봉인된 정의
sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail= aa.sliceArray(1 until aa.size)
            return if(aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        // 빈 리스트를 표현하기 위한 메소드
        fun <A> empty(): List<A> = Nil
    }
}
// List의 Nil(빈 리스트)구현
data object Nil: List<Nothing>()
// Cons도 List를 구현함
data class Cons<out A>(val head: A, val tail: List<A>): List<A>() {
    override fun toString(): String {
        fun <A> go(next: List<A>, b: String): String =
            when(next) {
                is Cons-> go(next.tail, b+ ", ${next.head}")
                else -> b
            }
        return go(tail, "$head")
    }
}


// 연습문제 3.1
fun <A> tail(xs: List<A>): List<A> =
    when(xs) {
        is Nil-> Nil
        is Cons-> xs.tail
    }

// 연습문제 3.2
fun <A> setHead(xs: List<A>, x: A): List<A> =
    when(xs) {
        is Nil-> Nil
        is Cons-> Cons(x, xs.tail)
    }

// 연습문제 3.3
fun <A> drop(l: List<A>, n: Int): List<A> {
    return if(n == 0) l
    else when(l) {
        is Nil-> Nil
        is Cons-> drop(l.tail, n - 1)
    }
}

// 연습문제 3.4
// 문제: 주어진 술어를 만족 하는 연속 적인 원소를 삭제
//      만족 하는 값을 연속 적으로 삭제 하려면 dropWhile 결과를 tail로 하는 리스트를 반환 해야 하지 않나?
fun <A> dropWhile(l: List<A>, f: (A)-> Boolean): List<A> =
    when(l) {
        is Cons->
            if(f(l.head)) dropWhile(l.tail, f)
            else Cons(l.head, dropWhile(l.tail, f))
        is Nil-> l
    }

/**
 * 리스트 3.9 한 리스트의 모든 원소를 다른 리스트 뒤에 덧붙이기
 */
fun <A> append(a1: List<A>, a2: List<A>): List<A> =
    when(a1) {
        is Cons-> Cons(a1.head, append(a1.tail, a2))
        is Nil-> a2
    }

// 연습문제 3.5
fun <A> init(l: List<A>): List<A> =
    when(l) {
        is Nil-> Nil
        is Cons->
            if(l.tail == Nil) Nil
            else Cons(l.head, init(l.tail))
    }

/**
 * 리스트 3.10
 * 쇼트 서킷을 제거함으로써 product 정규화하기
 */
fun sum(xs: List<Int>): Int = when(xs) {
    is Cons-> xs.head + sum(xs.tail)
    is Nil-> 0
}
fun product(xs: List<Double>): Double= when(xs) {
    is Cons-> xs.head * product(xs.tail)
    is Nil-> 1.0
}

/**
 * 리스트 3.11
 * foldRight를 사용해 product와 sum 일반화하기
 */
fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B)-> B): B= when(xs) {
    is Nil-> z
    is Cons-> f(xs.head, foldRight(xs.tail, z, f))
}

// 연습문제 3.6
fun <A, B> foldRight3(xs: List<A>, z: B, f: (A, B)-> B): B= when(xs) {
    is Nil-> z
    is Cons-> {
        if(xs.head is Double) {
            val v: Double= xs.head;
            if(v == 0.0) z
            else {
                f(xs.head, foldRight3(xs.tail, z, f))
            }
        } else {
            f(xs.head, foldRight3(xs.tail, z, f))
        }
    }
}
fun product3(dbs: List<Double>)= foldRight3(dbs, 1.0) { a, b -> a * b }

// 연습문제 3.7
// 교제 풀이: 함수 f를 Cons로 치환 할 수 있다.
// 책(45p): 1.3 참조 투명성, 순수성, 치환 모델
// 치환 모델
// 식이 참조 투명하면 계산 과정이 대수 방정식을 풀 때와 마찬가지로 진행된다고 생각할 수 있다.
// 우리는 방정식의 각 부분을 완전히 전개시킨 후, 모든 변수를 그 변수가 가리키는 대상으로 치환하고,
// 다시 가장 단순한 형태로 묶는다.

// "foldRight가 f 함수를 통해 Cons(x, y)를 반환 하기 때문에 foldRight는 Cons(x, y)로 치환 할 수 있다" 로 해석 하면 되나?
// foldRight(p1, p2, f)= Cons(p1.head, foldRight(p1.tail, p2, f)) 의 관계가 성립 하기 때문에
// foldRight를 계속 Cons로 치환 가능 하다...
// --> foldRight(List.of(1), List.of(2), f)

// List.of(1) = Cons(1, Nil)
// List.of(2) = Cons(2, Nil)
// --> foldRight(Cons(1, Nil), Cons(2, Nil), f)

// foldRight(p1, p2, f) = Cons(p1.head, foldRight(p1.tail, p2, f))
// --> Cons(1, foldRight(Nil, Cons(2, Nil), f))

// foldRight(p1, p2, f) = Cons(p1.head, foldRight(p1.tail, p2, f))
// --> Cons(1, Cons(2, Nil))

// 연습문제 3.8
fun <A> length(xs: List<A>): Int = foldRight(xs, 0, { x, y -> y + 1})

// 연습문제 3.9
tailrec fun  <A, B> foldLeft(l: List<A>, z: B, f: (A, B)-> B): B =
    when(l) {
        is Nil-> z
        is Cons-> foldLeft(l.tail, f(l.head, z), f)
    }

// 연습문제 3.10
fun sumLeft(l: List<Int>) = foldLeft(l, 0, { x, y -> x + y })
fun productLeft(l: List<Double>) = foldLeft(l, 1.0, { x, y -> x * y })
fun <A> lengthLeft(l: List<A>) = foldLeft(l, 0, { x, y -> y + 1 })

// 연습문제 3.11
fun <A> reverse(l: List<A>): List<A> = foldLeft(l, List.empty(), { x, y -> Cons(x, y) })

// 연습문제 3.12

// 연습문제 3.13
fun <A> appendLeft(l1: List<A>, l2: List<A>): List<A> = foldLeft(reverse(l1), l2, { x, y -> Cons(x, y) })
fun <A> appendRight(l1: List<A>, l2: List<A>): List<A> = foldRight(l1, l2, { x, y -> Cons(x, y) })

// 연습문제 3.14
fun <A> concatRight(ll: List<List<A>>): List<A> =
    foldRight(ll, List.empty(), { list, nil ->
        foldRight(list, nil, { x, y -> Cons(x, y) })
    })
fun <A> concatLeft(ll: List<List<A>>): List<A> =
    foldLeft(reverse(ll), List.empty(), { list, nil ->
        foldRight(list, nil, { x, y -> Cons(x, y) })
    })

// 연습문제 3.15
fun increment(l: List<Int>): List<Int> = foldRight(l, List.empty(), { x, y -> Cons(x+1, y) })

// 연습문제 3.16
fun doubleToString(l: List<Double>): List<String> = foldRight(l, List.empty(), { x, y -> Cons(x.toString(), y) })

// 연습문제 3.17
fun <A, B> map(l: List<A>, f: (A)-> B): List<B> = foldRight(l, List.empty(), { x, y -> Cons(f(x), y) })

// 연습문제 3.18
fun <A> filter(l: List<A>, f: (A)-> Boolean): List<A> = foldRight(l, List.empty(), { x, y -> if(f(x)) Cons(x, y) else y })

// 연습문제 3.19
fun <A, B> flatMap(l: List<A>, f: (A)-> List<B>): List<B> = foldRight(l, List.empty()) { x, y -> append(f(x), y) }

// 연습문제 3.20
fun <A> filterMap(l: List<A>, f: (A) -> Boolean): List<A> = flatMap(l) { a -> if(f(a)) List.of(a) else List.empty() }

// 연습문제 3.21
fun add(a: List<Int>, b: List<Int>): List<Int> =
    when(a) {
        is Nil-> Nil
        is Cons-> when(b) {
            is Nil-> Cons(a.head, add(a.tail, Nil))
            is Cons-> Cons(a.head + b.head, add(a.tail, b.tail))
        }
    }

// 연습문제 3.22
fun <A> zipWith(a: List<A>, b: List<A>, f: (A, A) -> A): List<A> =
    when(a) {
        is Nil-> Nil
        is Cons-> {
            when(b) {
                is Nil-> Cons(a.head, zipWith(a.tail, Nil, f))
                is Cons-> Cons(f(a.head, b.head), zipWith(a.tail, b.tail, f))
            }
        }
    }

// 연습문제 3.23
tailrec fun <A> startsWith(a: List<A>, b: List<A>): Boolean =
    when(a) {
        is Nil-> b == Nil
        is Cons-> when(b) {
            is Nil-> true
            is Cons->
                if(a.head == b.head) startsWith(a.tail, b.tail)
                else false
        }
    }
tailrec fun <A> hasSubsequence(a: List<A>, b: List<A>): Boolean =
    when(a) {
        is Nil-> false
        is Cons->
            if(startsWith(a, b)) true
            else hasSubsequence(a.tail, b)
    }

sealed class Tree<out A> {
    companion object {
        fun <A> of(a: A, vararg aa: A): Tree<A> {
            val tail= aa.sliceArray(1 until aa.size)
            return when(aa.size) {
                0-> Leaf(a)
                else-> Branch(Leaf(a), of(aa[0], *tail))
            }
        }
    }
}
data class Leaf<A>(val value: A): Tree<A>()
data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

// 연습문제 3.24
fun <A> size(t: Tree<A>): Int =
    when(t) {
        is Leaf-> 1
        is Branch-> 1 + size(t.left) + size(t.right)
    }

// 연습문제 3.25
fun maximum(t: Tree<Int>): Int =
    when(t) {
        is Leaf-> t.value
        is Branch-> maxOf(maximum(t.left), maximum(t.right))
    }

// 연습문제 3.26
fun depth(t: Tree<Int>): Int =
    when(t) {
        is Leaf-> 0
        is Branch-> 1 + maxOf(depth(t.left), depth(t.right))
    }

// 연습문제 3.27
fun <A, B> map(t: Tree<A>, f: (A)-> B): Tree<B> =
    when(t) {
        is Leaf-> Leaf(f(t.value))
        is Branch-> Branch(map(t.left, f), map(t.right, f))
    }

// 연습문제 3.28
fun <A, B> fold(t: Tree<A>, l: (A)-> B, b: (B, B)-> B): B =
    when(t) {
        is Leaf-> l(t.value)
        is Branch-> b(fold(t.left, l, b), fold(t.right, l, b))
    }
fun <A> sizeF(t: Tree<A>): Int = fold(t, { 1 }, { x, y -> 1 + x + y })
fun  maximumF(t: Tree<Int>): Int = fold(t, { a -> a }) { x, y -> maxOf(x, y) }
fun <A> depthF(t: Tree<A>): Int = fold(t, { 0 }) { x, y -> 1 + maxOf(x, y) }
fun <A, B> mapF(t: Tree<A>, f: (A)-> B): Tree<B> =
    fold(t, { a: A -> Leaf(f(a))}) { x: Tree<B>, y: Tree<B> -> Branch(x, y) }

fun main() {
    val list1= List.of(1, 2, 0, 3, 4)
    val list2= List.of(1.0, 2.0, 0.0, 3.0, 4.0)
    val tree = Branch(Leaf(1), Branch(Leaf(2), Leaf(3)))
    println("3. 함수형 데이터 구조")
    println("list1: $list1")
    println("list2: $list2")
    println("tree1: $tree")
    println("tree2: "+ Tree.of(1, 2, 3))
    println("==========")

    println("3.1: "+ tail(list1))
    println("3.2: "+ setHead(list1, 10))
    println("3.3: "+ drop(list1, 2))
    println("3.4: "+ dropWhile(list1) { it % 2 == 0 })
    println("3.5: "+ init(list1))
    println("3.6: "+ product3(list2))

    val z= Nil as List<Int>
    val f= { x: Int, y: List<Int> -> Cons(x, y) }
    println("3.7: ")
    println("    "+ foldRight(List.of(1, 2, 3), z, f))
    println("    "+ Cons(1, foldRight(List.of(2, 3), z, f)))
    println("    "+ Cons(1, Cons(2, foldRight(List.of(2), z, f))))
    println("    "+ Cons(1, Cons(2, Cons(3, foldRight(List.empty(), z, f)))))
    println("    "+ Cons(1, Cons(2, Cons(3, Nil))))
    println("3.8: "+ length(list1))
    println("3.9: "+ foldLeft(list1, List.of(1, 2, 3), f))
    println("3.10: ${sumLeft(list1)}, ${productLeft(list2)}, ${lengthLeft(list1)}")
    println("3.11: "+ reverse(list1))
    println("3.13: "+ appendLeft(list1, List.of(11, 22, 33)))
    println("      "+ appendRight(list1, List.of(11, 22, 33)))
    println("3.14: "+ concatRight(List.of(List.of(1, 2, 3), List.of(10, 20, 30))))
    println("      "+ concatLeft(List.of(List.of(1, 2, 3), List.of(10, 20, 30))))
    println("3.15: "+ increment(list1))
    println("3.16: "+ doubleToString(list2))
    println("3.17: "+ map(list1, { x -> x + 1 }))
    println("3.18: "+ filter(list1, { x -> x < 3 }))
    println("3.19 "+ flatMap(list1, { i-> List.of(i, i) }))
    println("3.20 "+ filterMap(list1, { x -> x < 3 }))
    println("3.21 "+ add(list1, List.of(1, 2, 3)))
    println("3.22 "+ zipWith(list1, List.of(1, 2, 3)) { a, b -> a + b })
    println("3.23 "+ hasSubsequence(list1, List.of(1, 2)))

    println("3.24 "+ size(tree))
    println("3.25 "+ maximum(tree))
    println("3.26 "+ depth(tree))
    println("3.27 "+ map(tree) { v -> v + 1 })
    println("3.28 "+ fold(tree, { 1 }, { x, y -> 1 + x + y }))
    println("     "+ sizeF(tree))
    println("     "+ maximumF(tree))
    println("     "+ depthF(tree))
    println("     "+ mapF(tree) { v -> v + 1 })

    println("==========")
    println("list1: $list1")
    println("list2: $list2")
}
