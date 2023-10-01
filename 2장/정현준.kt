class _02_코틀린으로_함수형_프로그래밍_시작하기: StringSpec ({

    "2.1 피보나치" {
        fun fib(i: Int) : Int {
            tailrec fun loop(count: Int, current: Int = 0, next: Int = 1): Int =
                if (count == 0) current
                else loop(count - 1, next , current + next)

            return loop(i)
        }

        fib(1) shouldBeEqual 1
        fib(5) shouldBeEqual 5
        fib(8) shouldBeEqual 21
    }

    "2.2 isSorted를 구현하라" {
        fun <A> isSorted(list: List<A>, order: (A, A) -> Boolean) : Boolean {
            tailrec fun loop(now: A, list: List<A>): Boolean =
                if (list.isEmpty()) true
                else if(!order(now, list.head)) false
                else loop(list.head, list.tail)

            return list.isEmpty() || loop(list.head, list.tail)
        }
        isSorted(listOf(1,2,3,4,5,5)) { e1 , e2 -> e1 <= e2 }  shouldBeEqual true
        isSorted(listOf('A','B','B','C')) { e1 , e2 -> e1 <= e2 }  shouldBeEqual true
        isSorted(listOf("A","B","D","C")) { e1 , e2 -> e1 <= e2 }  shouldBeEqual false
    }

    "2.3 커링" {
        fun <A, B, C> curry(f: (A, B) -> C) : (A) -> (B) -> C =
            { a: A -> { b: B -> f(a, b) } }

        val f: (Int) -> (Int) -> String = curry { a,b -> "$a : ${a+b}"}

        val f1: (Int) -> String = f(1)
        f1(2) shouldBeEqual "1 : 3"
        f(2)(3) shouldBeEqual "2 : 5"
        f(10)(20) shouldBeEqual "10 : 30"
    }

    "2.4 언커링" {
        fun <A, B, C> uncurry(f: (A) -> (B) -> C) : (A, B) -> C =
            { a: A, b: B -> f(a)(b) }

        val f: (Int, Int) -> String = uncurry { a -> { b -> "$a : $b" }}

        f(1,2) shouldBeEqual "1 : 2"
    }

    "2.5. 고차 함수" {
        fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C =
            { a: A -> f(g(a)) }

        fun plus(number: Int, add: Int = 10) = number + add
        fun multi(number: Int, multi: Int = 2) = number * multi
        val result = compose(::plus, ::multi)
        result(10) shouldBeEqual 30
    }
})

val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()
