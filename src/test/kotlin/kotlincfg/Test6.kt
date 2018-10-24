package test.kotlin.kotlincfg

fun test6(x: Int): Int {
    when (x) {
        1,10 -> println(1)
        2 -> {
            println(2)
            return 2
        }
        3 -> return 3
        4 -> println(4)
        5 -> return 5
    }
    return 42
}