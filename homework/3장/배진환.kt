
sealed interface FList<out E>{
    companion object{
        fun <E> of(vararg elements: E): FList<E> = when {
            elements.isEmpty() -> empty()
            else -> Cons(
                elements.first(),
                of(*elements.sliceArray(1 until elements.size))
            )

        }
        inline operator fun <E> invoke():FList<E> = Nil
        operator fun <E> invoke(head:E, tail:FList<E>):FList<E> = Cons(head,tail)

    }
}

@PublishedApi internal data class Cons<out E>(val head: E, val tail: FList<E>) : FList<E>
@PublishedApi internal data object Nil : FList<Nothing>

inline fun empty(): FList<Nothing> = Nil

internal val <E:Any> FList<E>.tail
    get() = when (this) {
        is Nil -> this
        is Cons -> tail
    }

internal inline fun <E:Any> FList<E>.setHead(e: E): FList<E> = when (this) {
    is Nil -> this
    is Cons -> FList(e, tail)
}

inline val <E:Any> FList<E>.isEmpty
    get():Boolean = when (this) {
        is Nil -> true
        is Cons -> false
    }

fun <E:Any> FList<E>.addFirst(e: E): FList<E> = FList(e, this)

fun sum(ints: FList<Int>): Int = when (ints) {
    is Nil -> 0
    is Cons -> ints.head + sum(ints.tail)
}

fun product(doubles: FList<Double>): Double = when (doubles) {
    is Nil -> 1.0
    is Cons -> if (doubles.head == .0) 0.0 else doubles.head * product(
        doubles.tail
    )
}

tailrec fun <E:Any> FList<E>.take(n: Int = 1): FList<E> = when(this) {
    is Cons -> if(n>0) FList(head, tail.take(n-1)) else Nil
    is Nil -> this
}
tailrec fun <E:Any> FList<E>.drop(n: Int = 1): FList<E> = when {
    isEmpty || n == 0 -> this
    else -> tail.drop(n - 1)
}

fun <E:Any> FList<E>.dropWhile(isNext: (E) -> Boolean): FList<E> = when(this){
    is Cons ->
        if (isNext(head)) tail.dropWhile(isNext) else this
    is Nil -> Nil
}

fun <E:Any> FList<E>.append(elements: FList<E>): FList<E> = when (this) {
    is Cons -> FList(head, tail.append(elements))
    is Nil -> elements
}

fun <E:Any> FList<E>._append(elements: FList<E>): FList<E> =
    reverse().foldLeft(elements) { acc, element: E -> Cons(element, acc) }

fun <E:Any> FList<E>.__append(elements: FList<E>): FList<E> =
    foldRight(elements, FList.Companion::invoke)

tailrec fun <E:Any> FList<E>.reverse(elements: FList<E> = Nil): FList<E> = when (this) {
    is Cons -> tail.reverse(FList(head, elements))
    is Nil -> elements
}

// fun <E> FList<E>._reverse(elements: FList<E> = Nil): FList<E> = fold(empty())
fun <E:Any, R:Any> FList<E>.foldRight(initial: R, f: (E, R) -> R): R = when (this) {
    is Nil -> initial
    is Cons -> f(head, tail.foldRight(initial, f))
}

tailrec fun <E:Any, R:Any> FList<E>._foldRight(acc: R,f:(E, R) -> R, block: (R)->R): R =
    when (this) {
        is Cons ->  when (tail) {
            is Cons -> tail._foldRight(acc,f){ acc-> block(f(tail.head,acc)) }
            is Nil -> block(f(head, acc))
        }
        is Nil -> acc
    }

fun <E:Any> FList<E>.length(): Int = foldRight(0) { _, prev ->
    1 + prev
}

fun <E:Any, R:Any> FList<E>.foldLeft(initial: R, f: (R, E) -> R): R = when (this) {
    is Nil -> initial
    is Cons -> tail.foldLeft(f(initial, head), f)
}

