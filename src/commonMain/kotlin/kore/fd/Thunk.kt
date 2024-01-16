package kore.fd

fun interface Thunk<out VALUE:Any>{
    operator fun invoke():VALUE
}