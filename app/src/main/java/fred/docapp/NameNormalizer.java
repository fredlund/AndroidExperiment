package fred.docapp;

/**
 * Created by fred on 9/06/17.
 */

public class NameNormalizer {
    public static String normalizeFileName(String fileName) {
        // First strip characters that do not interest us, then remove path, then strip character combinations
        return removeStrings(fileName,"cbr","cbz","rar","epub","pdf","zip","rtf","jpg","bdfr","bd","crg");
    }

    static String removeStrings(String origString, String... removes) {
        boolean changed;

        StringBuilder builder = new StringBuilder(origString.toLowerCase());

        for (int i=builder.length()-1; i>=0; i--) {
            int ch = (int) builder.charAt(i);
            if ((ch >= 48 && ch <= 57) || (ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122))
                continue;
            else
                builder.deleteCharAt(i);
        }
        origString = builder.toString();
        do {
            changed = false;
            for (String removeString : removes) {
                if (origString.contains(removeString)) {
                    origString = origString.replace(removeString,"");
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return origString;
    }
}
