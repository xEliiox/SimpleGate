package xeliox.simplegate.utils;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class Util {

    private static final Pattern PATTERN_ENUM_SPLIT = Pattern.compile("[\\s_]+");

    private static String getNicedEnumString(String str) {
        List<String> parts = new ArrayList<>();
        for (String part : PATTERN_ENUM_SPLIT.split(str.toLowerCase(Locale.getDefault()))) {
            parts.add(upperCaseFirst(part));
        }
        return implode(parts);
    }

    private static <T extends Enum<T>> String getNicedEnum(T enumObject) {
        return getNicedEnumString(enumObject.name());
    }

    public static String getMaterialName(Material material) {
        return getNicedEnum(material);
    }

    private static String upperCaseFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1);
    }

    private static String implode(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (String s : list) {
            result.append(s);
        }
        return result.toString();
    }


    public static String implodeCommaAndDot(
            Collection<?> objects,
            String comma,
            String and,
            String dot
    ) {
        if (objects == null || objects.isEmpty()) {
            return "";
        }

        List<String> list = new ArrayList<>();
        for (Object obj : objects) {
            list.add(String.valueOf(obj));
        }

        if (list.size() == 1) {
            return list.get(0) + dot;
        }

        if (list.size() == 2) {
            return list.get(0) + and + list.get(1) + dot;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                if (i == list.size() - 1) {
                    sb.append(and);
                } else {
                    sb.append(comma);
                }
            }
            sb.append(list.get(i));
        }

        sb.append(dot);
        return sb.toString();
    }


    public static String implodeCommaAnd(
            Collection<?> objects,
            String comma,
            String and
    ) {
        return implodeCommaAndDot(objects, comma, and, "");
    }




}
