import java.io.File

fun test9(a: Int, b: String): Int {
    var tmp: Int = a
    do {
        val c = "25"
        val d = File("abc")
        for (line in d.readLines()) {
            if (line != "some line") {
                for (i in 0..5)
                    return 25
            } else {
                print("foo")
                print("bar")
                println("baz")
            }
        }
        while(true) {
            tmp += 1
            if (tmp == 10)
                break;
            else {
                when (tmp) {
                    1,2 -> print(tmp)
                    !is Int -> return 404
                    else -> println("123")
                }
            }
            println(c + 1)
        }
    } while (a in 5..10)
    if (b.length < 7) {
        return 0
    } else
        return 7
}