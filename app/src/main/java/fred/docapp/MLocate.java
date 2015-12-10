package fred.docapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.DataInput;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.util.Stack;

public class MLocate {
    
    String file = null;
    int bufSize;
    String root = null;
    byte[] tmp = null;
    byte[] tmp2 = null;
    int pos = 0;
    static byte[] dirSaved = null;
    static byte[] entrySaved = null;
	Context context;
    boolean matchDotDir = false;
	boolean matchDotFile = false;

    static public String localLibraryFile(String library) {
	return library+".ldp";
    }
	static public String fullLocalLibraryFile(Context context, String library) {
	return context.getFilesDir().getAbsolutePath()+"/"+library+".ldp";
    }

    public MLocate(String library, int bufSize, Context context) throws IOException {
	this.bufSize = bufSize;
	this.dirSaved = new byte[4096];
	this.entrySaved = new byte[4096];
		this.context = context;
		this.file = fullLocalLibraryFile(context,library);
		tmp = new byte[8192];
		tmp2 = new byte[1];
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		matchDotDir = Boolean.parseBoolean(prefs.getString("show_hidden_directories","false"));
		matchDotFile = Boolean.parseBoolean(prefs.getString("show_hidden_files", "false"));
	//System.out.println("mlocate: library "+library+" is stored in "+this.file);
    }
    
    public String root() { return root; }
    
    public List<Entry> find(String arg) throws IOException {
	MySlowReader reader = null;
	
	//System.out.println("file is "+file);
	Term<byte[]> matchTerm = null;
	try { matchTerm = LogicMatcher.convert(Term.parse(arg)); }
	catch (ParseException exc) {
	    System.out.println("could not parse "+arg+" due to: "+exc);
	    throw new IOException();
	}
	System.out.println("will search for "+matchTerm);

	List<Entry> resultList = new ArrayList<Entry>();
	String dirPrefix = null;
	
	try {
		System.out.println("bufSize is "+bufSize);
	    reader = new MySlowReader(file,bufSize);
	    reader.skip(8);
	    long configurationBlockSize = reader.getLong();
	    reader.skip(4);
	    //System.out.println("current pos is "+reader.current());
	    String pathName = reader.getString();
	    System.out.println("path name is " + pathName);
	    System.out.println("current pos is "+reader.current());
	    root = pathName;
	    reader.skip(configurationBlockSize);
	    
	    while (!reader.atEOF()) {
		//System.out.println("dir loop: pos is "+reader.current());
		reader.skip(8+4+4);

		int dirSavedLen;
		if ((dirSavedLen = LogicMatcher.match(reader,matchTerm,dirSaved,true,null,0)) >= 0) {
		    String dirName = reader.getString(dirSaved,dirSavedLen);
		    Entry entry = new Entry(this, 0, reader.current(), null, dirName,Entry.EntryType.DefineDir);
		    resultList.add(entry);
		    //System.out.println("found dir "+dirName);
		    skip_dir(reader);
		} else {
		    //System.out.println("no match for "+reader.getString(dirSaved,Math.abs(dirSavedLen)));
		    reader_scan_dir(reader, dirSaved,Math.abs(dirSavedLen), resultList, matchTerm);
		}
	    } 
	    return resultList;
	} finally {
	    if (reader != null) {
		try { reader.close(); } catch (Exception exc) { }
	    }
	}
    }
    
    void reader_scan_dir(MySlowReader reader, byte[] dirNameBytes, int dirNameLen, List<Entry> resultList, Term<byte[]> matchTerm) throws java.io.IOException {
	
	String dirName = null;
	boolean in_dir = true;

	do {
	    //System.out.println("reader_scan_dir: pos is "+reader.current());
	    byte fileType = reader.nextByte();
	    //System.out.println("fileType = "+fileType);
	    switch (fileType) {
	    case 0: {
		long fileSize = reader.getLong();	
		//System.out.println("fileType 0: pos = "+reader.current());
		int entrySavedLen;
		if ((entrySavedLen = LogicMatcher.match(reader,matchTerm,entrySaved,false,dirNameBytes,dirNameLen)) >= 0) {
		    String fileName = 
			reader.getString(entrySaved,entrySavedLen);
		    if (dirName == null)
			dirName = reader.getString(dirNameBytes, dirNameLen);

		    //System.out.println("adding file "+fileName+" dirName="+dirName+" root="+root);
		    Entry entry = new Entry(this,fileSize, 0, dirName, fileName, Entry.EntryType.File);
		    resultList.add(entry);
		}
		//else System.out.println("not adding "+reader.getString(entrySaved,Math.abs(entrySavedLen)));
		break;		 
	    }
	    case 1: {
		reader.skip(4);
		//System.out.println("fileType 1: pos = "+reader.current()+" will skip dir "
				   //+reader.getString());
		//		   );
		reader.skipString();
		reader.skip(4);
		break;
	    }
	    case 2: {
		//System.out.println("reader_scan_dir: "+"<<<< end of dir >>>>");
		reader.skip(4);
		in_dir = false;
		break;
	    }
	    default: {
		if (dirName == null)
		    dirName = reader.getString(dirNameBytes, dirNameLen);
		System.out.println("dirName = "+dirName);
		throw new IOException("reader_scan_dir: wrong type "+fileType);
	    } 
	    }
	} while(in_dir);
    }
    
