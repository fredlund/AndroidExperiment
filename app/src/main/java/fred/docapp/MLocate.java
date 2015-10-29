package fred.docapp;

import android.content.Context;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class MLocate {

    static byte[] tmp = null;
    static byte[] tmp2 = null;
    static int pos = 0;

    static public Entry[] find(String arg, Context context) {
        return find(new String[] {arg}, context);
    }

    static public Entry[] find(String args[], Context context) {
	BufferedInputStream in = null;
	tmp = new byte[8192];
	tmp2 = new byte[1];
	pos = 0;
        boolean stopping = false;
	Matcher[] m = new Matcher[args.length];
	for (int i=0; i<args.length; i++) {
	    Pattern p = Pattern.compile(args[i]);
	    m[i] = p.matcher("");
	}

        List<Entry> resultList = new ArrayList<Entry>();
	    String dirPrefix = null;

        try {
            in = new BufferedInputStream(context.openFileInput("locatedb"));
	    int result;
	    final int headerSize = 8+4+1+1+2;
	    byte header[] = new byte[headerSize];

	    result = in.read(header,0,headerSize);
	    if (result < headerSize) {
		System.out.println
		    ("*** Error: read "+result+" bytes from header");
		System.exit(1);
	    }
	    pos += headerSize;
	    long configurationBlockSize = bytesToLong(header,8,4);
	    System.out.println
		("configuration block is of size "+configurationBlockSize);
	    String pathName = getString(in);
	    System.out.println("path name is "+pathName);
	    if (skip(in,configurationBlockSize)<configurationBlockSize) {
		System.out.println
		    ("*** Error: could not read configuration block");
		System.exit(1);
	    }

	    do {
		long readSize = skip(in,8+4+4);
		if (readSize < 8+4+4) {
		    System.out.println("short read "+readSize+" stopping");
		    stopping = true;
		}

		String dirName = getString(in);
		//System.out.println("dirName is "+dirName);

		if (dirPrefix != null)
		    if (!dirName.startsWith(dirPrefix))
			dirPrefix = null;

		if (dirPrefix == null) {
		    if (find(m,dirName,true,dirName)) {
			dirPrefix = dirName;
                Entry entry = new Entry(true,0,pos,dirName,null);
                    resultList.add(entry);
			System.out.println(dirName+":"+"    at "+pos);
		    }
		}

		boolean in_dir = true;
		do {
		    byte fileType = getByte(in);
		    final int sizeSize = 4;
		    byte size[] = new byte[sizeSize];

		    result = in.read(size,0,sizeSize);
		    if (result < sizeSize) {
			System.out.println
			    ("*** Error: could not read file size");
			System.exit(1);
		    }
		    long fileSize = bytesToLong(size,0,sizeSize);

		    ++pos;
		    switch (fileType) {
		    case 0 :
			String fileName = getString(in);
			if (dirPrefix == null) {
			    if (find(m,fileName,false,dirName)) {
                    Entry entry = new Entry(false,fileSize,pos,dirName,fileName);
                    resultList.add(entry);
                }
			}
			//System.out.println("a normal file "+fileName+" of size "+fileSize);
			break;
		    case 1 :
			//System.out.println("a directory "+getString(in));
			getString(in);
			break;
		    case 2 :
			//System.out.println("<<<< end of dir >>>>");
			in_dir = false;
			break;
		    default:
			//System.out.println("wrong type "+fileType);
			System.exit(1);
			break;
		    }
		} while (in_dir);
	    } while (!stopping);
        } catch (Exception exc) {
            if (!(exc instanceof java.io.IOException)) {
                System.out.println
                        ("*** Error: exception " + exc + " raised");
                exc.printStackTrace();
                System.exit(1);
            }

	} finally {
            if (in != null) {
                try { in.close(); } catch (Exception exc) { };
            }
        }
        return resultList.toArray(new Entry[resultList.size()]);
    }

    static boolean find(Matcher[] m, String string, boolean isDir, String dirPrefix) {
	for (int i=0; i<m.length; i++) {
	    if (i!=0 && !isDir) string = dirPrefix+"/"+string;
	    m[i].reset(string);
	    if (!m[i].find()) return false;
	}
	return true;
    }

    static long skip(BufferedInputStream in, long nchars) throws java.io.IOException {
	int skipped=0;

	while (nchars>0) {
	    long skippedThisTime = in.skip(nchars);
	    nchars-=skippedThisTime;
	    skipped+=skippedThisTime;
	};
	pos += skipped;
	return skipped;
    }

    static long bytesToLong(byte[] arr, int offset, int len) {
	long value = 0;
	for (int i = 0; i < len; i++)
	    {
		value = (value << 8) + (arr[i+offset] & 0xff);
	    }
	return value;
    }

    static String getString(BufferedInputStream in) throws java.io.IOException {
	int index = 0;
	int len = 0;
	byte b;

	do {
	    b = getByte(in);
	    if (b == 0) {
		pos += (len+1);
		return new String(tmp,0,len);
	    }
	    if (b == -1) {
		System.out.println
		    ("*** Error: eof before end-of-string");
		System.exit(1);
	    }
	    tmp[index++] = b;
	    len++;
	} while (true);
    }

    static Byte getByte(BufferedInputStream in) throws java.io.IOException {
	int readSize = 0;
	while (readSize < 1) {
	    readSize = in.read(tmp2,0,1);
	    if (readSize < 0) {
            throw new java.io.IOException();
	    }
	}
	return tmp2[0];
    }
}

