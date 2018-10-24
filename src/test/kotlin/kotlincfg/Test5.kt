package test.kotlin.kotlincfg

fun test5(): Int {
    var y = 0
    do {
        y++
        println(y)
    } while (y < 5)
    do
        y--
    while (y > 0)
    return 42
}