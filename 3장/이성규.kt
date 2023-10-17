sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        fun sum(ints: List<Int>): Int =
            when (ints) {
                is Nil -> 0
                is Cons -> ints.head + sum(ints.tail)
            }
        fun product(doubles: List<Double>): Double =
            when (doubles) {
                is Nil -> 1.0
                is Cons ->
                    if (doubles.head == 0.0) 0.0
                    else doubles.head * product(doubles.tail)
            }

        fun <A> empty(): List<A> = Nil

        // 3.1
        fun <A> tail(xs: List<A>): List<A> =
            when (xs) {
                is Nil -> Nil
                is Cons -> xs.tail
            }


        // 3.2
        fun <A> setHead(xs: List<A>, x: A): List<A> =
            when (xs) {
                is Nil -> Nil
                is Cons -> Cons(x, xs.tail)
            }


        // 3.3
        fun <A> drop(l: List<A>, n: Int): List<A> =
            if (n == 0) l
            else when(l) {
                is Cons -> drop(l.tail, n - 1)
                is Nil -> Nil
            }

        // 3.4
        fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> =
            when (l) {
                is Nil -> Nil
                is Cons -> if (f(l.head)) dropWhile(l.tail, f) else l
            }

        // 3.5
        fun <A> init(l: List<A>): List<A> =
            when (l) {
                is Nil -> Nil
                is Cons ->
                    if (l.tail == Nil) Nil
                    else Cons(l.head, init(l.tail))

            }

        // 3.6
        // impossible

        // 3.8
        fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> f(xs.head, foldRight(xs.tail, z, f))
            }

        fun <A> length(xs: List<A>): Int =
            foldRight(xs, 0) { _, acc -> acc + 1 }


        // 3.9
        tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
            }

        // 3.10
        fun sumFoldLeft(ints: List<Int>): Int =
            foldLeft(ints, 0) { a, b -> a + b }

        fun productFoldLeft(dbs: List<Double>): Double =
            foldLeft(dbs, 1.0) { a, b -> a * b }

        fun <A> lengthL(xs: List<A>): Int =
            foldLeft(xs, 0) { acc, _ -> acc + 1 }

        // 3.11
        fun <A : Any> reverse(xs: List<A>): List<A> =
            foldLeft(xs, Nil) { acc: List<A>, x: A -> Cons(x, acc) }

        // 3.12
        fun <A, ReturnType> foldLeftR(xs: List<A>, acc: ReturnType, f: (ReturnType, A) -> ReturnType): ReturnType =
            foldRight(
                xs,
                { b: ReturnType -> b},
                {
                    a, g ->
                    {
                            b ->
                        g(f(b, a))
                    }
                })(acc)

        fun <A, B> foldRightL(xs: List<A>, z: B, f: (A, B) -> B): B =
            foldLeft(xs,
                { b: B -> b },
                { g, a ->
                    { b ->
                        g(f(a, b))
                    }
                })(z)

        // 3.13
        fun <A> append(a1: List<A>, a2: List<A>): List<A> =
            foldRight(a1, a2) { x, y -> Cons(x, y) }

        // 3.14
        fun <A> concat(xxs: List<List<A>>): List<A> =
            foldRight(
                xxs,
                List.empty()
            ) { xs1, xs2 ->
                append(xs1, xs2)
            }

        // 3.15
        fun increment(list: List<Int>): List<Int> =
            foldRight(
                list,
                List.empty()
            ) { i: Int, ls -> Cons(i + 1, ls)}

        // 3.16
        fun doubleToString(list: List<Double>): List<String> =
            foldRight(
                list,
                List.empty()
            ) { d, ds ->
                Cons(d.toString(), ds)
            }

        // 3.17
        fun <A, B> map(list: List<A>, f: (A) -> B): List<B> =
            foldRight(
                list,
                List.empty()
            ) {
                x: A, xs: List<B> -> Cons(f(x), xs)
            }

        // 3.18
        fun <A> filter(list: List<A>, f: (A) -> Boolean): List<A> =
            foldRight(
                list,
                List.empty()
            ) { x: A, xs: List<A> ->
                if (f(x)) Cons(x, xs) else xs
            }

        // 3.19
        fun <A, B> flatMap2(list: List<A>, f: (A) -> List<B>): List<B> =
            foldRight(
                list,
                List.empty(),
            ) {
                x: A, xs: List<B> -> append(f(x), xs)
            }

        // 3.20
        fun <A> filter2(list: List<A>, f: (A) -> Boolean): List<A> =
            flatMap2(list) {
                x -> if (f(x)) List.of(x) else List.empty()
            }

        // 3.21
        fun add(list1: List<Int>, list2: List<Int>): List<Int> =
            when (list1) {
                is Nil -> Nil
                is Cons -> when (list2) {
                    is Nil -> Nil
                    is Cons -> Cons(list1.head + list2.head, add(list1.tail, list2.tail))
                }
            }

        // 3.22
        fun <A> zipWith(list1: List<A>, list2: List<A>, f: (A, A) -> A): List<A> =
            when (list1) {
                is Nil -> Nil
                is Cons -> when (list2) {
                    is Nil -> Nil
                    is Cons -> Cons(f(list1.head, list2.head), zipWith(list1.tail, list2.tail, f))
                }
            }

        // 3.23
        fun <A> startsWith(l1: List<A>, l2: List<A>): Boolean =
            when (l1) {
                is Nil -> l2 == Nil
                is Cons -> when (l2) {
                    is Nil -> true
                    is Cons -> if (l1.head == l2.head)
                        startsWith(l1.tail, l2.tail)
                    else false
                }

            }
        fun <A> hasSubsequence(list1: List<A>, list2: List<A>): Boolean =
            when (list1) {
                is Nil -> false
                is Cons -> if (startsWith(list1, list2)) true else hasSubsequence(list1.tail, list2)
            }

        // 3.24
        sealed class Tree<out A>

        data class Leaf<A>(val value: A) : Tree<A>()

        data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

        fun <A> size(a: Tree<A>): Int =
            when (a) {
                is Leaf -> 1
                is Branch -> size(a.left) + size(a.right) + 1
            }

        // 3.25
        fun maximum(tree: Tree<Int>): Int =
            when (tree) {
                is Leaf -> tree.value
                is Branch -> maxOf(maximum(tree.left), maximum(tree.right))
            }

        // 3.26
        fun <A> depth(tree: Tree<A>): Int =
            when (tree) {
                is Leaf -> 0
                is Branch -> maxOf(depth(tree.left), depth(tree.right)) + 1
            }

        // 3.27
        fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> =
            when (tree) {
                is Leaf -> Leaf(f(tree.value))
                is Branch -> Branch(map(tree.left, f), map(tree.right, f))
            }

        // 3.28
        fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
            when (ta) {
                is Leaf -> l(ta.value)
                is Branch -> b(fold(ta.left, l, b), fold(ta.right, l, b))
            }

        fun <A> sizeF(ta: Tree<A>): Int =
            fold(ta, { 1 }, { a, b -> a + b+ 1 })

        fun maximumF(tree: Tree<Int>): Int =
            fold(tree, { it }, { a, b -> maxOf(a, b)})

        fun <A> depthF(tree: Tree<A>): Int =
            fold(tree, { 0 }, { a, b, -> maxOf(a, b) + 1 })

        fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> =
            fold(ta, { Leaf(f(it)) },
                { b1: Tree<B>, b2: Tree<B> -> Branch(b1, b2) })
    }
}


data object Nil : List<Nothing>()

data class Cons<out A>(val head: A, val tail: List<A>): List<A>()
