package test.kotlin.kotlincfg

fun fib(n: Int): Int {
    var a = 0
    var b = 1
    var c: Int = 0
    if (n < 2)
        return n;
    for (i in 1..n) {
        c = a + b
        a = b
        b = c
    }
    return c
}