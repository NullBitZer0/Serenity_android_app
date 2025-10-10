package com.example.serenity_mad_le3.util

object EmojiMapper {
    private val map = mapOf(
        "\uD83E\uDD2C" to -10, // furious (🤬)
        "\uD83D\uDE21" to -9,  // enraged (😡)
        "\uD83D\uDE31" to -9,  // terrified (😱)
        "\uD83D\uDE20" to -8,  // angry (😠)
        "\uD83D\uDE2D" to -8,  // devastated (😭)
        "\uD83D\uDE22" to -7,  // tearful (😢)
        "\uD83D\uDE25" to -6,  // anxious relief (😥)
        "\uD83D\uDE1E" to -5,  // disappointed (😞)
        "\uD83D\uDE14" to -4,  // pensive (😔)
        "\uD83D\uDE1F" to -3,  // worried (😟)
        "\uD83D\uDE12" to -2,  // unamused (😒)
        "\uD83D\uDE11" to -1,  // expressionless (😑)
        "\uD83D\uDE10" to 0,   // neutral baseline (😐)
        "\uD83D\uDE2E" to 1,   // curious surprise (😮)
        "\uD83D\uDE42" to 2,   // slight smile (🙂)
        "\uD83D\uDE0C" to 3,   // relieved (😌)
        "\uD83D\uDE0A" to 4,   // warm smile (😊)
        "\uD83D\uDE03" to 5,   // grinning (😃)
        "\uD83D\uDE0E" to 6,   // confident (😎)
        "\uD83D\uDE04" to 7,   // big grin (😄)
        "\uD83D\uDE01" to 8,   // beaming (😁)
        "\uD83D\uDE06" to 9,   // laughing (😆)
        "\uD83D\uDE02" to 9,   // joyful tears (😂)
        "\uD83D\uDE18" to 8,   // playful affection (😘)
        "\uD83E\uDD70" to 10,  // loved-up (🥰)
        "\uD83D\uDE0D" to 10   // heart eyes (😍)
    )

    // The mood score feeds charts and summaries; default to neutral when the emoji is unrecognized.
    fun scoreFor(emoji: String): Int = map[emoji] ?: 0

    // Expose the same ordering the picker renders so moods and scores stay aligned.
    val allEmojis: List<String> = map.keys.toList()
}
