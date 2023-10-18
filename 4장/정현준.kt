sealed class Option<out ITEM: Any> {
    data class Some<out ITEM: Any>(val item: ITEM) : Option<ITEM>()
    object None: Option<Nothing>()

    companion object {
        fun <ITEM: Any> empty(): Option<ITEM> = None
        fun <ITEM: Any> of(item: ITEM): Option<ITEM> = Some(item)
    }

    fun <ITEM: Any, RESULT: Any> Option<ITEM>.map(block: (ITEM) -> RESULT): Option<RESULT> =
        when(this) {
            is None -> this
            is Some -> Some(block(this.item))
        }

    fun <ITEM: Any, RESULT: Any> Option<ITEM>.flatMap(block: (ITEM) -> Option<RESULT>): Option<RESULT> =
        this.map(block).getOrElse { None }

    fun <ITEM: Any> Option<ITEM>.getOrElse(default: () -> ITEM): ITEM =
        when(this) {
            is None -> default()
            is Some -> this.item
        }

    fun <ITEM: Any> Option<ITEM>.orElse(default: () -> Option<ITEM>): Option<ITEM> =
        this.map { Some(it) }.getOrElse { default() }

    fun <ITEM: Any> Option<ITEM>.filter(predicate: (ITEM) -> Boolean) : Option<ITEM> =
        this.flatMap { item -> if(predicate(item)) Some(item) else None }

    fun <ITEM: Any, RESULT: Any> Option<ITEM>.flatMap_2(block: (ITEM) -> Option<RESULT>): Option<RESULT> =
        when(this) {
            is None -> this
            is Some -> block(this.item)
        }
    fun <ITEM: Any> Option<ITEM>.orElse_2(default: () -> Option<ITEM>): Option<ITEM> =
        when(this) {
            is None -> default()
            is Some -> this
        }
    fun <ITEM: Any> Option<ITEM>.filter_2(predicate: (ITEM) -> Boolean) : Option<ITEM> =
        when(this) {
            is None -> this
            is Some -> if(predicate(this.item)) this else None
        }
}

fun <ITEM: Any, RESULT: Any> lift(block: (ITEM) -> RESULT) : (Option<ITEM>) -> Option<RESULT> =
    { oa -> oa.map(block) }
fun <ITEM: Any> catches(block: () -> ITEM) : Option<ITEM> =
    try { Some(block()) } catch(e: Throwable) { None }

sealed class Either<out E, out A> {
    data class Left<out E>(val value: E) : Either<E, Nothing>()
    data class Right<out A>(val value: A) : Either<Nothing, A>()
}

fun <ERROR: Any, ITEM: Any, RESULT: Any> Either<ERROR, ITEM>.map(
    block: (ITEM) -> RESULT
) : Either<ERROR, RESULT> =
    when(this) {
        is Left -> this
        is Right -> Right(block(this.value))
    }

fun <ERROR: Any, ITEM: Any, RESULT: Any> Either<ERROR, ITEM>.flatMap(
    block: (ITEM) -> Either<ERROR, RESULT>
) : Either<ERROR, RESULT> =
    when(this) {
        is Left -> this
        is Right -> block(this.value)
    }
fun <ERROR: Any, ITEM: Any> Either<ERROR, ITEM>.orElse(
    block: () -> Either<ERROR, ITEM>
): Either<ERROR, ITEM> =
    when(this) {
        is Left -> block()
        is Right -> this
    }

fun <ERROR: Any, ITEM1: Any, ITEM2: Any, RESULT: Any> map2(
    item1: Either<ERROR, ITEM1>,
    item2: Either<ERROR, ITEM2>,
    block: (ITEM1, ITEM2) -> RESULT
): Either<ERROR, RESULT> =
    item1.flatMap { a -> item2.map { b -> block(a, b) } }

fun <ITEM: Any> eitherCatches(block: () -> ITEM) : Either<Exception, ITEM>  =
    try { Right(block()) } catch(e: Exception) { Left(e) }

