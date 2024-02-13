package kore.fd

interface Context{
    operator fun <VALUE:Any> invoke(block:Thunk<VALUE>):Thunk<VALUE>
}