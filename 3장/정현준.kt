
object Nil: TestList<Nothing>()
data class Cons<out A>(
    val head: A,
    val tail: TestList<A>
): TestList<A>()
typealias Identity<B> = (B) -> B
sealed class TestList<out A> {
    companion object {
        fun <A> of(vararg aa: A) : TestList<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        fun <A> empty(): TestList<A> = Nil

        tailrec fun <A, B> foldLeft(xs: TestList<A>, z: B, f: (B, A) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
            }

        fun <A, B> foldRight(xs: TestList<A>, z: B, f: (A,B) -> B): B =
            when(xs) {
                is Nil -> z
                is Cons -> f(xs.head, foldRight(xs.tail, z, f))
            }

        fun sum2(ints: TestList<Int>): Int =
            foldRight(ints, 0) { a, b -> a + b }

        fun product2(doubles: TestList<Double>): Double =
            foldRight(doubles, 1.0) {a, b -> a * b }

        fun sum(ints: TestList<Int>): Int =
            when (ints) {
                is Nil -> 0
                is Cons -> ints.head + sum(ints.tail)
            }

        fun product(doubles: TestList<Double>): Double =
            when (doubles) {
                is Nil -> 1.0
                is Cons -> if (doubles.head == 0.0) 0.0
                else doubles.head * product(doubles.tail)
            }

        fun <A> append(a1: TestList<A>, a2: TestList<A>): TestList<A> =
            when(a1) {
                is Nil -> a2
                is Cons -> Cons(a1.head, append(a1.tail, a2))
            }
    }
}

sealed class Tree<out A>

data class Leaf<A>(val value: A): Tree<A>()
data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

