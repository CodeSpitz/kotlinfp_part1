sealed class Option<out A> {

    companion object {
        fun <A> of(a: A): Option<A> = Some(a)
        fun <A> empty(): Option<A> = None
    }
}

data class Some<out A>(val get: A) : Option<A>()
object None : Option<Nothing>()

// 4-1 option 에 대한 모든 함수 구현
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
    when (this) {
        is None -> None
        is Some -> Some(f(this.get))
    }

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
    when (this) {
        is None -> None
        is Some -> f(this.get)
    }

fun <A> Option<A>.getOrElse(default: () -> A): A =
    when (this) {
        is None -> default()
        is Some -> this.get
    }

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
    when (this) {
        is None -> ob()
        is Some -> this
    }

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    when (this) {
        is None -> None
        is Some -> if (f(this.get)) this else None
    }


// 4-2 variance 함수 구현 (flatMap 사용)
fun variance(xs: List<Double>): Option<Double> =
    mean(xs).flatMap {
            m -> mean(
        map(xs) { x -> Math.pow(x - m, 2.0) }
    )
    }

fun mean(xs: List<Double>): Option<Double> =
    if (isEmpty(xs)) None
    else Some(sum(xs) / size(xs))

// 4-3 map2 함수 구현
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
    a.flatMap { a1 -> b.map { b1 -> f(a1, b1) } }

// 4-4 sequence 함수 구현
fun <A> sequence(a: List<Option<A>>): Option<List<A>> =
    traverse(a) { it }

fun <A, B> traverse(xa: List<A>, f: (A) -> Option<B>): Option<List<B>> =
    when (xa) {
        is Nil -> Some(Nil)
        is Cons -> map2( f(xa.head), traverse(xa.tail, f)) { b, bs -> Cons(b, bs) }
    }

// 4-5 sequence 함수를 traverse를 이용해서 구현
fun <A> sequence2(a: List<Option<A>>): Option<List<A>> =
    traverse(a) { it }

sealed class Either<out E, out A> {
    companion object {
        fun <E, A> of(a: A): Either<E, A> = Right(a)
    }
}

data class Left<out E>(val value: E) : Either<E, Nothing>()
data class Right<out A>(val value: A) : Either<Nothing, A>()

// 4-6 Either 에 대한 모든 함수 구현  <-- f 가 항상 성공 ??
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> f(this.value)
    }

fun <E, A, B, C> map2(a: Either<E, A>, b: Either<E, B>, f: (A, B) -> C): Either<E, C> =
    a.flatMap { a1 -> b.map { b1 -> f(a1, b1) } }

fun <E, A> Either<E, A>.orElse(b: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Left -> b()
        is Right -> this
    }

// 4-7 Either 에 대한 traverse 함수 구현, 오류가 생긴 경우 최초로 발생한 오류 반환
fun <E, A, B> traverseEither(xa: List<A>, f: (A) -> Either<E, B>): Either<E, List<B>> =
    when (xa) {
        is Nil -> Right(Nil)
        is Cons -> map2( f(xa.head), traverseEither(xa.tail, f)) { b, bs -> Cons(b, bs) }
    }

// 4-7 Either 에 대한 sequence 함수 구현
fun <E, A> sequence(a: List<Either<E, A>>): Either<E, List<A>> =
    traverseEither(a) { it }


// ================================================================================
// 3장 리스트를 4장에서 사용하기 위해 복사


sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
    }
}
object Nil : List<Nothing>()
data class Cons<out A>(val head: A, val tail: List<A>) : List<A>()

fun sum(list: List<Int>): Int = when (list) {
    is Nil -> 0
    is Cons -> list.head + sum(list.tail)
}

fun sum(list: List<Double>): Double = when (list) {
    is Nil -> 0.0
    is Cons -> list.head + sum(list.tail)
}

fun <T> size(list: List<T>): Int = when (list) {
    is Nil -> 0
    is Cons -> 1 + size(list.tail)
}

fun product(list: List<Double>): Double = when (list) {
    is Nil -> 1.0
    is Cons -> if (list.head == 0.0) 0.0 else list.head * product(list.tail)
}

// 3-1 List 의 첫번째 원소를 제거하는 tail 함수 구현 - 상수시간 복잡도 O(1)
fun <A> tail(list: List<A>): List<A> = when (list) {
    is Nil -> Nil
    is Cons -> list.tail
}

// 3-2 List 의 첫 원소를 다른 값으로 대치하는 setHead 함수 구현
fun <A> setHead(list: List<A>, x: A): List<A> = when (list) {
    is Nil -> Cons(x, Nil)    // vs Nil : 대치한다는 의미를 어떻게 해석해야하지??
    is Cons -> Cons(x, list.tail)
}

// 3-3 List에서 처음 n개의 요소를 제거하는 drop 함수 구현
fun <A> drop(list: List<A>, n: Int): List<A> = when (list) {
    is Nil -> Nil
    is Cons -> when {
        (n == 0) -> list
        else -> drop(list.tail, n - 1)
    }
}

