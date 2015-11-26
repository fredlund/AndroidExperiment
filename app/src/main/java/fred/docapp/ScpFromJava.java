package fred.docapp;

import android.content.Context;
import android.os.Environment;

import com.jcraft.jsch.*;
import java.io.*;

public class ScpFromJava {
    public ScpReturnStatus transfer(Context context, String username, String password, String host, String reqFile, String localDir) {

        JSch jsch = new JSch();
        JschLogger logger = new JschLogger();
        logger.setLevel(Logger.INFO);
        jsch.setLogger(logger);
        logger.setLevel(Logger.INFO);
        ScpReturnStatus retStatus = new ScpReturnStatus(true);

        FileOutputStream fos = null;
        try {

            Session session = jsch.getSession(username, host, 22);
            MyUserInfo userInfo = new MyUserInfo(password);
            session.setUserInfo(userInfo);
            session.connect();

            // exec 'scp -f rfile' remotely
            String command = "scp -f " + (" \"" + reqFile + "\"");

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (retStatus.is_ok) {
                int c = checkAck(in);
                if (c != 'C') {
                    retStatus.is_ok = false;
                    break;
                }

                // read '0644 '
                in.read(buf, 0, 5);

                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') break;
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                String file = null;
                for (int i = 0; ; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

                System.out.println("filesize="+filesize+", file="+file);

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();


                File myFile = new File(localDir+"/"+file);
                myFile.setReadable(true,false);
                System.out.println("will open "+myFile);
                fos = new FileOutputStream(myFile);

                int foo;
                while (true) {
                    if (buf.length < filesize) foo = buf.length;
                    else foo = (int) filesize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        System.out.println("scp: read error");
                        retStatus.is_ok = false;
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L) break;
                }
                fos.close();
                fos = null;
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
            }
            session.disconnect();
        } catch (Exception e) {
            System.out.println("ScpFromJava: exception "+e);
            retStatus.exc = e;
            e.printStackTrace();
            try {
                if (fos != null) fos.close();
            } catch (Exception ee) {
            }
        }
        return retStatus;
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
            StringBuffer sb = new StringBuffer();
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

