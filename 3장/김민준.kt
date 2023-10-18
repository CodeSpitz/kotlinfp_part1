class Chapter3 {
    //연습문제 3.1
    fun <A> tail(xs: List<A>): List<A> = when (xs) {
        is Nil -> Nil
        is Cons -> xs.tail
    }

    //3.2
    fun <A> setHead(xs: List<A>, x: A): List<A> = when (xs) {
        is Nil -> throw IllegalStateException()
        is Cons -> Cons(x, xs.tail)
    }

    //3.3
    tailrec fun <A> drop(l: List<A>, n: Int): List<A> = when (n) {
        0 -> l
        else -> drop(tail(l), n - 1)
    }

    //3.4
    tailrec fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> =
            when (l) {
                is Cons -> if (f(l.head)) dropWhile(l.tail, f) else l
                is Nil -> l
            }

    //3.5
    fun <A> init(l: List<A>): List<A> = when (l) {
        is Nil -> throw IllegalStateException()
        is Cons -> if (l.tail == Nil) Nil else Cons(l.head, init(l.tail))
    }

    //3.6
    //3.7
    //3.8
    fun <A> length(xs: List<A>): Int =
            foldRight(xs, 0) { _, b -> b + 1 }

    //3.9
    tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B = when (xs) {
        is Nil -> z
        is Cons -> foldLeft(xs.tail, f(z, xs.head), f);
    }

    //3.10
    fun foldLeftSum(xs: List<Int>): Int =
            foldLeft(xs, 0) { x, y -> x + y }

    fun foldLeftProduct(xs: List<Int>): Int =
            foldLeft(xs, 1) { x, y -> x * y }

    fun <A> funLeftLength(xs: List<A>): Int =
            foldLeft(xs, 0) { x, _ -> x + 1 }

    //3.11
    fun <A> reverse(xs: List<A>): List<A> =
            foldLeft(xs, List.empty()) { x, y -> Cons(y, x) }

    //3.12
    //3.13
    fun <A> append(xs1: List<A>, xs2: List<A>): List<A> =
            foldRight(xs1, xs2) { x, y -> Cons(x, y) }

    //3.14
    //3.15
    fun plusOne(xs: List<Int>): List<Int> =
            foldRight(xs, List.empty()) { x, y -> Cons(x + 1, y) }

    //3.16
    fun doubleToString(xs: List<Double>): List<String> =
            foldRight(xs, List.empty()) { x, y -> Cons(x.toString(), y) }

    //3.17
    fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
            foldRight(xs, List.empty()) { x, y -> Cons(f(x), y) }

    //3.18
    fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
            foldRight(xs, List.empty()) { x, y -> if (f(x)) Cons(x, y) else y }

    //3.19 , append는 3.13에서 구현
    fun <A, B> flatMap(xs: List<A>, f: (A) -> List<B>): List<B> =
            foldRight(xs, List.empty()) { x, y -> append(f(x), y) }

    //3.20
    fun <A, B> flatMapFilter(xs: List<A>, f: (A) -> Boolean): List<A> =
            flatMap(xs) { x -> if (f(x)) List.of(x) else List.empty() }

    //3.21, 3.22(zipWith)
    fun <A> addSameIndexElements(xs1: List<A>, xs2: List<A>): List<A> =
            when (xs1) {
                is Nil -> Nil
                is Cons -> when (xs2) {
                    is Nil -> Nil
                    is Cons -> Cons(xs1.head + xs2.head, addSameIndexElements(xs1.tail, xs2.tail))
                }
            }

    //3.23
    //3.24
    fun <A> size(t: Tree<A>): Int =
            when (t) {
                is Leaf -> 1
                is Branch -> 1 + size(t.left) + size(t.right)
            }

    //3.25
    fun maximum(t: Tree<Int>): Int =
            when (t) {
                is Leaf -> t.value
                is Branch -> maxOf(t.left, t.right)
            }

    //3.26
    fun depth(t: Tree<Int>): Int =
            when (t) {
                is Leaf -> 1
                is Branch -> 1 + maxOf(depth(t.left), depth(t.right))
            }

    //3.27
    fun <A, B> map(t: Tree<A>, f: (A) -> B): Tree<B> = when (t) {
        is Leaf -> Leaf(f(t.value))
        is Branch -> Branch(map(t.left, f), map(t.right, f))
    }


    //도움함수
    fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> f(xs.head, foldRight(xs.tail, z, f))
            }
}
