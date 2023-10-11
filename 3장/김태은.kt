// 3.1 - tail
fun <T> List<T>.tail(): List<T> =
    when (this) {
        is Cons -> this.tail
        is Nil -> TODO()
    }

// 3.2 - setHead
fun <T> List<T>.setHead(x: T): List<T> =
    when (this) {
        is Cons ->
            println(this.tail)

        Cons(x, this.tail)
                is Nil -> TODO()
    }

// 3.3 - drop
fun <T> List<T>.drop(n: Int): List<T> =
    if (n <= 0) this
    else {
        when (this) {
            is Cons -> this.tail.drop(n - 1)
            is Nil -> TODO()
        }
    }

// 3.4 - dropWhile
fun <T> dropWhile(list: List<T>, function: (T) -> Boolean): List<T> =
    when (list) {
        is Cons ->
            if (function(list.head)) dropWhile(list.tail, function)
            else list

        is Nil -> list
    }

// 3.5 - init
fun <T> init(list: List<T>): List<T> =
    when (list) {
        is Cons ->
            if (list.tail() == Nil) Nil
            else Cons(list.head, init(list.tail))

        is Nil -> TODO()
    }

// 3.6
답안에는 가능하다고 되어있는데 어떤 방식으로 되는지 잘 모르겠습니다.

