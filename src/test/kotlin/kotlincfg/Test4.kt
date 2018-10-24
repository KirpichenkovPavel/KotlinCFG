package test.kotlin.kotlincfg

fun test4(): Int {
    var i = 0
    while (i < 10) {
        i++
        println(i)
    }
    while (i < 20)
        while (false) {
            println("never happens")
            return 42
        }
    while(true)
        println("infinite")
    return 42
}