// 3-4 List에서 주어진 술어와 부합하는 요소들을 제거하는 dropWhile 함수 구현
fun <A> dropWhile(list: List<A>, f: (A) -> Boolean): List<A> = when (list) {
    is Nil -> Nil
    is Cons -> when {
        f(list.head) -> dropWhile(list.tail, f)
        else -> list
    }
}

fun <A> append(list1: List<A>, list2: List<A>): List<A> = when (list1) {
    is Nil -> list2
    is Cons -> Cons(list1.head, append(list1.tail, list2))
}

// 3-5 List의 마지막 요소를 제외한 모든 요소를 반환하는 init 함수 구현
fun <A> init(list: List<A>): List<A> = when (list) {
    is Nil -> Nil
    is Cons -> when (list.tail) {
        is Nil -> Nil
        is Cons -> Cons(list.head, init(list.tail))
    }
}

fun <A, B> foldRight(xs: List<A>, acc: B, f: (A, B) -> B): B = when (xs) {
    is Nil -> acc
    is Cons -> f(xs.head, foldRight(xs.tail, acc, f))//.also { println(it) }  // 꼬리재귀 인가 vs 아닌가???? -> 아닌듯. 마지막이 f() 이기 때문에.
}

fun <A, B> foldRight2(xs: List<A>, acc: B, f: (A, B) -> B, check: (A) -> Boolean): B = when (xs) {
    is Nil -> acc
    is Cons ->
        when (check(xs.head)) {
            true -> acc
            false -> f(xs.head, foldRight2(xs.tail, acc, f, check)).also { println(it) }
        }
}

// 3-6 foldRight 를 이용한 product 구현 - 숏서킷 가능한가??  -> 그냥은 안됨. 숏서킷 조건을 foldRight 에 param 으로 던져주면 될듯. -> 안됨. foldRight 에서는 숏서킷을 하지 않는다.
fun productByFoldRight(list: List<Double>): Double =
    foldRight(list, 1.0) {
            a, acc -> if (a == 0.0) 0.0 else (a * acc)
    }

fun productByFoldRight2(list: List<Double>): Double =
    foldRight2(list, 1.0, {
            a, acc -> if (a == 0.0) 0.0 else (a * acc)
    }) { it == 0.0}

fun <A> empty(): List<A> = Nil

fun <A> isEmpty(xs: List<A>): Boolean = when (xs) {
    is Nil -> true
    is Cons -> false
}

// 3-7

// 3-8 foldRight 를 이용한 length 구현
fun <A> length(list: List<A>): Int = foldRight(list, 0) { _, acc -> acc + 1 }

// 3-9 foldLeft 를 꼬리재귀로 구현
fun <A, B> foldLeft(xs: List<A>, acc: B, f: (B, A) -> B): B = when (xs) {
    is Nil -> acc
    is Cons -> foldLeft(xs.tail, f(acc, xs.head), f)
}

// 3-10 foldLeft 를 이용한 sum, product, length 구현
fun sumByFoldLeft(list: List<Int>): Int = foldLeft(list, 0) { acc, a -> acc + a }    // 제너릭으로는 안될까?? <T: Number> 같은걸로... -> 캐스팅 없이는 안될듯. operator 때문에...
fun productByFoldLeft(list: List<Double>): Double = foldLeft(list, 1.0) { acc, a -> acc * a } // 제너릭으로는 안될까?? <T: Number> 같은걸로... -> 캐스팅 없이는 안될듯. operator 때문에...
fun <A> lengthByFoldLeft(list: List<A>): Int = foldLeft(list, 0) { acc, _ -> acc + 1 }

// 3-11 접기연산을 활용해 reverse 구현
fun <A> reverse(list: List<A>): List<A> = foldLeft(list, empty()) { acc, a -> Cons(a, acc) }
fun <A> reverse2(list: List<A>): List<A> = foldRight(list, empty()) { a, acc -> append(acc, List.of(a)) }

// 3-12 foldLeft 를 이용해 구현한 함수를 foldRight 를 이용해 구현하기
fun <A, B> foldRightByFoldLeft(xs: List<A>, acc: B, f: (A, B) -> B): B = foldLeft(reverse(xs), acc) { acc, a -> f(a, acc) } // reverse 도 foldLeft 로 구현했으니까 꼬리재귀 그런데 루프두번인데..
fun <A, B> foldLeftByFoldRight(xs: List<A>, acc: B, f: (B, A) -> B): B = foldRight(xs, acc) { a, acc -> f(acc, a) }


// 3-13 append 를 foldLeft 나 foldRight 로 구현하기
fun <A> appendByFoldLeft(list1: List<A>, list2: List<A>): List<A> = foldLeft(reverse(list1), list2) { acc, a -> Cons(a, acc) }
fun <A> appendByFoldRight(list1: List<A>, list2: List<A>): List<A> = foldRight(list1, list2) { a, acc -> Cons(a, acc) }

