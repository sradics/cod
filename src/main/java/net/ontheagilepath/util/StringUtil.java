package net.ontheagilepath.util;

import java.util.List;

/**
 * Created by sebastianradics on 12.03.17.
 */
public class StringUtil {
    public static String convertStringList(List<String> list){
        StringBuilder result = new StringBuilder();
        boolean append = false;
        for (String s : list) {
            if (append){
                result.append(",");
            }
            append = true;
            result.append(s);
        }
        return result.toString();
    }
}
