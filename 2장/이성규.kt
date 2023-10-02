// 2.1
fun fivonacci(num: UInt): UInt {
    tailrec fun go(targetIdx: UInt, curIdx: UInt, n1: UInt, n2: UInt): UInt {
        if (targetIdx == curIdx) {
			return n1 + n2
        }
        
        return go(targetIdx, curIdx + 1u, n2, n1 + n2)
    }
    return if (num <= 1u) num else go(num, 2u, 0u, 1u)
}

// 2.2
val <T> List<T>.tail: List<T>
    get() = drop(1)
    
val <T> List<T>.head: T
	get() = first()

tailrec fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    return when {
        aa.tail.isEmpty() -> true
        !order(aa.head, aa.tail.head) -> false
        else -> isSorted(aa.tail, order)
    }
}


// 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { 
    a: A -> { b: B -> f(a, b) }
}

// 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = {
    a: A, b: B -> f(a)(b)
}

// 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {
    a: A -> f(g(a))
}
