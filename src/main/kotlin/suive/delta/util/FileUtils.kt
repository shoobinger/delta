package suive.delta.util

fun getOffset(text: String, row: Int, col: Int): Int {
    return text.lineSequence().take(row).fold(0) { acc, l -> acc + l.length + 1 /* 1 for newline */ } + col
}
