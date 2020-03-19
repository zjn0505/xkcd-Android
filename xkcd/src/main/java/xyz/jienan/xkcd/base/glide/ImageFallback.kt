package xyz.jienan.xkcd.base.glide

fun String.fallback(): String {
    return when {
        startsWith("https") -> {
            replaceFirst("https".toRegex(), "http")
        }
        contains("_2x.") -> {
            val indexOf2x = lastIndexOf("_2x.")
            if (indexOf2x > 0) {
                replaceRange(indexOf2x, indexOf2x + 3, "")
                        .replaceFirst("http://", "https://")
            } else {
                this
            }
        }
        else -> this
    }
}
