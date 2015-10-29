package fred.docapp;

/**
 * Created by fred on 29/10/15.
 */
public class Entry {
    boolean isDir;
    long size;
    long pos;
    String fileName;
    String dirName;

    Entry(boolean isDir, long size, long pos, String dirName, String fileName) {
	this.isDir = isDir;
        this.size = size;
	this.pos = pos;
	this.fileName = fileName;
        this.dirName = dirName;
    }
}

