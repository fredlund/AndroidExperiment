package fred.docapp;

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
    Buffer currentBuf;
    Buffer otherBuf;

    public MySlowReader(String fileName) throws IOException {
        new MySlowReader(fileName,8192);
    }

    public MySlowReader(String fileName, int bufSize) throws IOException {
        File myFile = new File(fileName);
        this.in = new FileInputStream(myFile);
        this.bufSize = bufSize;
        currentBuf = new Buffer(in, bufSize);
        otherBuf = new Buffer(in, bufSize);
    }



    int skip(int n) throws IOException {
        if (n>bufSize) throw new IOException("bufSize");

        int skipped = 0;
        int advanced = currentBuf.advance(n);
        skipped += advanced;
        if (advanced < n && currentBuf.was_complete()) {
            int remainingToAdvance = n-advanced;
            Buffer swap = currentBuf;
            currentBuf = otherBuf;
            otherBuf = swap;
            if (currentBuf.remaining() == 0) {
                currentBuf.read();
            }
            if (currentBuf.remaining() > 0) {
                advanced = currentBuf.advance(remainingToAdvance);
                skipped += advanced;
            }
        }
        return skipped;
    }
}

class StringReader {
    Buffer first;
    Buffer last;

    StringReader() {
        
    }
}

class Buffer {
    byte[] buf;
    int remaining;
    int pointer;
    boolean was_complete;
    int bufSize;
    FileInputStream in;

    Buffer(FileInputStream in, int bufSize) {
        this.in = in;
        this.bufSize = bufSize;
        buf = new byte[bufSize];
        remaining = 0;
        pointer = 0;
        was_complete = true;
    }

    boolean was_complete() {
        return was_complete;
    }

    int remaining() {
       return remaining;
    }

    int advance(int n) {
        int advancing = Math.min(remaining, n);
        pointer += advancing;
        remaining -= advancing;
        return advancing;
    }

    void read() throws IOException {
        int bytesRead = in.read(buf,0,bufSize);
        was_complete = (bytesRead < bufSize);
        pointer = 0;
        remaining = bytesRead;
    }
}