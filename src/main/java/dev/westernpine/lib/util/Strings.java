package dev.westernpine.lib.util;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

    public static final char COLOR_CHAR = '\u00A7';
    public static final char ALT_COLOR_CHAR = '&';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");

    public static String color(String text) {
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == ALT_COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = java.lang.Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static String format(String text, Object... values) {
        for (int i = 0; i < values.length; i++) {
            text = text.replace("{" + i + "}", toSafeString(values[i]));
        }
        return text;
    }

    public static String formatAndColor(String text, Object... values) {
        return color(format(text, values));
    }

    public static String strip(String text) {
        return STRIP_COLOR_PATTERN.matcher(text).replaceAll("");
    }

    public static String toSafeString(Object value) {
        return value != null ? value.toString() : "null";
    }

    public static String join(String delimiter, Object... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.append(toSafeString(values[i]));

            if (i + 1 < values.length) {
                builder.append(delimiter);
            }
        }

        return builder.toString();
    }

    public static String getRemainderTime(int timer) {
        int minutes = timer / 60;
        int seconds = timer % 60;

        return minutes + (seconds < 10 ? ":0" : ":") + seconds;
    }

    public static String plural(int amount, String text) {
        return amount + " " + (amount == 1 ? text : text + "s");
    }

    public static String countlessPlural(int amount, String text) {
        return amount == 1 ? text : text + "s";
    }

    public static String getLastColor(String string) {
        string = strip(string);
        String[] splitted = string.split("");

        if (splitted.length == 0)
            return null;

        String color = null;
        for (int counter = splitted.length - 1; counter >= 0; counter--) {
            if (splitted[counter].equals("&")) {
                if (counter != splitted.length - 1) {
                    color = splitted[counter + 1];
                    break;
                }
            } else if (counter == 0) {
                break;
            }
        }
        return color;
    }

    public static String reverseColor(String string) {
        return string.replace(COLOR_CHAR + "", "&");
    }

    public static boolean doMatch(String string1, String string2) {
        return reverseColor(string1).equals(reverseColor(string2));
    }

    public static boolean resemblesNull(String string) {
        return (string == null || string.equals("") || string.equals(" "));
    }

    public static String capitalizeFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isLong(String string) {
        try {
            Long.parseLong(string);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isBoolean(String string) {
        try {
            Boolean.parseBoolean(string);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isUUID(String string) {
        return Pattern.compile("/^\\{?[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}‌​\\}?$/").matcher(string).matches();
    }

    public static boolean is(String string, String... strings) {
        try {
            for (String option : strings)
                if (string.equalsIgnoreCase(option))
                    return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isCase(String string, String... strings) {
        try {
            for (String option : strings)
                if (string.equals(option))
                    return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isAlpha(String string) {
        return string.matches("[a-zA-Z]+");
    }

    public static boolean isNumeric(String string) {
        return string.matches("[0-9]+");
    }

    public static boolean isAlphaNumeric(String string) {
        return string.matches("[a-zA-Z0-9]+");
    }

    public static boolean isRange(String string) {
        return getRangeMatcher(string).matches();
    }

    public static Matcher getRangeMatcher(String string) {
        return Pattern.compile("([\\d]+)-([\\d]+)").matcher(string);
    }

    public static boolean isURL(String string) {
        try {
            new URL(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String punctuate(String string) {
        return resemblesNull(string) ? string : string.matches("(.|?|!)$") ? string : string + ".";
    }

}
