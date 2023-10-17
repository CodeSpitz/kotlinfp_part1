// 2.1
fun fib(i: Int): Int {
    if(i<=1){
        return i
    }
    tailrec fun go(n:Int=1, acc:Int=1, prev:Int=0) :Int {
        if(n==i){
            return acc
        }
        return go(n+1, acc+prev, acc)
    }
    return go()
}

// 2.2
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    if(aa.isEmpty()) {
        return true
    }

    val first = aa.head
    val rest = aa.tail

    for(element in rest) {
        if (!order(first, element)) {
            return false
        }
    }

    return true
}

// 2.3
fun <A,B,C> curry(f:(A,B)->C):(A)->(B)->C = {a:A -> {b:B -> f(a,b) }}

// 2.4
fun <A,B,C> uncurry(f:(A)->(B)->C):(A,B)->C = { a:A, b:B -> f(a)(b) }

// 2.5
fun <A,B,C> compose(f: (B)->C,g:(A)->B):(A)->C = {a:A -> f(g(a))}