    void skip_dir(MySlowReader reader)
	throws java.io.IOException {

	Stack<Integer> stack = new Stack<Integer>();
	boolean firstTime = true;
	int numSubDirs;
	stack.push(1);
	
	//System.out.println("skip_dir");
	do {
	    boolean inDir = true;

	    do {
		numSubDirs = stack.pop();
	    } while (numSubDirs==0 && !stack.isEmpty());

	    if (numSubDirs==0) return;
	    else stack.push(numSubDirs-1);
	    
	    //System.out.println("numSubDirs = "+numSubDirs+" level = "+stack.size());

	    if (!firstTime) {
		if (reader.atEOF())
		    return;
		reader.skip(8+4+4);
		reader.skipString();
	    } else firstTime=false;

	    numSubDirs = 0;
	    do {
		byte fileType = reader.nextByte();
		//System.out.println
		//  ("skip: fileType="+fileType+" current="+reader.current());
		reader.skip(4);
		//System.out.println("after reader.skip");
		
		switch (fileType) {
		case 0:
		    reader.skipString();
		    //System.out.println("skipping file "+reader.getString());
		    break;
		case 1:
		    ++numSubDirs;
		    //System.out.println("skipping dir "+reader.getString());
		    reader.skipString();
		    reader.skip(4);
		    break;
		case 2:
		    //System.out.println("skip: <<<< end of dir >>>>");
		    inDir = false;
		    break;
		default:
		    throw new IOException("skip_dir: wrong file type "+fileType);
		}
	    } while (inDir);
	    
	    if (numSubDirs > 0) 
		stack.push(numSubDirs);
	    
	} while (!stack.isEmpty());
    }
    
    List<Entry> read_dir(Entry entry) {
	try {
	    RandomAccessFile in = new RandomAccessFile(this.file, "rw");
	    long seek_pos = entry.pos;
	    System.out.println("seeking to "+seek_pos);
	    in.seek(seek_pos);
	    List<Entry> result = new ArrayList<Entry>();
	    String dirName = path_compose(entry.dirName,entry.fileName);
		System.out.println("calling scan_dir");
		scan_dir(in, dirName, result);
	    in.close();
	    return result;
	} catch (FileNotFoundException exc) { System.out.println("FileNotFound"); return null; }
	catch (IOException exc) { System.out.println("IOException"); return null; }
    }
    
    String path_compose(String path1, String path2) {
	if (path1==null) return path2;
	if (path2==null) return path1;
	if (path1.equals("")) return path2;
	else return path1+"/"+path2;
    }

    void scan_dir(RandomAccessFile in, String dirName, List<Entry> resultList) throws java.io.IOException {
	
	boolean in_dir = true;
	do {
		System.out.flush();
	    byte fileType = getByte(in);
	    final int sizeSize = 4;
	    byte size[] = new byte[sizeSize];

	    int result = read(in,size,0,sizeSize);
	    if (result < sizeSize) {
		System.out.println
		  ("*** Error: could not read file size at dir "+dirName+" read="+result+" " +
		  "should be "+sizeSize+" pos="+pos+" fileType was "+fileType);
		throw new RuntimeException();
	    }
	    pos += sizeSize;
	    long fileSize = bytesToLong(size, 0, sizeSize);
	    
	    switch (fileType) {
	    case 0: {
		String fileName = getString(in);
		//System.out.println("adding file "+fileName+" dirName="+dirName+" root="+root);
		Entry entry = new Entry(this,fileSize, pos, dirName, fileName, Entry.EntryType.File);
		resultList.add(entry);
		break;
	    }
	    case 1: {
		String dirRef = getString(in);
		byte pointer[] = new byte[4];
		read(in,pointer, 0, 4);
		pos += 4;
		long lpointer = bytesToLong(pointer,0,4);
		//System.out.println("adding directory "+dirRef);
		Entry entry = new Entry(this,fileSize, lpointer, dirName, dirRef, Entry.EntryType.ReferDir);
		resultList.add(entry);
		break;
	    }
	    case 2: {
		//System.out.println("<<<< end of dir >>>>");
		in_dir = false;
		break;
	    }
	    default: {
			System.out.println("wrong fileType="+fileType);
		throw new IOException("scan_dir");
	    }
	    }
	} while(in_dir);
    }
    
    long bytesToLong(byte[] arr, int offset, int len) {
	long value = 0;
	for (int i = 0; i < len; i++)
	    {
		value = (value << 8) + (arr[i+offset] & 0xff);
	    }
	return value;
    }
    
    int read(RandomAccessFile f, byte[] buf, int offset, int len) throws IOException {
	int bytes_to_read = len;
	do {
	    int bytes_read = f.read(buf,offset,bytes_to_read);
	    if (bytes_read < 0) return bytes_read;
	    bytes_to_read -= bytes_read;
	    offset += bytes_read;
	} while (bytes_to_read > 0);
	return len;
    }
    
    byte[] longToByteArray(long value) {
	return new byte[] {
	    (byte) (value >> 24),
	    (byte) (value >> 16),
	    (byte) (value >> 8),
	    (byte) value
	};
    }
    long skip(RandomAccessFile in,long size) throws java.io.IOException {
	int index = 0;
	byte b;
	
	while (index++ < size) {
	    try { b = getByte(in); }
	    catch (IOException exc) {
		return index;
	    }
	}
	return index;
    }
    
    String getString(RandomAccessFile in) throws java.io.IOException {
	int index = 0;
	int len = 0;
	byte b;
	
	do {
	    b = getByte(in);
	    if (b == 0)
		return new String(tmp,0,len);
	    tmp[index++] = b;
	    len++;
	} while (true);
    }
    
    byte getByte(RandomAccessFile in) throws java.io.IOException {
	int readSize = 0;
	while (readSize < 1) {
	    readSize = in.read(tmp2,0,1);
	    if (readSize < 0) {
		System.out.println("EOF at getByte");
		new Exception().printStackTrace();
		System.out.flush();
		throw new java.io.IOException();
	    }
	    pos += 1;
	}
	return tmp2[0];
    }
}

