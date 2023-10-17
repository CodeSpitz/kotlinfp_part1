sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
    }
}

object Nil : List<Nothing>() {
    override fun toString(): String = "Nil"
}

data class Cons<out A>(val head: A, val tail: List<A>): List<A>()

// Example 3.1
fun <A> List<A>.tail(): List<A> = when (this) {
    is Nil -> Nil
    is Cons<A> -> this.tail
}

// Example 3.2
fun <A> List<A>.setHead(x: A): List<A> = when (this) {
    is Nil -> throw UnsupportedOperationException("This operation is not supported for Nil")
    is Cons<A> -> Cons(x, this.tail)
}

// Example 3.3
tailrec fun <A> List<A>.drop(n: Int): List<A> = if (n == 0) this else this.tail().drop(n - 1)

// Example 3.4
tailrec fun <A> List<A>.dropWhile(f: (A) -> Boolean): List<A> = when (this) {
    is Nil -> Nil
    is Cons -> if (f(this.head)) this.drop(1).dropWhile(f) else this
}

// Example 3.5
fun <A> List<A>.init(): List<A> = when (this) {
    is Nil -> Nil
    is Cons<A> -> if (this.tail is Nil) Nil else Cons(this.head, this.tail.init())
}

// Example 3.6
fun <A> List<A>.append(other: List<A>): List<A> = when (this) {
    is Nil -> other
    is Cons -> Cons(head = this.head, tail = this.tail.append(other))
}

tailrec fun <TYPE, RETURN_TYPE> foldRight(
    list: kotlin.collections.List<TYPE>,
    defaultValue: RETURN_TYPE,
    f: (TYPE, RETURN_TYPE) -> RETURN_TYPE
): RETURN_TYPE = when (list) {
    is Nil -> defaultValue
    is Cons -> foldRight(list.tail, f(list.head, defaultValue), f)
}

// Example 3.7
// Cons(head=1, tail=Cons(head=2, tail=Cons(head=3, tail=Nil)))
// 서로 같으므로 치환가능 한 존재?

// Example 3.8
fun <A> length(xs: kotlin.collections.List<A>) = foldRight(
    list = xs,
    defaultValue = 0,
    f = { a, b -> b + 1 },
)

// Example 3.9
tailrec fun <TYPE, RETURN_TYPE> foldLeft(
    list: kotlin.collections.List<TYPE>,
    defaultValue: RETURN_TYPE,
    f: (RETURN_TYPE, TYPE) -> RETURN_TYPE
): RETURN_TYPE = when (list) {
    is Nil -> defaultValue
    is Cons -> foldLeft(list.tail, f(defaultValue, list.head), f)
}

// Example 3.10
fun sum(xs: kotlin.collections.List<Int>) = foldLeft(
    list = xs,
    defaultValue = 0,
    f = { a, b -> b + a }
)

fun product(abs: kotlin.collections.List<Double>) = foldLeft(
    list = abs,
    defaultValue = 1.0,
    f = { a , b -> b * a }
)

fun <A> length_(xs: kotlin.collections.List<A>) = foldRight(
    list = xs,
    defaultValue = 0,
    f = { a, b -> b + 1 },
)

// Example 3.11
fun <A> reverse(list: kotlin.collections.List<A>) = foldLeft(
    list = list,
    defaultValue = Nil as kotlin.collections.List<A>,
    f = { a, b -> Cons(b, a) }
)

// Example 3.12
fun <TYPE, RETURN_TYPE> foldLeftR(
    list: kotlin.collections.List<TYPE>,
    defaultValue: RETURN_TYPE,
    f: (RETURN_TYPE, TYPE) -> RETURN_TYPE
): RETURN_TYPE = foldRight(
    list,
    { returnType -> returnType },
    { a: TYPE, g: (RETURN_TYPE) -> RETURN_TYPE -> { b: RETURN_TYPE -> g(f(b,a))} }
)(defaultValue)

// Example 3.13
fun <A> kotlin.collections.List<A>.append2(other: kotlin.collections.List<A>): kotlin.collections.List<A> = foldRight(
    list = this,
    defaultValue = other,
    f = { a, r -> Cons(a, r) }
)

fun <A> empty(): kotlin.collections.List<A> = Nil

// Example 3.14
fun <A> concat(doubleList: kotlin.collections.List<kotlin.collections.List<A>>): kotlin.collections.List<A> = foldRight(
    doubleList,
    Nil as kotlin.collections.List<A>
) { outer, inner -> foldRight(outer, inner) { h, l -> Cons(h, l) } }

// Example 3.15
fun addOne(xs: kotlin.collections.List<Int>) = foldRight<Int, kotlin.collections.List<Int>>(
    list = xs,
    defaultValue = empty(),
    f = { a, b -> Cons(a + 1, b) }
)

// Example 3.16
fun doubleToString(xs: kotlin.collections.List<Double>) = foldRight<Double, kotlin.collections.List<String>>(
    list = xs,
    defaultValue = empty(),
    f = { a, b -> Cons(a.toString(), b) }
)

// Example 3.17
fun <A, TO_BE> map(
    list: kotlin.collections.List<A>,
    f: (A) -> TO_BE,
) = foldRight<A, kotlin.collections.List<TO_BE>>(
    list = list,
    defaultValue = empty()
) { a, b -> Cons(f(a), b) }

// Example 3.19
fun <A, TO_BE> flatMap(
    list: kotlin.collections.List<A>,
    f: (A) -> kotlin.collections.List<TO_BE>,
) = foldRight<A, kotlin.collections.List<TO_BE>>(
    list = list,
    defaultValue = empty()
) { a, b -> f(a).append2(b) }

// Example 3.20
fun <A> kotlin.collections.List<A>.filter2(f: (A) -> Boolean): kotlin.collections.List<A> = flatMap(
    list = this,
) { a -> if(f(a)) kotlin.collections.List.of(a) else empty() }

// Example 3.21
fun kotlin.collections.List<Int>.add(other: kotlin.collections.List<Int>): kotlin.collections.List<Int> = zipWith(other) { a, b -> a + b }

// Example 3.22
fun <A> kotlin.collections.List<A>.zipWith(other: kotlin.collections.List<A>, f: (A, A) -> A): kotlin.collections.List<A> = when(this) {
    is Nil -> Nil
    is Cons -> when(other) {
        is Nil -> Nil
        is Cons -> Cons(f(this.head, other.head), this.tail.zipWith(other.tail, f))
    }
}

sealed class Tree<out A>

data class Leaf<A>(val value: A): Tree<A>()

data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

// Example 3.24
fun <A> Tree<A>.size(): Int = when (this) {
    is Leaf -> 1
    is Branch -> 1 + this.left.size() + this.right.size()
}

// Example 3.25
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

// Example 3.26
fun <A> Tree<A>.depth(): Int = when (this) {
    is Leaf -> 1
    is Branch -> maxOf(this.left.size(), this.right.size())
}

// Example 3.27
fun <A, B> Tree<A>.map(f: (A) -> B): Tree<B> = when(this) {
    is Leaf -> Leaf(f(value))
    is Branch -> Branch(
        left = left.map(f),
        right = right.map(f),
    )
}

// Example 3.28
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