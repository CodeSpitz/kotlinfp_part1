package kore.fd

//inline fun <VALUE:Any> catchNull(throwBlock:()->VALUE):VALUE? = try{
//    throwBlock()
//}catch (e:Throwable) {
//    null
//}
//inline fun <VALUE:Any, OTHER:Any> VALUE?.map(block:(VALUE)->OTHER):OTHER? = this?.let{block(it)}
//inline fun <VALUE:Any, OTHER:Any> VALUE?.flatMap(block:(VALUE)->OTHER?):OTHER? = this?.let{block(it)}
//inline fun <VALUE:Any> VALUE?.get(block:()->VALUE):VALUE = this ?: block()
//inline fun <VALUE:Any> VALUE?.filter(block:(VALUE)->Boolean):VALUE?= this?.let{if(block(it)) it else null}