class _03_함수형_데이터_구조: StringSpec ({

    "3.1 tail 함수를 만들어라" {
        fun <A> TestList<A>.tail() : TestList<A> =
            when(this) {
                is Cons -> this.tail
                is Nil -> Nil
            }
        of("A", "B", "C", "D").tail() shouldBeEqual of("B", "C", "D")
        of(Nil).tail() shouldBeEqual Nil
    }

    "3.2 List 첫 원소를 다른 값으로 대치하는 setHead 함수를 만들어라" {
        fun <A> TestList<A>.setHead(x: A) : TestList<A> =
            when(this) {
                is Cons -> Cons(x, this.tail)
                is Nil -> Nil
            }
        of(1, 2, 3, 4).setHead(5) shouldBeEqual of(5, 2, 3, 4)
        of(Nil).setHead(5) shouldBeEqual Nil
    }

    "3.3 List 맨 앞부터 n개 원소를 제거하는 drop 함수를 만들어라" {
        fun <A> TestList<A>.drop(n: Int) : TestList<A> =
            if (n <= 0) this
            else when(this) {
                is Cons -> this.tail.drop(n - 1)
                is Nil -> Nil
            }
        val list = of(1, 2, 3, 4)
        list.drop(2) shouldBeEqual of(3, 4)
        list.drop(4) shouldBeEqual Nil
        list.drop(5) shouldBeEqual Nil
    }

    "3.4 List 맨 앞에서부터 주어진 술어를 만족(술어 함수가 true를 반환)하는 연속적인 원소를 삭제하는 dropWhile 함수를 만들어라" {
        fun <A> TestList<A>.dropWhile(predicate: Predicate<A>) : TestList<A> =
            when(this) {
                is Cons ->
                    if(predicate.test(this.head)) this.tail.dropWhile(predicate)
                    else this
                is Nil -> Nil
            }

        val isEven : (Int) -> Boolean = { it % 2 == 0 }
        val list = of(2,4,6,8,5,10,20)
        list.dropWhile(isEven) shouldBeEqual of(5,10,20)

        val list1 = of(2,4,6)
        list1.dropWhile(isEven) shouldBeEqual Nil
    }

    "3.5 List 에서 마지막 원소를 제외한 나머지 원소로 이뤄진 새 List 를 반환하는 init 함수를 만들어라" {
        fun <A> TestList<A>.init() : TestList<A> =
            when(this) {
                is Cons ->
                    if(this.tail == Nil) Nil
                    else Cons(this.head, this.tail.init())
                is Nil -> Nil
            }

        val list = of("A", "B", "C", "D")
        list.init() shouldBeEqual of("A", "B", "C")
    }

    "3.6 foldRight로 구현된 product가 리스트 원소로 0.0을 만나면 재귀를 즉시 중단하고 결과를 돌려줄 수 있는가? 긴 리스트에 대해 쇼트 서킷을 제공할 수 있으면 어떤 장점이 있을까?" {}

    "3.8 foldRight를 사용해 리스트 길이를 계산하라" {
        fun <A> TestList<A>.length(): Int = foldRight(this, 0)
            { _: A, length: Int  -> 1 + length}

        of(1, 2, 3, 4).length() shouldBeEqual 4
        of(Nil).length() shouldBeEqual 1
        Nil.length() shouldBeEqual 0
    }

    "3.9 foldRight는 꼬리재귀가 아니므로 stack-safe 하지 않다. foldLeft를 꼬리 재귀로 작성하라" {
        fun <A, B> TestList<A>.foldLeft(z: B, f: (B, A) -> B): B {
            tailrec fun <A, B> loop(list: TestList<A>, default: B, function: (B, A) -> B) : B =
                when(list) {
                    is Cons -> loop(list.tail, function(default, list.head), function)
                    is Nil -> default
                }
            return loop(this, z, f)
        }

        val strings = of("A","B","C","D","E")
        val acc : (String, String) -> String = { f, s -> "f($f,$s)" }

        val foldLeft = strings.foldLeft("END",acc)
        foldLeft shouldBeEqual "f(f(f(f(f(END,A),B),C),D),E)"

        val foldRight = foldRight(strings, "END", acc)
        foldRight shouldBeEqual "f(A,f(B,f(C,f(D,f(E,END)))))"
    }

    "3.10 foldLeft를 사용해 sum, product, 리스트 길이 계산 함수를 작성하라" {
        fun <A, B> TestList<A>.foldLeft(z: B, f: (B, A) -> B): B {
            tailrec fun <A, B> loop(list: TestList<A>, default: B, function: (B, A) -> B) : B =
                when(list) {
                    is Cons -> loop(list.tail, function(default, list.head), function)
                    is Nil -> default
                }
            return loop(this, z, f)
        }

        val sum : (Int, Int) -> Int = { i1,i2 -> i1 + i2 }
        val product : (Int, Int) -> Int = { i1, i2 -> i1 * i2 }
        val length : (Int, Int) -> Int = { length, _ -> length + 1 }

        val list = of(1,2,3,4,5)

        list.foldLeft(0, sum) shouldBeEqual 15
        list.foldLeft(1, product) shouldBeEqual 120
        list.foldLeft(0, length) shouldBeEqual 5
    }

    "3.11 리스트 순서를 뒤집은 새 리스트를 반환하는 함수를 작성하라" {
        fun <A, B> TestList<A>.foldLeft(z: B, f: (B, A) -> B): B {
            tailrec fun <A, B> loop(list: TestList<A>, default: B, function: (B, A) -> B) : B =
                when(list) {
                    is Cons -> loop(list.tail, function(default, list.head), function)
                    is Nil -> default
                }
            return loop(this, z, f)
        }

        fun <A> TestList<A>.reverse(): TestList<A> =
            this.foldLeft(TestList.empty()) { list: TestList<A>, e: A -> Cons(e, list) }

        val list = of(1,2,3,4,5)
        list.reverse() shouldBeEqual of(5,4,3,2,1)
    }

    "3.12 foldLeft를 foldRight를 사용해 작성하라. foldRight를 foldLeft로 구현하면 꼬리 재귀로 구현할 수 있다." {
        fun <A, B> foldLeftReverse(list: TestList<A>, z: B, f: (B, A) -> B) : B =
            foldRight(
                list,
                { b: B -> b },
                { a, g ->
                    { b ->
                        g(f(b, a))
                    }
                }
            )(z)

        fun <A, B> foldRightReverse(list: TestList<A>, z: B, f: (A, B) -> B) : B =
            foldLeft(
                list,
                { b: B -> b },
                { g, a ->
                    { b ->
                        g(f(a, b))
                    }
                }
            )(z)

        fun <A, B> foldLeftRight(
            list: TestList<A>,
            acc: B,
            combiner: (B, A) -> B
        ): B {
            val identity: Identity<B> = { b: B -> b }
            val combinerDelayer: (A, Identity<B>) -> Identity<B> =
                { a: A, delayedExec: Identity<B> ->
                    { b: B ->
                        delayedExec(combiner(b, a))
                    }
                }
            val chain: Identity<B> = foldRight(list, identity, combinerDelayer)
            return chain(acc)
        }
        val list = of("a", "b", "c", "d", "e")
        val acc : (String, String) -> String = {a, b -> "f($a,$b)"}

        foldLeftReverse(list,"end",acc) shouldBeEqual "f(f(f(f(f(end,a),b),c),d),e)"
        foldLeftRight(list,"end",acc) shouldBeEqual "f(f(f(f(f(end,a),b),c),d),e)"
        foldRightReverse(list,"end",acc) shouldBeEqual "f(a,f(b,f(c,f(d,f(e,end)))))"
    }

    "3.13 append를 접기 연산을 사용해 구현하라." {
        fun <A> reverse(xs: TestList<A>): TestList<A> =
            TestList.foldLeft(xs, TestList.empty()) { t: TestList<A>, h: A -> Cons(h, t) }

        fun <A> append(a1: TestList<A>, a2: TestList<A>): TestList<A> =
            foldRight(a1, a2) { x, y -> Cons(x, y) }

        fun <A> appendL(a1: TestList<A>, a2: TestList<A>): TestList<A> =
            TestList.foldLeft(reverse(a1), a2) { y, x -> Cons(x, y) }

        val list1 = of("a", "b", "c")
        val list2 = of("A", "B", "C", "D")

        append(list1, list2) shouldBeEqual of("a", "b", "c", "A", "B", "C", "D")
        appendL(list1, list2) shouldBeEqual of("a", "b", "c", "A", "B", "C", "D")
    }

    "3.14 중첩리스트를 단일 리스트로 연결해주고 시간은 길이 합계에 선형으로 비례하는 함수를 작성하라" {
        fun <A> append(a1: TestList<A>, a2: TestList<A>): TestList<A> =
            foldRight(a1, a2) { x, y -> Cons(x, y) }

        fun <A> concat(xxs: TestList<TestList<A>>): TestList<A> =
            foldRight(
                xxs,
                TestList.empty()
            ) { xs1: TestList<A>, xs2: TestList<A> ->
                foldRight(xs1, xs2) { a, ls -> Cons(a, ls) }
            }

        fun <A> appendConcat(xxs: TestList<TestList<A>>): TestList<A> =
            foldRight(
                xxs,
                TestList.empty()
            ) { xs1, xs2 ->
                append(xs1, xs2)
            }

        val nestedList = of(
            of("a","b","c"),
            of("A", "B")
        )

        concat(nestedList) shouldBeEqual of("a","b","c","A","B")
        appendConcat(nestedList) shouldBeEqual of("a","b","c","A","B")
    }

    "3.15 정수로 이뤄진 리스트를 각 원소에 1을 더한 리스트로 변환하는 함수를 작성하라." {
        fun increment(list: TestList<Int>): TestList<Int> =
            foldRight(
                list,
                TestList.empty()
            ) { e: Int, ls: TestList<Int> ->
                Cons(e + 1, ls)
            }

        val list = of(1, 2, 3, 4, 5)
        increment(list) shouldBeEqual of(2, 3, 4, 5, 6)
    }

    "3.16 List<Double> 각 원소를 String으로 변환하는 함수를 작성하라." {
        fun doubleToString(list: TestList<Double>): TestList<String> =
            foldRight(
                list,
                TestList.empty()
            ) { e: Double, ls: TestList<String> ->
                Cons(e.toString(), ls)
            }

        val list = of(1.1, 2.0, 3.333)
        doubleToString(list) shouldBeEqual of("1.1", "2.0", "3.333")

//        before 1.1 : Cons(head=2.0, tail=Cons(head=3.333, tail=kotlinfp.Nil@54c01b21))
//        before 2.0 : Cons(head=3.333, tail=kotlinfp.Nil@54c01b21)
//        before 3.333 : kotlinfp.Nil@54c01b21
//        do
//        after 3.333 : kotlinfp.Nil@54c01b21
//        after 2.0 : Cons(head=3.333, tail=kotlinfp.Nil@54c01b21)
//        after 1.1 : Cons(head=2.0, tail=Cons(head=3.333, tail=kotlinfp.Nil@54c01b21))
    }

    "3.17 리스트의 모든 원소를 변경하되 리스트 구조는 그대로 유지하는 map 함수를 작성하라" {
        fun <A, B> foldRightL(xs: TestList<A>, z: B, f: (A, B) -> B): B =
            foldLeft(xs,
                { b: B -> b },
                { g, a ->
                    { b ->
                        g(f(a, b))
                    }
                })(z)

        fun <A, B> map(xs: TestList<A>, f: (A) -> B): TestList<B> =
            foldRightL(xs, TestList.empty()) { a: A, xa: TestList<B> ->
                Cons(f(a), xa)
            }

        val list = of(1, 2, 3, 4, 5)
        map(list) { i -> i * i } shouldBeEqual of(1, 4, 9, 16, 25)
    }

    "3.18 리스트에서 주어진 술어를 만족하지 않는 원소를 제거해주는 filter 함수를 작성하라" {
        fun <A> filter(list: TestList<A>, f: (A) -> Boolean): TestList<A> =
            foldRight(
                list,
                TestList.empty()
            ) { a, ls ->
                if (f(a)) Cons(a, ls)
                else ls
            }

        val isEven : (Int) -> Boolean = { i -> i % 2 == 0 }
        val list = of(1,2,3,4,5,6)

        filter(list, isEven) shouldBeEqual of(2,4,6)
    }

    "3.19 map처럼 작동하지만 인자로 리스트를 반환하는 함수를 받는 flatMap 함수를 작성하라." {
        // 인자로 전달된 함수가 만들어낸 모든 리스트의 원소가 최종 리스트 안에 삽입 돼야 한다.
        fun <A, B> flatMap(xa: TestList<A>, f: (A) -> TestList<B>): TestList<B> =
            foldRight(
                xa,
                TestList.empty()
            ) { a, lb ->
                append(f(a), lb)
            }

        fun <A, B> flatMap2(xa: TestList<A>, f: (A) -> TestList<B>): TestList<B> =
            foldRight(
                xa,
                TestList.empty()
            ) { a, xb ->
                foldRight(f(a), xb) { b, lb -> Cons(b, lb) }
            }

        val list = of(1, 2, 3)
        val copy : (Int) -> TestList<Int> = { i -> of(i , i) }

        flatMap(list, copy) shouldBeEqual of(1, 1, 2, 2, 3, 3)
        flatMap2(list, copy) shouldBeEqual of(1, 1, 2, 2, 3, 3)
    }

    "3.20 flatMap을 사용해 filter를 구현하라" {
        fun <A, B> flatMap(xa: TestList<A>, f: (A) -> TestList<B>): TestList<B> =
            foldRight(
                xa,
                TestList.empty()
            ) { a, lb ->
                append(f(a), lb)
            }

        fun <A> filter(list: TestList<A>, f: (A) -> Boolean): TestList<A> =
            flatMap(
                list,
            ) { a ->
                if (f(a)) of(a)
                else TestList.empty()
            }

        val isEven : (Int) -> Boolean = { i -> i % 2 == 0 }
        val list = of(1, 2, 3, 4, 5, 6)

        filter(list, isEven) shouldBeEqual of(2, 4, 6)
    }

    "3.21 두 리스트를 받아서 서로 같은 인덱스에 있는 원소들을 더한 값으로 새 리스트를 돌려주는 함수를 작성하라" {
        fun add(xa: TestList<Int>, xb: TestList<Int>): TestList<Int> =
            when (xa) {
                is Nil -> Nil
                is Cons -> when (xb) {
                    is Nil -> Nil
                    is Cons ->
                        Cons(xa.head + xb.head, add(xa.tail, xb.tail))
                }
            }

        val list1 = of(1, 2, 3, 4, 5)
        val list2 = of(1, 2, 3, 4)

        add(list1, list2) shouldBeEqual of(2, 4, 6, 8)
    }

    "3.22 위에서 작성한 함수를 일반화해 다양한 처리가 가능하게 하라" {
        fun <A> zipWith(xa: TestList<A>, xb: TestList<A>, operator: (A, A) -> A): TestList<A> =
            when (xa) {
                is Nil -> Nil
                is Cons -> when (xb) {
                    is Nil -> Nil
                    is Cons ->
                        Cons(operator(xa.head, xb.head), zipWith(xa.tail, xb.tail, operator))
                }
            }

        val add : (String, String) -> String = { a,b -> a+b }
        val list1 = of("a", "b", "c", "d", "e")
        val list2 = of("a", "b", "c")

        zipWith(list1, list2, add) shouldBeEqual of("aa", "bb", "cc")
    }

    "3.23 어떤 List가 다른 List를 부분열로 포함하는지 검사하는 hasSubsequence를 구현하라" {
        tailrec fun <A> startsWith(l1: TestList<A>, l2: TestList<A>): Boolean =
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

        tailrec fun <A> hasSubsequence(xs: TestList<A>, sub: TestList<A>): Boolean =
            when (xs) {
                is Nil -> false
                is Cons ->
                    if (startsWith(xs, sub))
                        true
                    else hasSubsequence(xs.tail, sub)
            }

        val list = of(1, 3, 5, 6)
        val sub1 = of(3, 5)
        val sub2 = of(1)
        val sub3 = of(3, 6)

        hasSubsequence(list, sub1) shouldBeEqual true
        hasSubsequence(list, sub2) shouldBeEqual true
        hasSubsequence(list, sub3) shouldBeEqual false
    }

    "3.24 트리 안에 들어 있는 노드(잎과 가지 모두)의 개수를 반환하는 size 함수를 작성하라" {
        fun <A> size(tree: Tree<A>): Int =
            when(tree) {
                is Branch -> 1 + size(tree.left) + size(tree.right)
                is Leaf -> 1
            }

        val tree = Branch(
            Branch(Leaf(1), Leaf(2)),
            Branch(Leaf(3), Leaf(4))
        )

        size(tree) shouldBeEqual 7
    }

    "3.25 Tree<Int> 에서 가장 큰 원소를 돌려주는 maximum 함수를 작성하라" {
        fun maximum(tree: Tree<Int>): Int =
            when (tree) {
                is Leaf -> tree.value
                is Branch -> maxOf(maximum(tree.left), maximum(tree.right))
            }

        val tree = Branch(
            Branch(Leaf(1), Leaf(9)),
            Branch(Leaf(3), Leaf(4))
        )

        maximum(tree) shouldBeEqual 9
    }

    "3.26 트리 뿌리에서 각 잎까지의 경로 중 가장 길이가 긴 값을 돌려주는 depth 함수를 작성하라" {
        fun depth(tree: Tree<Int>): Int =
            when(tree) {
                is Branch -> 1 + maxOf(depth(tree.left), depth(tree.right))
                is Leaf -> 0
            }

        val tree = Branch(
            Branch(
                Leaf(1),
                Branch(
                    Leaf(2),
                    Branch(
                        Leaf(3),
                        Leaf(4)
                    )
                )
            ),
            Branch(
                Leaf(5),
                Leaf(6)
            )
        )

        depth(tree) shouldBeEqual 4
    }

    "3.27 트리의 모든 원소를 주어진 함수를 사용해 변환하는 map 함수를 정의하라" {
        fun <A, B> map(tree: Tree<A>, f: (A) ->  B): Tree<B> =
            when(tree) {
                is Branch -> Branch(
                    map(tree.left, f),
                    map(tree.right, f)
                )
                is Leaf -> Leaf(f(tree.value))
            }

        val tree = Branch(
            Branch(
                Leaf(1),
                Branch(
                    Leaf(2),
                    Branch(
                        Leaf(3),
                        Leaf(4)
                    )
                )
            ),
            Branch(
                Leaf(5),
                Leaf(6)
            )
        )
        val intToString : (Int) -> String = { i -> "$i" }
        val expect = Branch(
            Branch(
                Leaf("1"),
                Branch(
                    Leaf("2"),
                    Branch(
                        Leaf("3"),
                        Leaf("4")
                    )
                )
            ),
            Branch(
                Leaf("5"),
                Leaf("6")
            )
        )

        map(tree, intToString) shouldBeEqual expect
    }

    "3.28 Tree에서 size, maximum, depth, map을 일반화해 이 함수들의 유사한 점을 추상화한 새로운 fold 함수를 작성하라" {
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

        val tree = Branch(
            Branch(Leaf(1), Leaf(2)),
            Branch(
                Leaf(3),
                Branch(
                    Branch(Leaf(4), Leaf(5)),
                    Branch(
                        Leaf(21),
                        Branch(Leaf(7), Leaf(8))
                    )
                )
            )
        )

        sizeF(tree) shouldBeEqual 15
        maximumF(tree) shouldBe 21
        depthF(tree) shouldBe 5
        mapF(tree) { it * 10 } shouldBe
                Branch(
                    Branch(Leaf(10), Leaf(20)),
                    Branch(
                        Leaf(30),
                        Branch(
                            Branch(Leaf(40), Leaf(50)),
                            Branch(
                                Leaf(210),
                                Branch(Leaf(70), Leaf(80))
                            )
                        )
                    )
                )
    }
})