class _04_예외를_사용하지_않고_오류_다루기: StringSpec ({

    "Option.map" {
        val none = Option.empty<String>()
        val some = Option.of("test")
        val toUppercase : (String) -> String = { it.uppercase() }

        some.map(toUppercase) shouldBeEqual Option.of("TEST")
        none.map(toUppercase) shouldBeEqual None
    }

    "Option.flatMap" {
        val none = Option.empty<Int>()
        val some = Option.of(123456)
        val toString : (Int) -> Option<String> = { Option.of(it.toString()) }

        some.flatMap(toString) shouldBeEqual Option.of("123456")
        some.flatMap_2(toString) shouldBeEqual Option.of("123456")

        none.flatMap(toString) shouldBeEqual None
    }

    "Option.getOrElse" {
        val none = Option.empty<String>()
        val some = Option.of("ABCDE")
        val default : () -> String = { "INIT" }

        some.getOrElse(default) shouldBeEqual "ABCDE"
        none.getOrElse(default) shouldBeEqual "INIT"
    }

    "Option.orElse" {
        val none = Option.empty<String>()
        val some = Option.of("TEST")
        val default : () -> Option<String> = { Option.of("INIT") }

        some.orElse(default) shouldBeEqual Option.of("TEST")
        none.orElse(default) shouldBeEqual Option.of("INIT")
    }

    "Option.filter" {
        val none = Option.empty<Int>()
        val some = Option.of(21)
        val isOdd : (Int) -> Boolean = { it % 2 != 0 }
        val isEven : (Int) -> Boolean = { it % 2 == 0 }

        some.filter(isOdd) shouldBeEqual Option.of(21)
        some.filter(isEven) shouldBeEqual None

        none.filter(isOdd) shouldBeEqual None
    }

    "4.2 flatMap을 사용해 variance 함수를 구현하라" {
        // 시퀀스의 평균이 m이면, variance는 시퀀스의 원소를 x라 할 때, x - m을 제곱한 값의 평균이다.
        // (x - m).pow(2)라 할 수 있다.
        fun mean(list: List<Double>) : Option<Double> =
            if(list.isEmpty()) None
            else Some(list.sum() / list.size)

        fun variance(list: List<Double>) : Option<Double> =
            mean(list).flatMap { m ->
                mean(list.map { x -> (x - m).pow(2) })
            }

        val list = listOf(1.1, 2.2, 3.3)
        variance(list) shouldBeEqual Option.of(0.8066666666666665)
        variance(emptyList()) shouldBeEqual Option.empty()

    }

    "4.3 두 Option 값을 이항 함수를 통해 조합하는 제네릭 함수 map2 를 작성하라." {
        // 두 Option 중에 하나라도 None이 존재하면 반환값도 None이다.
        fun <ITEM1: Any, ITEM2: Any, RESULT: Any> map2(
            item1: Option<ITEM1>,
            item2: Option<ITEM2>,
            block: (ITEM1, ITEM2) -> RESULT
        ): Option<RESULT> = item1.flatMap { first ->
            item2.map { second ->
                block(first, second)
            }
        }

        val item1 = Option.of("AB")
        val item2 = Option.of("CD")
        val none = Option.empty<String>()
        val concat : (String, String) -> String = { p1, p2 -> p1 + p2 }

        map2(item1, item2, concat) shouldBeEqual Option.of("ABCD")
        map2(item1, none, concat) shouldBeEqual Option.empty()
    }

    "4.4 원소가 Option인 리스트를 리스트인 Option으로 합쳐주는 sequence 함수를 작성하라" {
        // 리스트안의 원소 중 None이 하나라도 존재한다면 결괏값은 None이어야 한다.
        fun <ITEM1: Any, ITEM2: Any, RESULT: Any> map2(
            item1: Option<ITEM1>,
            item2: Option<ITEM2>,
            block: (ITEM1, ITEM2) -> RESULT
        ): Option<RESULT> = item1.flatMap { first ->
            item2.map { second ->
                block(first, second)
            }
        }

        fun <ITEM: Any> sequence(list: TestList<Option<ITEM>>) : Option<TestList<ITEM>> =
            TestList.foldRight(
                list,
                Some(Nil)
            ) { item: Option<ITEM>, items: Option<TestList<ITEM>> ->
                map2(item, items) { a1: ITEM, a2: TestList<ITEM> ->
                    Cons(a1, a2)
                }
            }

        val list = TestList(
                Option.of("A"),
                Option.of("B"),
                Option.of("C"),
                Option.of("D"),
                Option.of("E")
        )
        sequence(list) shouldBeEqual Option.of(
            TestList("A","B","C","D","E")
        )

        val none = TestList(
            Option.of("A"),
            Option.of("B"),
            Option.empty(),
            Option.of("D"),
            Option.of("E")
        )
        sequence(none) shouldBeEqual Option.empty()
    }

    "4.5 리스트를 단 한 번만 순회하는 traverse 함수를 구현하라" {
        fun <ITEM1: Any, ITEM2: Any, RESULT: Any> map2(
            item1: Option<ITEM1>,
            item2: Option<ITEM2>,
            block: (ITEM1, ITEM2) -> RESULT
        ): Option<RESULT> = item1.flatMap { first ->
            item2.map { second ->
                block(first, second)
            }
        }

        fun <ITEM: Any, RESULT: Any> traverse(
            list: TestList<ITEM>,
            block: (ITEM) -> Option<RESULT>
        ) : Option<TestList<RESULT>> =
            when(list) {
                is Nil -> Some(Nil)
                is Cons -> map2(
                    block(list.head),
                    traverse(list.tail, block)
                ) { item: RESULT, items: TestList<RESULT> -> Cons(item, items) }
            }

        val list = TestList(1,2,3,4,5)
        traverse(list) { e ->
            catches { e.toString() }
        } shouldBeEqual Option.of(TestList("1","2","3","4","5"))

        val list2 = TestList("1","2","3","$","5")
        traverse(list2) { s ->
            catches { s.toInt() }
        } shouldBeEqual None
    }

    "4.6 Right 값에 대해 활용할 수 있는 map, flatMap, orElse, map2를 구현하라" {
        val right: Either<Throwable, Int> = Right(100)
        val left: Either<Throwable, Int> = Left(Throwable("Exception!!!"))

        right.map { it.toString() } shouldBeEqual Right("100")
        left.map { it.toString() } shouldBeEqual left

        right.flatMap { item -> Right(item.toString()) } shouldBeEqual Right("100")
        left.flatMap { item -> Right(item.toString()) } shouldBeEqual left

        right.orElse { Left("Exception!!!") } shouldBeEqual right
        left.orElse { Left("Exception!!!") } shouldBeEqual  Left("Exception!!!")

        val right1: Right<Int> = Right(100)
        val right2: Right<Int> = Right(200)
        val left1: Either<Throwable, Int> =
            Left(IllegalArgumentException("IllegalArgumentException!!!"))
        val left2: Either<Throwable, Int> =
            Left(IllegalStateException("IllegalStateException!!!"))
        val plus : (Int, Int) -> Int = { a,b -> a + b }

        map2(right1, right2, plus)  shouldBeEqual Right(300)
        map2(right, left1, plus) shouldBeEqual left1
        map2(left1, left2, plus) shouldBeEqual left1
        map2(left2, left1, plus) shouldBeEqual left2
    }

    "4.7 Either에 대한 sequence와 traverse를 구현하라" {
        fun <ERROR: Any, ITEM: Any, RESULT: Any> traverse(
            list: TestList<ITEM>,
            block: (ITEM) -> Either<ERROR, RESULT>
        ): Either<ERROR, TestList<RESULT>> =
            when(list) {
                is Nil -> Right(Nil)
                is Cons ->
                    map2(block(list.head), traverse(list.tail, block)) { item, items ->
                        Cons(item, items)
                    }
            }

        fun <ERROR: Any, ITEM: Any> sequence(
            list: TestList<Either<ERROR, ITEM>>
        ) : Either<ERROR, TestList<ITEM>> =
            traverse(list) { it }

        fun <ITEM: Any> catches(block: () -> ITEM) : Either<String, ITEM>  =
            try { Right(block()) } catch(e: Throwable) { Left(e.message!!) }

        val list = TestList("1", "2", "3", "4", "5")
        traverse(list) { a ->
            catches { a.toInt() }
        } shouldBeEqual Right(TestList(1,2,3,4,5))

        val list2 = TestList("1", "2", "3", "4", "x")
        traverse(list2) { a ->
            catches { a.toInt() }
        } shouldBeEqual Left("""For input string: "x"""")

        val list3 = TestList(
            Right(1),
            Right(2),
            Right(3)
        )
        sequence(list3) shouldBeEqual Right(TestList(1, 2, 3))

        val list4 = TestList(
            Right(1),
            Right(2),
            Left("Exception!!!"),
            Right(3)
        )
        sequence(list4) shouldBeEqual Left("Exception!!!")
    }

    "4.8 두 개의 예외를 다 반환하려면 어떻게 해야할까?" {
        val left1: Either<Throwable, Int> =
            Left(IllegalArgumentException("IllegalArgumentException!!!"))
        val left2: Either<Throwable, Int> =
            Left(IllegalStateException("IllegalStateException!!!"))
        val plus : (Int, Int) -> Int = { a,b -> a + b }

        map2(left1, left2, plus) shouldBeEqual left1
        map2(left2, left1, plus) shouldBeEqual left2
    }
})