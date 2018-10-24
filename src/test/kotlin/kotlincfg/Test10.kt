package test.kotlin.kotlincfg

fun test10(foo: Int): Int {
    var a = 0
    if (foo > 5) {
        when (foo) {
            6 -> a = 6
            7 -> a = 7
        }
    } else {
        when (foo) {
            8 -> a = 8
            9 -> a = 8
        }
    }
    return a
}