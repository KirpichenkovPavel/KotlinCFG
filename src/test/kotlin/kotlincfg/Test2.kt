package kotlincfg

fun foo(): Int {
    if (true) {
        print("foo")
    } else {
        if (false)
            return 0
    }
    println("hi")
    return 42
}
