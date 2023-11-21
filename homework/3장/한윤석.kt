// ex1
fun <A> tail(xs: List<A>): List<A> = when (xs) {
  is Nil -> Nil
  is Cons -> xs.tail
}

// ex2
fun <A> setHead(xs: List<A>, x: A): List<A> = when (xs) {
  is Nil -> Nil
  is Cons -> Cons(x, xs.tail)
}

// ex3
fun <A> drop(l: List<A>, n: Int): List<A> {
  if (n <= 0) {
      return l
  }

  return when (l) {
      is Nil -> Nil
      is Cons -> drop(l.tail, n - 1)
  }
}

// ex4
fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> = when (l) {
  is Nil -> Nil
  is Cons -> if (f(l.head)) dropWhile(l.tail, f) else l
}

// ex5
fun <A> init(l: List<A>): List<A> = when (l) {
  is Nil -> Nil
  is Cons -> when (l.tail) {
      is Nil -> Nil
      is Cons -> Cons(l.head, init(l.tail))
  }
}

// ex8
fun <A> length(xs: List<A>): Int = foldRight(xs, 0) { _, acc -> acc + 1 }

// ex9
tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B =
    when (xs) {
        is Nil -> z
        is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
    }

// ex10
fun sumL(xs: List<Int>): Int = foldLeft(xs, 0) { acc, cur -> acc + cur }
fun productL(xs: List<Double>): Double = foldLeft(xs, 1.0) { acc, cur ->
    acc * cur
}

fun <A> lengthL(xs: List<A>): Int = foldLeft(xs, 0) { acc, _ -> acc + 1}

// ex11
fun <A> reverse(xs: List<A>): List<A> = foldLeft(xs, List.empty()) { acc, cur ->
  Cons(cur, acc)
}

// ex12
fun <A, B> foldLeftR(xs: List<A>, z: B, f: (B, A) -> B): B = foldRight(
    xs,
    { b: B -> b },
    { a, g ->
        { b ->
            g(f(b, a))
        }
    })(z)

fun <A, B> foldRightL(xs: List<A>, z: B, f: (A, B) -> B): B = foldLeft(xs,
    { b: B -> b },
    { g, a ->
        { b ->
            g(f(a, b))
        }
    })(z)

// ex13
fun <A> append(a1: List<A>, a2: List<A>): List<A> =
    foldRight(a1, a2) { cur, acc ->
        Cons(cur, acc)
    }

fun <A> appendL(a1: List<A>, a2: List<A>): List<A> =
    foldLeft(reverse(a1), a2) { acc, cur -> Cons(cur, acc)}

// ex14
fun <A> concat(lla: List<List<A>>): List<A> = foldLeft(
    lla, List.empty(), ::append
)

// ex15
fun increment(xs: List<Int>): List<Int> = foldRightL(xs, List.empty()) {
  cur, acc ->
  Cons(cur + 1, acc)
}

// ex16
fun doubleToString(xs: List<Double>): List<String> =
    foldRightL(xs, List.empty()) { cur, acc ->
        Cons(cur.toString(), acc)
    }

// ex17
fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
    foldRightL(xs, List.empty()) { cur, acc ->
        Cons(f(cur), acc)
    }

// ex18
fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
    foldRightL(xs, List.empty()) { cur, acc ->
        if (f(cur)) {
            Cons(cur, acc)
        } else {
            acc
        }
    }

// ex19
fun <A, B> flatMap(xa: List<A>, f: (A) -> List<B>): List<B> =
    foldRightL(xa, List.empty()) { cur, acc ->
        append(f(cur), acc)
    }

// ex20
fun <A> filter2(xa: List<A>, f: (A) -> Boolean): List<A> = flatMap(xa) {
  if (f(it)) {
      List.of(it)
  } else {
      List.empty()
  }
}

// ex21
fun add(xa: List<Int>, xb: List<Int>): List<Int> =
    when (xa) {
        is Nil -> Nil
        is Cons -> when (xb) {
            is Nil -> Nil
            is Cons ->
                Cons(xa.head + xb.head, add(xa.tail, xb.tail))
        }
    }

// ex22
fun <A> zipWith(xa: List<A>, xb: List<A>, f: (A, A) -> A): List<A> = when (xa) {
  is Nil -> Nil
  is Cons -> when (xb) {
      is Nil -> Nil
      is Cons -> Cons(f(xa.head, xb.head), zipWith(xa.tail, xb.tail, f))
  }
}

// ex23
tailrec fun <A> startsWith(l1: List<A>, l2: List<A>): Boolean =
    when (l1) {
        is Nil -> l2 == Nil
        is Cons -> when (l2) {
            is Nil -> true
            is Cons ->
                if (l1.head == l2.head)
                    startsWith(l1.tail, l2.tail)
                else false
        }
    }

tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean =
    when (xs) {
        is Nil -> false
        is Cons ->
            if (startsWith(xs, sub))
                true
            else hasSubsequence(xs.tail, sub)
    }

// ex24
fun <A> size(tree: Tree<A>): Int =
    when (tree) {
        is Leaf -> 1
        is Branch -> 1 + size(tree.left) + size(tree.right)
    }

// ex25
fun maximum(tree: Tree<Int>): Int =
    when (tree) {
        is Leaf -> tree.value
        is Branch -> maxOf(maximum(tree.left), maximum(tree.right))
    }

// ex26
fun depth(tree: Tree<Int>): Int =
    when (tree) {
        is Leaf -> 0
        is Branch -> 1 + maxOf(depth(tree.left), depth(tree.right))
    }

// ex27
fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> =
    when (tree) {
        is Leaf -> Leaf(f(tree.value))
        is Branch -> Branch(
            map(tree.left, f),
            map(tree.right, f)
        )
    }

// ex28
fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
    when (ta) {
        is Leaf -> l(ta.value)
        is Branch -> b(fold(ta.left, l, b), fold(ta.right, l, b))
    }

fun <A> sizeF(ta: Tree<A>): Int =
    fold(ta, { 1 }, { b1, b2 -> 1 + b1 + b2 })

fun maximumF(ta: Tree<Int>): Int =
    fold(ta, { a -> a }, { b1, b2 -> maxOf(b1, b2) })

fun <A> depthF(ta: Tree<A>): Int =
    fold(ta, { 0 }, { b1, b2 -> 1 + maxOf(b1, b2) })

fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> =
    fold(ta, { a: A -> Leaf(f(a)) },
        { b1: Tree<B>, b2: Tree<B> -> Branch(b1, b2) })

