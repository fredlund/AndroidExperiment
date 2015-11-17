package fred.docapp;

import android.content.Context;
import com.jcraft.jsch.*;
import java.io.*;

public class ScpFromJava {
    public boolean transfer(Context context, String username, String password, String host, String reqFile) {

        JSch jsch = new JSch();
        JschLogger logger = new JschLogger();
        logger.setLevel(Logger.INFO);
        jsch.setLogger(logger);
        logger.setLevel(Logger.INFO);
        boolean is_ok = false;

        FileOutputStream fos = null;
        try {

            Session session = jsch.getSession(username, host, 22);
            MyUserInfo userInfo = new MyUserInfo(password);
            session.setUserInfo(userInfo);
            session.connect();

            // exec 'scp -f rfile' remotely
            String command = "scp -f " + reqFile;
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

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
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

                //System.out.println("filesize="+filesize+", file="+file);

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of lfile
                String localFileName = new File(file).getName();
                fos = context.openFileOutput(localFileName, 0);

                int foo;
                while (true) {
                    if (buf.length < filesize) foo = buf.length;
                    else foo = (int) filesize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L) break;
                }
                fos.close();
                fos = null;
                is_ok = true;

                if (checkAck(in) != 0) {
                    System.exit(0);
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
            }

            session.disconnect();

            System.exit(0);
        } catch (Exception e) {
            System.out.println("ScpFromJava: exception "+e);
            e.printStackTrace();
            try {
                if (fos != null) fos.close();
            } catch (Exception ee) {
            }
        }
        return is_ok;
    }

    public static void main(String[] arg) {
        if (arg.length != 2) {
            System.err.println("usage: java ScpFrom user@remotehost:file1 file2");
            System.exit(-1);
        }

        FileOutputStream fos = null;

        String user = arg[0].substring(0, arg[0].indexOf('@'));
        arg[0] = arg[0].substring(arg[0].indexOf('@') + 1);
        String host = arg[0].substring(0, arg[0].indexOf(':'));
        String rfile = arg[0].substring(arg[0].indexOf(':') + 1);
        String lfile = arg[1];

        String prefix = null;
        if (new File(lfile).isDirectory()) {
            prefix = lfile + File.separator;
        }


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

    class MyUserInfo implements com.jcraft.jsch.UserInfo {
        private String password;

        MyUserInfo(String password) {
            this.password = password;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptYesNo(String str) {
             return true;
         }

        @Override
        public void showMessage(String message) {
        }
    }

