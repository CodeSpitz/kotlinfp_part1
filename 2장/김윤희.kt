// 2.1
fun fib(i: Int): Int {
    tailrec fun go(i: Int, current: Int, next: Int): Int =
        if (i <= 0)
            current
        else go(i - 1, next, current + next) + 1
    return go(i, 0, 1)
}