tailrec fun <E:Any, R:Any> FList<E>.fold(acc: R, block: (R, E) -> R): R =
    when (this) {
        is Nil -> acc
        is Cons -> tail.fold(block(acc, head), block)
    }

fun FList<Int>.increase() = map{ it + 1 }
fun FList<Int>.convertToString() = map{ it.toString() }

fun <E:Any, E2:Any> FList<E>.map(block:(E)->E2):FList<E2> = fold<E, FList<E2>>(FList()){ acc, item ->
    FList(block(item),acc)
}.reverse()

fun <E:Any,E2:Any> FList<E>.flatMap(block:(E) -> FList<E2>):FList<E2> =
    fold<E, FList<E2>>(empty()){ result, el ->
        result.append(block(el))
    }.reverse()

fun <E:Any> FList<E>.filter(block:(E)->Boolean): FList<E> =
    flatMap { if(block(it)) FList.of(it) else empty() }


fun <E:Any> List<E>.hasContain(e: E): Boolean =
    fold(false) { accu, curr -> accu || curr == e }
tailrec fun <E:Any> List<E>.hasSubsequence(other: List<E>): Boolean =
    when (other) {
        is Cons -> if (hasContain(other.head)) hasSubsequence(other.tail) else false
        is Nil -> true
    }

fun <E:Any, E2:Any, E3:Any> FList<E>.map2(other:FList<E2>, block: (E, E2)-> E3):FList<E3> =
    zipWith(other,block)

fun <E:Any, E2:Any, E3:Any> FList<E>.zipWith(other:FList<E2>, block:(E, E2) -> E3): FList<E3>
    = fold<E, Pair<FList<E3>, FList<E2>>>(empty() to other){(acc, other), it ->
    when(other){
        is Cons -> Cons(
            block(it, other.head),
            acc
        ) to other.tail
        is Nil -> acc to other
    }
}.first.reverse()

sealed class FTree<out E:Any> {
    companion object{
        operator fun <E:Any> invoke(left: FTree<E>, right: FTree<E>): FTree<E> = Branch(left, right)
        operator fun <E:Any> invoke(value:E): FTree<E> = Leaf(value)
    }

}
data class Leaf<E:Any>@PublishedApi internal constructor(@PublishedApi internal val item:E): FTree<E>()
data class Branch<E:Any>@PublishedApi internal constructor(@PublishedApi internal val left: FTree<E>, @PublishedApi internal val right: FTree<E>): FTree<E>()

val <E:Any> FTree<E>.size:Int get() = fold({1}){ l, r->1 + l + r}
val <E:Any> FTree<E>.depth:Int get() = fold({1}){ l, r->1 + if(l > r) l else r}
val <E> FTree<E>.max:E where E:Comparable<E>, E:Number get() = fold({it}){ l, r->if(l > r) l else r}
fun <E:Any> FTree<E>.setLeft(tree: FTree<E>): FTree<E> = when(this){
    is Leaf -> Branch(tree, this)
    is Branch -> Branch(tree, right)
}
fun <E:Any> FTree<E>.setRight(tree: FTree<E>): FTree<E> = when(this){
    is Leaf -> Branch(this, tree)
    is Branch -> Branch(left, tree)
}
fun <E:Any, E2:Any> FTree<E>.fold(leafBlock:(E)->E2, branchBlock:(l:E2, r:E2)->E2):E2 = when(this){
    is Leaf -> leafBlock(item)
    is Branch -> branchBlock(left.fold(leafBlock, branchBlock), right.fold(leafBlock, branchBlock))
}
fun <E:Any, E2:Any> FTree<E>.map(block:(E)->E2): FTree<E2> = fold({FTree(block(it))}){ l, r->FTree(l, r)}
operator fun <E:Any> FTree<E>.contains(item:E):Boolean = fold({it == item}){ l, r-> l || r}