// 3.7
// foldRight(
//      Cons(1, Cons(2, Cons(3, Nil))),
//      Nil as List<Int>,
//      { x, y -> Cons(x, y) }
// )
=> Cons(1, foldRight(
Cons(2, Cons(3, Nil)),
Nil as List<Int>,
{ x, y -> Cons(x, y) }
))
=> Cons(1, Cons(2, foldRight(
Cons(3, Nil),
Nil as List<Int>,
{ x, y -> Cons(x, y) }
)))
=> Cons(1, Cons(2, Cons(3, foldright(
Nil,
Nil as List<Int>,
{ x, y -> Cons(x, y) }
)
=> Cons(1, Cons(2, Cons(3, Nil)))

// 3.8 - length using foldRight
fun <T> length(list: List<T>): Int =
    when (list) {
        is Nil -> 0
        is Cons -> foldRight(list, 1) { _, count -> count + 1 }
    }

// 3.9 - foldLeft using tailrec
tailrec fun <TYPE, RETURN> foldLeft(
    list: List<TYPE>,
    z: RETURN,
    function: (RETURN, TYPE) -> RETURN
): RETURN =
    when (list) {
        is Nil -> z
        is Cons -> foldLeft(list.tail, function(z, list.head), function)
    }

// 3.10 - sum, product, length using foldLeft
fun sumWithFoldLeft(list: List<Int>): Int =
    foldLeft(list, 0, { sum, element -> sum + element })

fun productWithFoldLeft(list: List<Int>): Int =
    foldLeft(list, 1, { product, element -> product * element })

fun <T> lengthWithFoldLeft(list: List<T>): Int =
    when (list) {
        is Nil -> 0
        is Cons -> foldLeft(list.tail, 1, { count, _ -> count + 1 })
    }

// 3.11 - reverse using fold
fun <T> reverse(list: List<T>): List<T> =
    foldLeft(list, of(), { prev, next -> Cons(next, prev) })

// 3.12 - foldLeft using foldRight? foldRight using foldLeft?
fun <TYPE, RETURN> foldLeftWithFoldRight(
    list: List<TYPE>,
    initial: RETURN,
    function: (RETURN, TYPE) -> RETURN
): List<RETURN> =
    foldRight(list, { it }, { })

// 3.13 - append using foldLeft or foldRight
fun <T> append(list: List<T>, elementToAppend: T): List<T> =
    when (list) {
        is Nil -> of(elementToAppend)
        is Cons -> foldRight(
            list,
            Cons(elementToAppend, Nil),
            { head, tail -> Cons(head, tail) }
        )
    }

// 3.14 - concat
fun <T> concat(list: List<List<T>>): List<T> =
    foldRight(list, empty(), { list1, list2 -> foldRight(list1, list2, { head, tail -> Cons(head, tail) }) }))

// 3.15 - plus one
fun plusOne(list: List<Int>): List<Int> =
    foldRight(list, of<Int>(), { head, tail -> Cons(head + 1, tail) })

// 3.16 - double to string
fun doubleToString(list: List<Double>): List<String> =
    foldRight(list, empty<String>(), { head, tail -> Cons(head.toString(), tail) })

// 3.17 - map
fun <TYPE, RETURN> map(list: List<TYPE>, function: (TYPE) -> RETURN): List<RETURN> =
    foldRight(list, empty(), { head, tail -> Cons(function(head), tail) })

// 3.18 - filter
fun <T> filter(list: List<T>, function: (T) -> Boolean): List<T> =
    foldRight(
        list,
        empty(),
        { head, tail -> if (function(head)) Cons(head, tail) else tail }
    )

// 3.19 - flatMap
fun <TYPE, RETURN> flatMap(list: List<TYPE>, function: (TYPE) -> List<RETURN>): List<RETURN> =
    foldRight(
        list,
        empty(),
        { type, returnType -> foldRight(function(type), returnType, { head, tail -> Cons(head, tail) }) }
    )

// 3.20 - filterWithFlatMap
fun <T> filter2(list: List<T>, function: (T) -> Boolean): List<T> =
    flatMap(list, { t -> if (f(t)) of(t) else empty() })

// 3.21 - combine
fun add(list1: List<Int>, list2: List<Int>): List<Int>) =
when (list1) {
    is Nil -> Nil
    is Cons -> when (list2) {
        is Nil -> nil
        is Cons -> Cons(list1.head+list2.head, add(list1.tail, list2.tail))
    }
}

// 3.22 - zipWith
fun <T> zipWith(list1: List<T>, list2: List<T>, function: (T, T) -> T): List<T> =
    when (list1) {
        is Nil -> Nil
        is Cons -> when (list2) {
            is Nil -> Nil
            is Cons -> Cons(f(list1.head, list2.head), zipWith(list1.tail, list2.tail))
        }
    }

// 3.23 - hasSubsequence
tailrec fun <T> startsWith(list1: List<T>, list2: List<T>): Boolean =
    when (list1) {
        is Nil -> list2 == Nil
        is Cons -> when(list2) {
            is Nil -> true
            is Cons -> if (list1.head == list2.head) startsWith(list1.tail, list2.tail) else false
        }
    }

tailrec fun <T> hasSubsequence(list: List<T>, sub: List<T>): Boolean =
    when (list) {
        is Nil -> false
        is Cons -> if (startsWith(list, sub)) true else hasSubsequence(list.tail, sub)
    }

// 3.24 - size
fun <T> size(tree: Tree<T>): Int =
    when (tree) {
        is Leaf -> 1
        is Branch -> 1 + size(tree.left) + size(tree.right)
    }

// 3.25 - maximum
fun <T> maximum(tree: Tree<T>): Int =
    when (tree) {
        is Leaf -> tree.value
        is Branch -> maxOf(maximum(tree.left), maximum(tree.right))
    }

// 3.26 - depth
fun <T> depth(tree: Tree<T>): Int =
    when (tree) {
        is Leaf -> 1
        is Branch -> 1 + maxOf(depth(tree.left), depth(tree.right))
    }

// 3.27 - map
fun <TYPE, RETURN> map(tree: Tree<TYPE>, function: (TYPE) -> RETURN): Tree<RETURN> =
    when (tree) {
        is Leaf -> Leaf(f(tree.value))
        is Branch -> Branch(map(tree.left, f), map(tree.right, f))
    }

// 3.28 - fold then size, maximum, depth, map with fold
fun <TYPE, RETURN> fold(tree: Tree<TYPE>, function: (TYPE) -> RETURN, combine: (RETURN, RETURN) -> RETURN): RETURN =
    when (tree) {
        is Leaf -> function(tree.value)
        is Branch -> combine(fold(tree.left, function, combine), fold(tree.right, function, combine))
    }

fun <T> sizeWithFold(tree: Tree<T>): Int =
    fold(tree, { 1 }, { left, right -> 1 + left + right })

fun <T> maximumWithFold(tree: Tree<T>): Int =
    fold(tree, { it }, { left, right -> maxOf(left, right) })

fun <T> depthWithFold(tree: Tree<T>): Int =
    fold(tree, { 0 }, { left, right -> 1 + maxOf(left, right) })

fun <TYPE, RETURN> mapWithFold(tree: Tree<TYPE>, function: (TYPE) -> RETURN): Tree<RETURN> =
    fold(tree, { Leaf(function(it)) }, { left, right -> Branch(left, right) })