// 3-14 리스트가 원소인 리스트를 단일 리스트로 연결해주는 함수 [ O(n) 복잡도]
fun <A> flatten(list: List<List<A>>): List<A> = foldLeft(list, empty()) { acc, a -> appendByFoldLeft(acc, a) }

// 3-15 정수로 리스트의 각 원소에 1더한 리스트를 반환하는 함수
fun mapAddOne(list: List<Int>): List<Int> = foldLeft(reverse(list), empty()) { acc, a -> Cons(a + 1, acc) }

// 3-16 List<Double> 을 List<String> 으로 변환하는 함수
fun mapDouble2String(list: List<Double>): List<String> = foldLeft(reverse(list), empty()) { acc, a -> Cons(a.toString(), acc) }

// 3-17 map
fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> = foldLeft(reverse(xs), empty()) { acc, a -> Cons(f(a), acc) }

// 3-18 filter
fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> = foldLeft(reverse(xs), empty()) { acc, a -> if (f(a)) Cons(a, acc) else acc }

// 3-19 flatMap
fun <A, B> flatMap(xs: List<A>, f: (A) -> List<B>): List<B> = flatten(map(xs, f))

// 3-20 filter 를 flatMap 으로 구현하기
fun <A> filterByFlatMap(xs: List<A>, f: (A) -> Boolean): List<A> = flatMap(xs) { if (f(it)) List.of(it) else empty() }

// 3-21, 22 List 두개를 받아서 해당 리스트의 각 원소를 더한 리스트를 반환하는 함수
fun <A, B, R> zipWith(xs1: List<A>, xs2: List<B>, f: (A, B) -> R): List<R> = when (xs1) {
    is Nil -> Nil
    is Cons -> when (xs2) {
        is Nil -> Nil
        is Cons -> Cons(f(xs1.head, xs2.head), zipWith(xs1.tail, xs2.tail, f))
    }
}

// 3-23 특정 리스트가 원 리스트의 부분열 인지 검사하는 hasSubsequence 함수 구현
fun <A> checkSub(xs: List<A>, sub: List<A>): Boolean = when (xs) {
    is Nil -> false
    is Cons -> when (sub) {
        is Nil -> true
        is Cons -> when {
            (xs.head == sub.head) -> checkSub(xs.tail, sub.tail)
            else -> false
        }
    }
}
fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean = when (xs) {
    is Nil -> false
    is Cons ->
        if (checkSub(xs, sub)) {
            true
        }
        else {
            hasSubsequence(xs.tail, sub)
        }
}

sealed class Tree<out A>
data class Leaf<out A>(val value: A) : Tree<A>()
data class Branch<out A>(val left: Tree<A>, val right: Tree<A>) : Tree<A>()

// 3-24 트리의 노드의 갯수 반환하는 함수
fun <A> size(tree: Tree<A>): Int = when (tree) {
    is Leaf -> 1
    is Branch -> size(tree.left) + size(tree.right) + 1
}

// 3-25 maximum 함수 구현
fun <T:Comparable<T>> maximum(tree: Tree<T>): T = when (tree) {
    is Leaf -> tree.value
    is Branch -> maxOf(maximum(tree.left), maximum(tree.right))
}

// 3-26 Tree 의 루트에서 각 리프까지 가장 긴 경로의 길이를 반환하는 함수
fun depth(tree: Tree<Int>): Int = when (tree) {
    is Leaf -> 1
    is Branch -> maxOf(depth(tree.left), depth(tree.right)) + 1
}

// 3-27 Tree 의 각 노드에 저장된 값을 변경하는 함수
fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> = when (tree) {
    is Leaf -> Leaf(f(tree.value))
    is Branch -> Branch(map(tree.left, f), map(tree.right, f))
}

// 3-28 Tree fold
fun <A, B> fold(tree: Tree<A>, f: (A) -> B, g: (B, B) -> B): B = when (tree) {
    is Leaf -> f(tree.value)
    is Branch -> g(fold(tree.left, f, g), fold(tree.right, f, g))
}
// sizeF
fun sizeF(tree: Tree<Int>): Int = fold(tree, { 1 }, { a, b -> a + b + 1 })

// maximumF
fun <T: Comparable<T>> maximumF(tree: Tree<T>): T = fold(tree, { it }, { a, b -> maxOf(a, b) })

// depthF
fun depthF(tree: Tree<Int>): Int = fold(tree, { 1 }, { a, b -> maxOf(a, b) + 1 })

// mapF  fold 를 사용한 tree map
fun <A, B> mapF(tree: Tree<A>, f: (A) -> B): Tree<B> =
    fold(tree, { Leaf(f(it)) }, { a: Tree<B>, b: Tree<B> -> Branch(a, b) })


fun main() {
    println("start main")

}