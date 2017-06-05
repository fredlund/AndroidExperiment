package fred.docapp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.*;
import java.io.*;

public class ScpFromJava {
        JSch jsch;
        JschLogger logger;
        ScpReturnStatus retStatus;
    File tmpFile;
    Session session;
    MyUserInfo userInfo;
    byte[] buf;
    OutputStream out;
    BufferedInputStream in;
    String file;
    String reqFile;
    long fileSize;
    Context cntxt;
    long lastStatusReport = 0;
    FileOutputStream fos = null;

    public ScpFromJava(Context cntxt) {
        this.cntxt = cntxt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public ScpReturnStatus setupTransfer(String username, String password, String host, int port, String reqFile) {
        jsch = new JSch();
        logger = new JschLogger();
        logger.setLevel(Logger.INFO);
        jsch.setLogger(logger);
        logger.setLevel(Logger.INFO);
        retStatus = new ScpReturnStatus(true);
        this.reqFile = reqFile;

        try {

            session = jsch.getSession(username, host, port);
            userInfo = new MyUserInfo(password);
            session.setUserInfo(userInfo);
            session.connect();

            // exec 'scp -f rfile' remotely
            String command = "scp -f " + (" \"" + reqFile + "\"");

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            out = channel.getOutputStream();
            in = new BufferedInputStream(channel.getInputStream());

            channel.connect();

            buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            if (retStatus.is_ok) {
                int c = checkAck(in);
                if (c != 'C') {
                    retStatus.is_ok = false;
                    return retStatus;
                }

                // read '0644 '
                //in.read(buf, 0, 5);

                fileSize = 0L;
                int pos = 0;
                while (true) {
                    if (in.read(buf, pos, 1) < 0) {
                        retStatus.is_ok = false;
                        in.close();
                        out.close();
                        return retStatus;
                    }

                    if (buf[pos] == (byte) 0xa)
                        break;
                    ++pos;
                }

                System.out.println("read line " + MySlowReader.printBs(buf, pos));

                int index = 0;
                while (index < pos && buf[index] != 0x20) ++index;
                ++index;
                System.out.println("after skipping mode: index=" + index);

                while (index < pos && buf[index] != 0x20) {
                    fileSize = fileSize * 10L + (long) (buf[index] - '0');
                    ++index;
                }
                ++index;
                System.out.println("fileSize = " + fileSize + " index=" + index);

                file = "";
                while (index < pos && buf[index] != 0xa) {
                    file += ((char) buf[index]);
                    ++index;
                }

                System.out.println("filesize=" + fileSize + ", file=" + file);
                return retStatus;
            }
        } catch (Exception e) {
            System.out.println("ScpFromJava: exception " + e);
            retStatus.is_ok = false;
            retStatus.exc = e;
            e.printStackTrace();
            try {
                if (fos != null) fos.close();
            } catch (Exception ignored) {
            }
        }
        return retStatus;
    }


    public ScpReturnStatus doTransfer(StringBuffer sbuf) {
        return getFile(sbuf);
    }

        public ScpReturnStatus doTransfer(String localDir) {
            ScpReturnStatus retStatus = new ScpReturnStatus(true);
            try {
                System.out.println("file="+file+" localDir="+localDir);
                tmpFile = File.createTempFile(file, null, new File(localDir));
                System.out.println("tmpFile is " + tmpFile + " local file name is localDir=" + localDir + " file=" + file);
                File myFile = new File(localDir + "/" + file);
                myFile.setReadable(true, false);
                System.out.println("will open " + myFile);
                fos = new FileOutputStream(tmpFile);
            retStatus = getFile(fos);
                fos.close();
                fos = null;
                if (retStatus.is_ok && !moveFile(tmpFile, myFile)) {
                    System.out.println("could not move " + tmpFile + " to " + myFile);
                    retStatus.is_ok = false;
                }
            } catch (Exception e) {
                System.out.println("ScpFromJava: exception "+e);
                retStatus.is_ok = false;
                retStatus.exc = e;
                e.printStackTrace();
                try {
                    if (fos != null) fos.close();
                } catch (Exception ignored) { }
            }
            return retStatus;
        }

        ScpReturnStatus getFile(Object obj) {
            boolean toStringBuffer = obj instanceof StringBuffer;
            StringBuffer sbuf = null;
            FileOutputStream fos = null;
            if (!toStringBuffer) fos = (FileOutputStream) obj;
            else sbuf = (StringBuffer) obj;

            try {
                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                lastStatusReport = 0;


                long transferred = 0;
                long remaining = fileSize;
                int foo;
                while (true) {
                    if (buf.length < remaining) foo = buf.length;
                    else foo = (int) remaining;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        System.out.println("scp: read error");
                        retStatus.is_ok = false;
                        break;
                    }
                    if (!toStringBuffer)
                      fos.write(buf, 0, foo);
                    else
                       sbuf.append(new String(buf,0,foo, StandardCharsets.UTF_8));

                    transferred += foo;
                    remaining -= foo;
                    if (remaining == 0L) break;
                    if (transferred - lastStatusReport > 1024 * 512) {
                        System.out.println("reporting "+transferred+" bytes transferred");
                        Intent statusIntent = new Intent("file_transfer");
                        statusIntent.putExtra("file", reqFile);
                        statusIntent.putExtra("status",Transfer.progressing());
                        statusIntent.putExtra("transferred",transferred);
                        statusIntent.putExtra("fileSize",fileSize);
                        LocalBroadcastManager.getInstance(cntxt).sendBroadcast(statusIntent);
                        lastStatusReport = transferred;
                    }
                }

                if (remaining > 0)
                    System.out.println("file " + reqFile + ": could only read " + (fileSize - remaining) + "bytes");

                retStatus.is_ok = true;

                if (checkAck(in) != 0) {
                    System.out.println("scp: checkAck(in) failed");
                    retStatus.is_ok = false;
                }

                if (retStatus.is_ok) {
                    // send '\0'
                    buf[0] = 0;
                    out.write(buf, 0, 1);
                    out.flush();
                }

            session.disconnect();
        } catch (Exception e) {
            System.out.println("ScpFromJava: exception "+e);
                retStatus.is_ok = false;
            retStatus.exc = e;
            e.printStackTrace();
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

    class MyUserInfo implements com.jcraft.jsch.UserInfo {
        private String password;

        MyUserInfo(String password) {
            this.password = password;
        }

        @Override
        public String getPassphrase() {
            return "";
        }

        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassword(String message) {
            return true;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptYesNo(String str) {
             return true;
         }

        @Override
        public void showMessage(String message) {
        }
    }

