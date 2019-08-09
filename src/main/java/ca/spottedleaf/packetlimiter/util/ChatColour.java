package ca.spottedleaf.packetlimiter.util;

import org.bukkit.ChatColor;

/**
 * Use this class instead of {@link ChatColor} for compile-time string constants.
 */
public final class ChatColour {

    // javadoc taken from org.bukkit.ChatColor

    /**
     * Represents black.
     */
    public static final String BLACK = ChatColor.COLOR_CHAR + "0";

    /**
     * Represents dark blue.
     */
    public static final String DARK_BLUE = ChatColor.COLOR_CHAR + "1";

    /**
     * Represents dark green.
     */
    public static final String DARK_GREEN = ChatColor.COLOR_CHAR + "2";

    /**
     * Represents dark blue (aqua).
     */
    public static final String DARK_AQUA = ChatColor.COLOR_CHAR + "3";

    /**
     * Represents dark red.
     */
    public static final String DARK_RED = ChatColor.COLOR_CHAR + "4";

    /**
     * Represents dark purple.
     */
    public static final String DARK_PURPLE = ChatColor.COLOR_CHAR + "5";

    /**
     * Represents gold.
     */
    public static final String GOLD = ChatColor.COLOR_CHAR + "6";

    /**
     * Represents grey.
     */
    public static final String GREY = ChatColor.COLOR_CHAR + "7";

    /**
     * Represents dark grey.
     */
    public static final String DARK_GREY = ChatColor.COLOR_CHAR + "8";

    /**
     * Represents blue.
     */
    public static final String BLUE = ChatColor.COLOR_CHAR + "9";

    /**
     * Represents green.
     */
    public static final String GREEN = ChatColor.COLOR_CHAR + "a";

    /**
     * Represents aqua.
     */
    public static final String AQUA = ChatColor.COLOR_CHAR + "b";

    /**
     * Represents red.
     */
    public static final String RED = ChatColor.COLOR_CHAR + "c";

    /**
     * Represents light purple.
     */
    public static final String LIGHT_PURPLE = ChatColor.COLOR_CHAR + "d";

    /**
     * Represents yellow.
     */
    public static final String YELLOW = ChatColor.COLOR_CHAR + "e";

    /**
     * Represents white.
     */
    public static final String WHITE = ChatColor.COLOR_CHAR + "f";

    /**
     * Represents magical characters that change around randomly.
     */
    public static final String MAGIC = ChatColor.COLOR_CHAR + "k";

    /**
     * Makes the text bold.
     */
    public static final String BOLD = ChatColor.COLOR_CHAR + "l";

    /**
     * Makes a line appear through the text.
     */
    public static final String STRIKETHROUGH = ChatColor.COLOR_CHAR + "m";

    /**
     * Makes the text appear underlined.
     */
    public static final String UNDERLINE = ChatColor.COLOR_CHAR + "n";

    /**
     * Makes the text italic.
     */
    public static final String ITALIC = ChatColor.COLOR_CHAR + "o";

    /**
     * Resets all previous chat colors or formats.
     */
    public static final String RESET = ChatColor.COLOR_CHAR + "r";

    private ChatColour() {
        throw new RuntimeException();
    }
}