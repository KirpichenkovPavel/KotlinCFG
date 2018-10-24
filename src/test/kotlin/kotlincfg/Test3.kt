package test.kotlin.kotlincfg

fun foo(): Int {
    for (i in 1..10) {
        println(i)
    }
    for (k in arrayListOf<Int>(1,2,3).indices)
        println(k)
    for (i in 1..3)
        for (j in 4..6) {
            println("${i}${j}")
        }
    return 42
}
