// ex1
fun <T> map2(a: Par<T>, b: Par<T>, block: (lx: T, rx: T) -> T): Par<T> =
    Par(block(a.get, b.get))

// ex2
// ex3
fun <A, B, C> map2(
    a: Par<A>,
    b: Par<B>,
    f: (A, B) -> C
): Par<C> = { es: ExecutorService ->
    val af: Future<A> = a(es)
    val bf: Future<B> = b(es)
    TimedMap2Future(af, bf, f)
}

data class TimedMap2Future<A, B, C>(
  val pa: Future<A>,
  val pb: Future<B>,
  val f: (A, B) -> C
): Future<C> {
  override fun get(to: Long, tu: Timeunit): C {
    val timeoutMills = TimeUnit.MILLISECONDS.convert(to, tu)

    val start = System.currentTimeMillis()
    val a = pa.get(to, tu)
    val duration = System.currentTimeMillis() - start

    val remainder = timeoutMills - duration
    val b = pb.get(remainder, TimeUnit.MILLISECONDS)
    return f(a, b)
  }
}

// ex4
fun <A. B> asyncF(f: (A) -> B): (A) -> Par<B> = { a: A -> lazyUnit(f(a)) }

// ex5
fun <A> sequence(ps: List<Par<A>>): Par<List<A>> = when {
    ps.isEmpty() -> unit(emptyList())
    ps.size == 1 -> map(ps.head()) { listOf(it) }
    else -> {
        val (l, r) = ps.splitAt(ps.size / 2)
        map2(sequence(l), sequence(r)) { lx, rx -> lx + rx }
    }
}

// ex6 
fun <A> parFilter(list: List<A>, f: (A) -> Boolean): Par<List<A>> {
  val pars: List<Par<List<A>>> = list.map(lazyUnit { it })
  return map(sequence(pars)) {
    it.flatMap { if (f(it)) listOf(it) else emptyList() }
  }
}

// ex7
