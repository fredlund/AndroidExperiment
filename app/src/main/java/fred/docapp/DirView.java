package fred.docapp;

import java.util.List;

/**
 * Created by fred on 2/11/15.
 */
public class DirView {
    List<Entry> entries;
    String dirName;

    DirView(String dirName, List<Entry> entries) {
        this.dirName = dirName;
        this.entries = entries;
    }
}
