package com.example.serenity_mad_le3.util

object EmojiMapper {
    private val map = mapOf(
        "\uD83E\uDD2C" to 1, // 🤬 face with symbols - very upset
        "\uD83D\uDE21" to 1, // 😡 very upset
        "\uD83D\uDE20" to 1, // 😠 angry
        "\uD83D\uDE22" to 2, // 😢 sad tears
        "\uD83D\uDE2D" to 2, // 😭 loudly crying
        "\uD83D\uDE25" to 2, // 😥 disappointed but relieved
        "\uD83D\uDE1E" to 3, // 😞 downcast
        "\uD83D\uDE14" to 3, // 😔 pensive
        "\uD83D\uDE1F" to 3, // 😟 worried
        "\uD83D\uDE10" to 4, // 😐 neutral
        "\uD83D\uDE11" to 4, // 😑 expressionless
        "\uD83D\uDE12" to 4, // 😒 unamused
        "\uD83D\uDE42" to 5, // 🙂 slight smile
        "\uD83D\uDE0C" to 5, // 😌 relieved
        "\uD83D\uDE0A" to 6, // 😊 warm smile
        "\uD83D\uDE03" to 6, // 😃 grinning
        "\uD83D\uDE04" to 7, // 😄 big grin
        "\uD83D\uDE01" to 8, // 😁 beaming smile
        "\uD83D\uDE06" to 8, // 😆 laughing
        "\uD83D\uDE02" to 8, // 😂 tears of joy
        "\uD83D\uDE0D" to 9, // 😍 heart eyes
        "\uD83E\uDD70" to 9, // 🥰 smiling with hearts
        "\uD83D\uDE18" to 9, // 😘 face blowing kiss
        "\uD83D\uDE0E" to 9, // 😎 smiling with sunglasses
        "\uD83D\uDE2E" to 6, // 😮 surprised (mildly positive)
        "\uD83D\uDE31" to 2  // 😱 shocked/screaming (negative)
    )

    fun scoreFor(emoji: String): Int = map[emoji] ?: 5

    val allEmojis: List<String> = map.keys.toList()
}