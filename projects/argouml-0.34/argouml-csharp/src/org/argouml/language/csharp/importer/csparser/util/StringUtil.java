package org.argouml.language.csharp.importer.csparser.util;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 5:43:07 PM
 */
public class StringUtil {
    public static String removeChar(String string, char[] toRemove) {
        for (int i = 0; i < toRemove.length; i++) {
            if (string.charAt(string.length()-1)==toRemove[i]) {
                string = string.substring(0, string.length() - 1);
                return string;
            }
        }
        return string;
    }
}
