package test.kotlin.kotlincfg

fun test11(a: Int) {
    when(a) {
        3 -> println("foo")
        42 -> println("bar")
    }
    do {
        println("baz")
    } while (true);
}