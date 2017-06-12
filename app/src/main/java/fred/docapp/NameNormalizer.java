package fred.docapp;

/**
 * Created by fred on 9/06/17.
 */

public class NameNormalizer {
    public static String normalizeFileName(String fileName) {
        // First strip characters that do not interest us, then remove path, then strip character combinations
        return removeStrings(fileName,".cbr",".cbz",".rar",".epub",".pdf",".zip",".rtf",".jpg","BD.FR","BD","CRG","(",")"," ","-","_",".");
    }

    static String removeStrings(String origString, String... removes) {
        boolean changed;

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
