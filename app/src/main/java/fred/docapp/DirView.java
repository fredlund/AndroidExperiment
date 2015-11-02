package fred.docapp;

/**
 * Created by fred on 2/11/15.
 */
public class DirView {
    EntryAdapter adapter;
    Entry[] entries;
    String dirName;

    DirView(String dirName, EntryAdapter adapter, Entry[] entries) {
        this.dirName = dirName;
        this.adapter = adapter;
        this.entries = entries;
    }
}
