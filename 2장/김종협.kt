fun fib(i: Int): Int {
    tailrec fun go(count: Int, curr: Int, next: Int): Int =
        if (count == 0) curr
        else go(count - 1, next, curr + next)
    return go(i, 0, 1)
}
