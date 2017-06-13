package fred.docapp;

/**
 * Created by fred on 9/06/17.
 */

public class NameNormalizer {
    public static String normalizeFileName(String fileName) {
        // First strip characters that do not interest us, then remove path, then strip character combinations
        return removeStrings(fileName,"bd fr","bdfr","bd","crg");
    }

    static String removeStrings(String origString, String... removes) {
        boolean changed;

        String strippedString = stripSymbols(origString);
        //System.out.println("origString="+origString+" strippedString="+strippedString);
        do {
            changed = false;
            for (String removeString : removes) {
                if (strippedString.contains(removeString)) {
                    strippedString = strippedString.replace(removeString,"");
                    changed = true;
                    break;
                }
            }
        } while (changed);
        String finalString = stripSymbols(strippedString);
        //System.out.println("cutString="+strippedString+" final="+finalString);
        return finalString;
    }

    static String stripSymbols(String origString) {
        StringBuilder builder = new StringBuilder(origString.toLowerCase());
        int lastChar = -1;
        boolean skippedSymbols = false;
        boolean outputSymbol = false;

        int i = 0;
        while (i < builder.length()) {
            int ch = (int) builder.charAt(i);
            if (ch == 48 && (!outputSymbol || skippedSymbols))
                builder.deleteCharAt(i);
            else if (!isLetterOrDigit(ch)) {
                skippedSymbols = true;
                builder.deleteCharAt(i);
            } else {
                if (skippedSymbols && outputSymbol) builder.insert(i,' ');
                skippedSymbols = false;
                outputSymbol = true;
                i++;
            }
            lastChar = ch;
        }
        return builder.toString();
    }

    static boolean isLetterOrDigit(int ch) {
        return (ch >= 48 && ch <= 57) || (ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122);
    }
}
