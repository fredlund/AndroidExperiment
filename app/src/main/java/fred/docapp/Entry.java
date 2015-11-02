package fred.docapp;

public class Entry {


    public enum EntryType {
        DefineDir, File, ReferDir;
    }

    boolean isDir;
    long size;
    long pos;
    long pointer;
    String fileName;
    String dirName;
    boolean isEnabled = false;
    EntryType entryType;


    Entry(long size, long pos, long pointer, String dirName, String fileName, EntryType entryType) {
	this.entryType = entryType;
        this.size = size;
	this.pos = pos;
        this.pointer = pointer;
	this.fileName = fileName;
        this.dirName = dirName;
    }

    public String toString() {
        return "entry:{size=" + size + ",pos=" + pos + ",dirName=" + dirName + ",fileName=" +
                fileName + ",entryType=" + entryType + "}";
    }
}
