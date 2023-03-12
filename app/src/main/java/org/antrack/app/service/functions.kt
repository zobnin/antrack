
fun String.splitMultiCommand() =
    this.removePrefix("!")
        .split(";")
        .dropLastWhile { it.isEmpty() }
        .map { it.trim() }
        .map { it.split(" ", limit = 2) }
        .associate { it[0] to (it.getOrNull(1) ?: "") }
