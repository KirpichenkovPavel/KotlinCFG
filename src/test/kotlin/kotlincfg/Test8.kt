import java.io.File

fun test8(s: String): Int {
    val a = 20
    var b: String
    b = "123"
    do {
        if (b.startsWith("12"))
            return 12
        else if ("foo" > "bar")
            for (i in 1..3)
                print("message")
            else {
                val c = File("fromCurDir")
                println(c.absolutePath)
            }
    } while (s == "25.0")
    return 55 - 8
}