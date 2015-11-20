package fred.docapp;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by fred on 20/11/15.
 */
public class SpeedTest {
    public void test1(Context context) {
        BufferedInputStream fbuf = null;
        try {
            fbuf = new BufferedInputStream(context.openFileInput(MLocate.localLibraryFile("tabitha")), 8192);

            File file = new File(context.getFilesDir()+"/"+MLocate.localLibraryFile("tabitha"));
            long len = file.length();
            System.out.println("length of "+file+" is "+len);
            if (len <= 0) {
                System.out.println("length is wrong: "+len);
                return;
            }

            int size = (int) len;
            System.out.println("allocating buffer of size "+size);
            byte buffer[] = new byte[size];
            System.out.println("buffer allocated");

            long startTime = System.currentTimeMillis();
            int read = 0;
            int toRead = size;
            while (toRead > 0) {
                int readNow = Math.min(8192,toRead);
                int numRead = fbuf.read(buffer,read,readNow);
                if (numRead < readNow) {
                    System.out.println("read too few bytes: "+numRead+" < "+readNow);
                    return;
                }
                toRead -= numRead;
                read += numRead;
            }
            long endTime = System.currentTimeMillis();
            System.out.println(read+" bytes read");
            System.out.println("read speed: "+((size / (1024.0*1024.0)) / ((System.currentTimeMillis() - startTime) / 1000.0)) + "MB/sec");
        } catch (IOException e) {
            System.out.println("exception "+e); e.printStackTrace(); return;
        } finally {
            if (fbuf != null) {
                try {
                    fbuf.close();
                } catch (Exception exc) {
                }
                ;
            }
        }
    }
}
