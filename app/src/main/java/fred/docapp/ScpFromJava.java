package fred.docapp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.LocalDestFile;
import net.schmizz.sshj.xfer.TransferListener;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import java.io.*;

public class ScpFromJava {
        ScpReturnStatus retStatus;
        FileOutputStream fos = null;
    File tmpFile;
    byte[] buf;
    OutputStream out;
    BufferedInputStream in;
    String file;
    String reqFile;
    long fileSize;
    Context cntxt;
    long lastStatusReport = 0;

    SSHClient ssh;
    SCPFileTransfer tr;

    public ScpFromJava(Context cntxt) {
        this.cntxt = cntxt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public ScpReturnStatus setupTransfer(String username, String password, String host, int port, String reqFile) {
        SSHClient ssh = null;
        retStatus = new ScpReturnStatus(true);
        this.reqFile = reqFile;

        try {
            ssh = new SSHClient(new MyAndroidConfig());
            //ssh.loadKnownHosts();
            ssh.connect(host, port);
            ssh.authPassword(username, password);
            tr = ssh.newSCPFileTransfer();
            tr.setTransferListener(new SCPDownloadListener(reqFile,cntxt));
        } catch (IOException exc) {
            System.out.println("ssh connection failure: "+exc);
            exc.printStackTrace();
            retStatus.is_ok=false;
            retStatus.add_exception(exc);
        };

        return retStatus;
    }

        public ScpReturnStatus doTransfer(String localDir) {
            try {
                tmpFile = File.createTempFile(file, null, new File(localDir));
                System.out.println("tmpFile is " + tmpFile + " local file name is localDir=" + localDir + " file=" + file);
                File myFile = new File(localDir + "/" + file);
                myFile.setReadable(true, false);
                System.out.println("will open " + myFile);
                tr.newSCPDownloadClient().copy(reqFile, new FileSystemFile(tmpFile));
                if (!moveFile(tmpFile, myFile)) {
                    System.out.println("could not move " + tmpFile + " to " + myFile);
                    retStatus.is_ok = false;
                } else retStatus.is_ok = true;
            } catch (IOException exc) {
                 System.out.println("ssh transfer failure: "+exc);
            exc.printStackTrace();
            retStatus.is_ok=false;
            retStatus.add_exception(exc);
            }
        return retStatus;
    }

    static boolean moveFile(File from, File to) {
        if (!from.renameTo(to)) {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(from);
                out = new FileOutputStream(to);
                long remains = from.length();
                byte buf[] = new byte[8192];

                while (remains > 0) {
                    int read = in.read(buf, 0, (int) Math.min(buf.length,remains));
                    if (read < 0) {
                        System.out.println("premature eof on "+from);
                        return false;
                    }
                    out.write(buf,0,read);
                    remains -= read;
                }
                in.close();
                in = null;
                boolean result = from.delete();
                if (!result)
                    System.out.println("Could not delete "+from);
                return result;
            } catch (IOException exc) {
                System.out.println("IOException "+exc);
                return false;
            } finally {
                if (in != null) try {
                    in.close();
                } catch (IOException ignored) {
                }
                if (out != null) try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
        return true;
    }

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}

class SCPDownloadListener implements TransferListener, StreamCopier.Listener {
        String name;
        long size;
        long transferred;
        long lastStatusReport;
        Context cntxt;
    String reqFile;

        public SCPDownloadListener(String reqFile, Context cntxt) {
            this.cntxt = cntxt;
            this.reqFile = reqFile;
        }

        @Override
        public TransferListener directory(String name) {
            return null;
        }

        @Override
        public StreamCopier.Listener file(String name, long size) {
            this.name = name;
            this.size = size;
            this.transferred = 0;
            this.lastStatusReport = 0;
            return this;
        }

        public long getSize() {
            return size;
        }

    @Override
    public void reportProgress(long transferred) throws IOException {
        this.transferred += transferred;
        if (transferred - lastStatusReport > 1024 * 512) {
            System.out.println("reporting "+transferred+" bytes transferred");
            Intent statusIntent = new Intent("file_transfer");
            statusIntent.putExtra("file", reqFile);
            statusIntent.putExtra("status",Transfer.progressing());
            statusIntent.putExtra("transferred",transferred);
            statusIntent.putExtra("fileSize",size);
            LocalBroadcastManager.getInstance(cntxt).sendBroadcast(statusIntent);
            lastStatusReport = this.transferred;
        }
    }
}

class ScpReturnStatus {
    boolean is_ok;
    Object exc;

    @Override
    public String toString() {
        return is_ok+" obj="+exc;
    }

    ScpReturnStatus(boolean is_ok) {
        this.is_ok = is_ok;
        this.exc = null;
    }

    void add_exception(Object exc) {
        this.exc = exc;
    }
}



