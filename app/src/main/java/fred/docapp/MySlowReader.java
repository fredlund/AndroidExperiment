package fred.docapp;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.*;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;

/**
 * Created by fred on 20/11/15.
 */
public class MySlowReader {
    final int defaultBufSize = 8192;
    FileInputStream in = null;

    // Size of a buffer
    int bufSize = 0;
    
    // Size of file
    long fileSize = 0;

    // Points to the current buffer
    Buffer currentBuffer;
    // Points to other buffer
    Buffer otherBuffer;

	Context context;

    // Position in the file, i.e., next position that will be read
    long currentPos = 0;   // 0..

    public MySlowReader(String fileName, int bufSize) throws IOException {
	if (bufSize < 4096) throw new IOException("bufSize must be at least "+4096);
        File myFile = new File(fileName);
	this.fileSize = myFile.length();
        if (fileSize <= 0) throw new IOException(myFile.getName()+" is empty");
        this.in = new FileInputStream(myFile);
		//this.in = context.openFileInput(fileName);
        this.bufSize = bufSize;
		this.context = context;
	currentBuffer = new Buffer(in,bufSize,fileSize);
	otherBuffer = new Buffer(in,bufSize,fileSize);
        currentBuffer.read(0);
	currentPos = 0;
    }

    long getLong() throws IOException {
	Buffer myCurrentBuffer = currentBuffer;
	
	if ((myCurrentBuffer.last-currentPos) > 5) {
	    byte[] buf = myCurrentBuffer.buf;
	    long value = 0;
	    int start = (int) (currentPos-myCurrentBuffer.first);
	    for (int i = start; i < start+4; i++)
		value = (value << 8) + (buf[i] & 0xff);
	    seek(currentPos+4);
	    return value;
	}
	else {
	    long value = 0;
	    for (int i = 0; i < 4; i++)
                value = (value << 8) + (nextByte() & 0xff);
	    return value;
	}
    }

    // copies a string to a byte buffer; returns size of string copied
    int copyStringToBuf(byte toBuf[]) throws IOException {
	int toIndex = 0;

	for (int round = 0; round < 2; round++) {
	    byte[] buf = currentBuffer.buf;
	    int index = (int) (currentPos-currentBuffer.first);
	    long last = currentBuffer.last;

	    while (currentPos <= last) {
		byte bs = buf[index];
		++currentPos;
		if (bs == 0) {
		    if (currentPos > last) moveToCurrentBuffer();
		    return toIndex;
		} else {
		    toBuf[toIndex++] = bs;
		    ++index;
		}
	    }
	    moveToCurrentBuffer();
	}
	throw new IOException("copyStringToBuf");
    }

    void moveToCurrentBuffer() throws IOException {
	//System.out.println
	//  ("moveToCurrentBuffer: currentPos="+currentPos+
	//   " fileSize="+fileSize+" currentBuffer.last="+
	//     buffers[currentBufferIndex].last);
	if (currentPos >= fileSize) return;

	long otherBufferLast;
	do {
	    long currentBufferLast = currentBuffer.last;

	    if (currentBufferLast >= currentPos)
		return;

	    if (otherBuffer.first < currentBufferLast)
		otherBuffer.read(currentBufferLast+1);
	    otherBufferLast = otherBuffer.last;
	    if (otherBufferLast <= currentBufferLast)
		throw new IOException("strange");

	    Buffer swap = otherBuffer;
	    otherBuffer = currentBuffer;
	    currentBuffer = swap;
	    //System.out.println("currentPos = "+currentPos+" otherBufferLast = "+otherBufferLast);
	} while (currentPos > otherBufferLast);
    }


	static public String printBs(byte[] bytes, int max) {
	StringBuilder builder = new StringBuilder();
	for (int i=0; i<max; i++)
	    builder.append((char) bytes[i]);
	return builder.toString();
    }


	// copies a string to a byte buffer; returns size of string copied
	int isDotDir(byte toBuf[]) throws IOException {
		int toIndex = 0;
		boolean hasDot = false;
		boolean beginDir = false;

		for (int round = 0; round < 2; round++) {
			byte[] buf = currentBuffer.buf;
			int index = (int) (currentPos-currentBuffer.first);
			long last = currentBuffer.last;

			while (currentPos <= last) {
				byte bs = buf[index];
				++currentPos;
				if (bs == 0) {
					if (currentPos > last) moveToCurrentBuffer();
					if (hasDot) return toIndex;
					else return -toIndex;
				} else {
					toBuf[toIndex++] = bs;
					++index;
					if (bs == '/') {
						hasDot = false;
						beginDir = true;
					} else if (beginDir && bs == '.') {
						hasDot = true;
						beginDir = false;
					}
					else
						beginDir = false;
				}
			}
			moveToCurrentBuffer();
		}
		throw new IOException("copyStringToBuf");
	}

