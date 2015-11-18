package fred.docapp;

import android.content.Context;

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

interface FileOps {
    public int read(byte[] buffer, int offset, int size) throws java.io.IOException;
	public void close() throws java.io.IOException;
}

class FileOpsClass extends BufferedInputStream implements FileOps {
	FileOpsClass(FileInputStream f) {
		super(f);
	}
}

class FileOpsClassB extends RandomAccessFile implements FileOps {
	FileOpsClassB(String f,String m) throws FileNotFoundException {
		super(f,m);
	}
}

public class MLocate {

	String file = null;
	String root = null;
	byte[] tmp = null;
	byte[] tmp2 = null;
	Entry[] entries = null;
	int pos = 0;

	public MLocate() {
		this.file = "locatedb_pnt";
	}

	public MLocate(String file) {
		this.file = "locatedb_pnt";
	}

	public Entry[] open(Entry entry) {
		return null;
	}

	public String root() { return root; }

	public Entry[] find(String arg, Context context) {
		return find(new String[]{arg}, context);
	}

	public Entry[] find(String args[], Context context) {
		FileOpsClass in = null;
		tmp = new byte[8192];
		tmp2 = new byte[1];
		boolean stopping = false;
		Matcher[] m = new Matcher[args.length];
		pos = 0;

		System.out.println("file is "+file);
		for (int i = 0; i < args.length; i++) {
			Pattern p = Pattern.compile(args[i]);
			m[i] = p.matcher("");
		}

		List<Entry> resultList = new ArrayList<Entry>();
		String dirPrefix = null;

		try {
			in = new FileOpsClass(context.openFileInput(file));
			int result;
			final int headerSize = 8 + 4 + 1 + 1 + 2;
			byte header[] = new byte[headerSize];

			result = read(in,header, 0, headerSize);
			if (result < headerSize) {
				System.out.println
						("*** Error: read " + result + " bytes from header");
				System.exit(1);
			}
			pos += headerSize;
			long configurationBlockSize = bytesToLong(header, 8, 4);
			System.out.println
					("configuration block is of size " + configurationBlockSize);
			String pathName = getString(in);
			System.out.println("path name is " + pathName);
			root = pathName;
			if (skip(in, configurationBlockSize) < configurationBlockSize) {
				System.out.println
						("*** Error: could not read configuration block");
				System.exit(1);
			}

			do {
				long readSize = skip(in, 8 + 4 + 4);
				if (readSize < 8 + 4 + 4) {
					System.out.println("short read " + readSize + " stopping");
					stopping = true;
				}

				if (!stopping) {
					String dirName = getString(in);
					if (dirName.length() > root.length())
						dirName = dirName.substring(root.length() + 1);
					//System.out.println("dirName is "+dirName);

					if (dirPrefix != null)
						if (!dirName.startsWith(dirPrefix))
							dirPrefix = null;

					if (dirPrefix == null) {
						if (find(m, dirName, true, dirName)) {
							dirPrefix = dirName;
							Entry entry = new Entry(this, 0, pos, null, dirName, Entry.EntryType.DefineDir);
							resultList.add(entry);
							System.out.println(dirName + ":" + "    at " + pos);
						}
					}

					scan_dir(in, dirPrefix != null, false, dirName, resultList, m);
				}
			} while (!stopping);
		} catch (Exception exc) {
			if (!(exc instanceof java.io.IOException)) {
				System.out.println
						("*** Error: exception " + exc + " raised");
				exc.printStackTrace();
				throw new RuntimeException();
			}

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception exc) {
				}
				;
			}
		}
		entries = resultList.toArray(new Entry[resultList.size()]);
		return entries;
	}

	Entry[] read_dir(Context context, Entry entry) {
		try {
			FileOpsClassB in = new FileOpsClassB(context.getFilesDir()+"/locatedb_pnt", "rw");
			long seek_pos = entry.pos;
			System.out.println("seeking to "+seek_pos);
			in.seek(seek_pos);
			List<Entry> result = new ArrayList<Entry>();
			String dirName = entry.dirName+"/"+entry.fileName;
			scan_dir(in, false, true, dirName, result, null);
			in.close();
			return result.toArray(new Entry[result.size()]);
		} catch (FileNotFoundException exc) { System.out.println("FileNotFound"); return null; }
		catch (IOException exc) { System.out.println("IOException"); return null; }
	}

	void scan_dir(FileOps in, boolean skipping, boolean exploring, String dirName, List<Entry> resultList, Matcher[] m) throws java.io.IOException {

	boolean in_dir = true;
	do

	{
		byte fileType = getByte(in);
		final int sizeSize = 4;
		byte size[] = new byte[sizeSize];

		int result = read(in,size, 0, sizeSize);
		if (result < sizeSize) {
			System.out.println
					("*** Error: could not read file size at dir "+dirName+" read="+result+" " +
							"should be "+sizeSize+" pos="+pos+" fileType was "+fileType);
			throw new RuntimeException();
		}
		pos += sizeSize;
		long fileSize = bytesToLong(size, 0, sizeSize);

		switch (fileType) {
			case 0:
				String fileName = getString(in);
				if (!skipping) {
					if (exploring || (!skipping && find(m, fileName, false, dirName))) {
						System.out.println("adding file "+fileName+" dirName="+dirName+" root="+root);
						Entry entry = new Entry(this,fileSize, pos, dirName, fileName, Entry.EntryType
								.File);
						System.out.println("in scan_dir: entry: "+entry);
						resultList.add(entry);
					}
				}
				//System.out.println("a normal file "+fileName+" of size "+fileSize);
				break;
			case 1:
				//System.out.println("a directory "+getString(in));
				String dirRef = getString(in);
				byte pointer[] = new byte[4];
				read(in,pointer, 0, 4);
				pos += 4;
				long lpointer = bytesToLong(pointer,0,4);
				if (exploring) {
					System.out.println("adding directory "+dirRef);
					Entry entry = new Entry(this,fileSize, lpointer, dirName, dirRef, Entry.EntryType
							.ReferDir);
					System.out.println("in scan_dir: entry: "+entry);
					resultList.add(entry);
				}
				break;
			case 2:
				//System.out.println("<<<< end of dir >>>>");
				in_dir = false;
				break;
			default:
				//System.out.println("wrong type "+fileType);
				System.exit(1);
				break;
		}
	}	while(in_dir);
}

	boolean find(Matcher[] m, String string, boolean isDir, String dirPrefix) {
	for (int i=0; i<m.length; i++) {
	    if (i!=0 && !isDir) string = dirPrefix+"/"+string;
	    m[i].reset(string);
	    if (!m[i].find()) return false;
	}
	return true;
    }


   long bytesToLong(byte[] arr, int offset, int len) {
	long value = 0;
	for (int i = 0; i < len; i++)
	    {
		value = (value << 8) + (arr[i+offset] & 0xff);
	    }
	return value;
    }

	int read(FileOps f, byte[] buf, int offset, int len) throws IOException {
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
    long skip(FileOps in,long size) throws java.io.IOException {
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

    String getString(FileOps in) throws java.io.IOException {
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

    byte getByte(FileOps in) throws java.io.IOException {
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

