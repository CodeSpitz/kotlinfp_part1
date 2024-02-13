package kore.fd


fun interface ContextThunk<out VALUE:Any>:(Context)->Thunk<VALUE>{
    companion object{
        operator fun <VALUE:Any>invoke(block:(Context)->Thunk<VALUE>):ContextThunk<VALUE> = ContextThunk(block)
        operator fun <VALUE:Any>invoke(value:VALUE):ContextThunk<VALUE> = ContextThunk{Thunk{value}}
        fun <VALUE:Any> fork(block:()->ContextThunk<VALUE>):ContextThunk<VALUE> = ContextThunk{block()(it)}
    }
}
fun <VALUE:Any, OTHER:Any, RESULT:Any> ContextThunk<VALUE>.map2(target:ContextThunk<OTHER>, block:(VALUE, OTHER)->RESULT):ContextThunk<RESULT>
        = ContextThunk{context->
    val actor = Actor<ContextThunk<*>, Any>{ct->ct(context)()}
    actor(this, target)
    Thunk {
        val result = actor.result()
        @Suppress("UNCHECKED_CAST")
        block(result[0] as VALUE, result[1] as OTHER)
    }
}
fun <VALUE:Any> ContextThunk<Int>.choice(targets:List<ContextThunk<VALUE>>):ContextThunk<VALUE>
= ContextThunk{context->
    targets[this(context)()](context)
}
fun <KEY:Any, VALUE:Any> ContextThunk<KEY>.choice(targets:Map<KEY, ContextThunk<VALUE>>):ContextThunk<VALUE>
= ContextThunk{context->
    targets[this(context)()]!!(context)
}
fun <VALUE:Any, OTHER:Any> ContextThunk<VALUE>.flatMap(block:(VALUE)->ContextThunk<OTHER>):ContextThunk<OTHER>
= ContextThunk{context->
    block(this(context)())(context)
}
fun <KEY:Any, VALUE:Any> ContextThunk<KEY>.choiceFlatMap(targets:Map<KEY, ContextThunk<VALUE>>):ContextThunk<VALUE>
= flatMap {
    targets[it]!!
}
fun <VALUE:Any> ContextThunk<Int>.choiceFlatMap(targets:List<ContextThunk<VALUE>>):ContextThunk<VALUE>
= flatMap {
    targets[it]
}
fun <VALUE:Any> ContextThunk<ContextThunk<VALUE>>.flatten():ContextThunk<VALUE>
= flatMap{it}
fun <VALUE:Any, OTHER:Any, RESULT:Any> ContextThunk<VALUE>.map2FlatMap(target:ContextThunk<OTHER>, block:(VALUE, OTHER)->ContextThunk<RESULT>):ContextThunk<RESULT>
= flatMap {a->
    target.flatMap {b->
        block(a, b)
    }
}