    int bytesMatch(byte[] bytes, boolean doSave, byte[] save, int bsMax, boolean caseConvert, boolean startAt0, boolean finishAtEnd) throws IOException {
	//System.out.println("pos is "+current());
	if (doSave) bsMax = copyStringToBuf(save);
	//System.out.println("bsMax is "+bsMax+": "+printBs(save,bsMax));
	//System.out.println("pos is "+current());
	//System.out.println("bytes is "+printBs(bytes,bytes.length)+" bytes.length="+bytes.length);

	int bsStart = 0;
	int bpMax = bytes.length;
	
	while ((bsMax - bsStart) >= bpMax) {
	    int bsIndex = bsStart;
	    int index = 0;
	    byte bs = 1;
	    byte bp = 2;

            while (index < bpMax) {
                bs = save[bsIndex++];
                bp = bytes[index++];
		if (bs == 0) return -bsMax;
		if ((caseConvert && !compare(bs,bp))
		    || (!caseConvert && bs != bp))
		    break;
            } 
	    
	    if ((caseConvert && !compare(bs,bp))
		|| (!caseConvert && bs != bp)) {
					if (startAt0) return -bsMax;
					else ++bsStart;
				    }
			    else {
					if (!finishAtEnd || bsIndex==bsMax) return bsMax;
					else return -bsMax;
				    }

	}
	return -bsMax;
    }

    boolean compare(byte bs, byte bp) {
	if (bs == bp) return true;
	if ((bs & 0b10000000) == 0b00000000) {
	    if (bs >= 65 && bs <= 90)
		return (bs+32 == bp);
	    else if (bs >= 97 && bs <= 122)
		return (bs-32 == bp);
	    else 
		return false;
	} else return false;
    }

    String getString() throws IOException {
	StringBuilder s = new StringBuilder();
	byte b;
	do {
	    b = nextByte();
	    if (b != 0) s.append((char) b);
	} while (b != 0);
	return s.toString();
    }
    
    String getString(byte[] buffer, int length) throws IOException {
	
	Charset utf8 = Charset.forName("UTF-8");
	CharsetDecoder decoder = utf8.newDecoder();
	ByteBuffer input = ByteBuffer.wrap(buffer,0,length);
	CharBuffer output = CharBuffer.allocate(length);
	while (input.hasRemaining()) {
	    CoderResult result = decoder.decode(input,output,false);
	    if (result.isError()) throw new IOException();
	}
	decoder.decode(input,output,true);
	decoder.flush(output);
	output.flip();
	return output.toString();
	/*
	StringBuilder s = new StringBuilder();
	for (int i=0; i<length; i++)
	    s.append((char) buffer[i]);
	return s.toString();
	*/
    }
    
    void skipString() throws IOException {
	for (int round = 0; round < 2; round++) {
	    byte[] buf = currentBuffer.buf;
	    long last = currentBuffer.last;
	    int index = (int) (currentPos-currentBuffer.first);

	    while (currentPos <= last) {
		byte bs = buf[index++];
		++currentPos;
		if (bs == 0) {
		    if (currentPos > last) moveToCurrentBuffer();
		    return;
		} 
	    }
	    moveToCurrentBuffer();
	}
	throw new IOException("skipString");
    }

    void skip(long n) throws IOException {
	long NewPos = Math.min(fileSize,currentPos+n);
	seek(NewPos);
    }

    public void seek(long pos) throws IOException {
	if (pos > fileSize)
	    throw new IOException("seeking beyond eof");
	if (pos < currentPos)
	    throw new IOException("seeking backwards no longer supported");

	currentPos = pos;
	moveToCurrentBuffer();
    }

    public long current() {
        return currentPos;
    }

    public boolean atEOF() {
        return currentPos>=fileSize;
    }

    public byte nextByte() throws IOException {
	int index = (int) (currentPos-currentBuffer.first);
        byte toReturn = currentBuffer.buf[index];
	// We should probably just do seek(currentPosition+1) here but
	// we try to optimise slightly
	++currentPos;
	if (currentPos > currentBuffer.last)
	    moveToCurrentBuffer();
	return toReturn;
    }

    void close() throws IOException {
	if (in != null)
	    in.close();
    }
}

class Buffer {
    byte[] buf;
    long first;
    long last;
    int bufSize;
    FileInputStream in;
    long fileSize;
    static int fileBytesRead;

    Buffer(FileInputStream in, int bufSize, long fileSize) {
        this.in = in;
        this.bufSize = bufSize;
        buf = new byte[bufSize];
        first = 0;
        last = 0;
	fileBytesRead = 0;
	this.fileSize = fileSize;
    }

    void read(long first) throws IOException {
	this.first = first;
	int expectToRead = 
	    (int) Math.min((long) bufSize,
			   Math.max(fileSize-first,0));
	int toRead = expectToRead;
	this.last = first+toRead-1;
	//System.out.println
	//  ("first = "+first+
	//   " expectToRead = "+expectToRead);

	int readPos = 0;
	while (expectToRead > 0) {
	    int bytesRead = in.read(buf,readPos,expectToRead);
	    if (bytesRead == -1) {
		System.out.println("*** Error: reader.read:");
		System.out.println("have read "+fileBytesRead+" bytes so far");
		System.out.println("filesize = "+fileSize+" first = "+first);
		throw
		    new IOException
		    ("eof after having read "+readPos+" bytes. Expected to read "+
		     toRead+" bytes");
	    }
	    expectToRead -= bytesRead;
	    readPos += readPos;
	}
	fileBytesRead += toRead;
    }
}
