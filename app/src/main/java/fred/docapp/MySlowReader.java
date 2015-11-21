package fred.docapp;

import com.jcraft.jsch.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by fred on 20/11/15.
 */
public class MySlowReader {
    final int defaultBufSize = 8192;
    FileInputStream in = null;
    int bufSize = 0;
    int fileSize = 0;
    Buffer buffers[];
    Buffer currentBuffer;
    int stringSize = 0;
    int currentBuf;
    int currentPos = 0;

    public MySlowReader(String fileName, int fileSize) throws IOException {
        new MySlowReader(fileName,fileSize,8192);
    }

    public MySlowReader(String fileName, int fileSize, int bufSize) throws IOException {
        if (fileSize <= 0) throw new IOException("empty file");
        File myFile = new File(fileName);
        this.in = new FileInputStream(myFile);
        this.bufSize = bufSize;
        this.fileSize = fileSize;
        for (int bIndex=0; bIndex<buffers.length; bIndex++)
            buffers[bIndex] = new Buffer(in,bufSize);
        currentBuf = 0;
        buffers[currentBuf].read(Math.min(fileSize,bufSize),0);
    }

    long getLong() throws IOException {
        long value = 0;
        for (int i = 0; i < 4; i++)
                value = (value << 8) + (nextByte() & 0xff);
        return value;
    }

    void skip(int n) throws IOException {
        seek(current()+n);
    }

    // seeking forwards work (by reading sequentially), seeking backwards only within buffers
    public void seek(long pos) throws IOException {
        Buffer currentBuffer = buffers[currentBuf];
        long first = currentBuffer.first;
        long last = currentBuffer.last;
        if (pos >= first && pos <= last) {
            currentPos = (int) (pos-first);
            return;
        }
        if (pos > fileSize) return;

        int otherBuf = (currentBuf+1)%buffers.length;
        Buffer otherBuffer = buffers[otherBuf];
        long otherFirst = otherBuffer.first;
        long otherLast = otherBuffer.last;
        if (pos >= otherFirst && pos <= otherLast) {
            currentPos = (int) (pos - otherFirst);
            currentBuf = otherBuf;
            return;
        } else if (pos >= last) {
            do {
                if (otherFirst < last) {
                    int toRead;
                    if (fileSize < (last + 1 + bufSize))
                        toRead = (int) (fileSize - last);
                    else
                        toRead = bufSize;
                    otherBuffer.read(toRead, last + 1);
                }
                currentBuf = otherBuf;
                currentBuffer = buffers[currentBuf];
                last = currentBuffer.last;
                otherBuf = (currentBuf + 1) % buffers.length;
                otherFirst = otherBuffer.first;
            } while (pos >= last);
            currentPos = (int) (pos-currentBuffer.first);
            return;
        } else throw new IOException ("seeking backwards");
    }

    public long current() {
        return buffers[currentBuf].first+currentPos;
    }

    public byte nextByte() throws IOException {
        Buffer currentBuffer = buffers[currentBuf];
        long last = currentBuffer.last;
        byte toReturn = currentBuffer.buf[currentPos];
        if (last > currentPos) { // normal case, next byte in same buffer
            currentPos++;
            return toReturn;
        } else if (last == fileSize) { // this byte was the last in the file
            return toReturn;
        } else {  // last byte in buffer, but file continues
            int otherBuf = (currentBuf+1)%buffers.length;
            Buffer otherBuffer = buffers[otherBuf];
            if (otherBuffer.first != last+1) {    // next buffer does not contain next byte
                int toRead;
                if (fileSize < (last + 1 + bufSize))
                    toRead = (int) (fileSize - last);
                else
                    toRead = bufSize;
                otherBuffer.read(toRead, last + 1);
            }
            currentPos = 0;
            currentBuf = otherBuf;
            return toReturn;
        }
    }
}

class Buffer {
    byte[] buf;
    long first;
    long last;
    int bufSize;
    FileInputStream in;

    Buffer(FileInputStream in, int bufSize) {
        this.in = in;
        this.bufSize = bufSize;
        buf = new byte[bufSize];
        first = 0;
        last = 0;
    }

    void read(int expectToRead, long first) throws IOException {
        int bytesRead = in.read(buf,0,bufSize);
        if (bytesRead < expectToRead) throw new IOException();
        this.first = first;
        this.last = first+expectToRead;
    }
}