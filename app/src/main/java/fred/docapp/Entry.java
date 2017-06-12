package fred.docapp;

public class Entry {


    public enum EntryType {
        DefineDir, File, ReferDir
    }

    long size;
    long pos;
    String fileName;
    String dirName;
    String normalizedName;
    boolean isEnabled = false;
    MLocate mloc;
    EntryType entryType;


    Entry(MLocate mloc, long size, long pos, String dirName, String fileName, EntryType entryType) {
        this.mloc = mloc;
	this.entryType = entryType;
        this.size = size;
	this.pos = pos;
	this.fileName = fileName;
        this.dirName = dirName;
        this.normalizedName = NameNormalizer.normalizeFileName(fileName);
    }

    public String toString() {
        return "entry:{size=" + size + ",pos=" + pos + ",dirName=" + dirName + ",fileName=" +
                fileName + ",entryType=" + entryType + "}";
    }
}
