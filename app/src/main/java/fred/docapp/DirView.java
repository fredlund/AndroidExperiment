package fred.docapp;

import java.util.List;

/**
 * Created by fred on 2/11/15.
 */
public class DirView {
    EntryAdapter adapter;
    List<Entry> entries;
    String dirName;

    DirView(String dirName, EntryAdapter adapter, List<Entry> entries) {
        this.dirName = dirName;
        this.adapter = adapter;
        this.entries = entries;
    }
}
