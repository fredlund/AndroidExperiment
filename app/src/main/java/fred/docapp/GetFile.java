package fred.docapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

/**
 * Created by fred on 2/12/15.
 *
 */
public class GetFile {
    static void doFileRequest(String library, String file, boolean tryOpen, Context cntxt) {
        doFileRequest(library, new String[] {file}, tryOpen, cntxt);
    }

    static void doFileRequest(String library, String[] files, boolean tryOpen, Context cntxt) {
        String host;
        String username;

        final SharedPreferences libraryPrefs = cntxt.getSharedPreferences(library, 0);
        if ((host = find_host(library, libraryPrefs)) != null) {
            if ((username = find_username(library, libraryPrefs)) != null) {
                String password = find_password(library, libraryPrefs);
                    String port = find_port(library, libraryPrefs);
                File localFile = Environment.getExternalStorageDirectory();
                File myDir = new File(localFile.getAbsolutePath() + "/Billy/");
                myDir.mkdir();
                    doFileRequest(cntxt, library, host, port, myDir.getAbsolutePath(), username, password, files, tryOpen);

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(cntxt);
                builder.setTitle("Error");
                builder.setMessage("Don't know which username to use");
                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.show();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(cntxt);
            builder.setTitle("Error");
            builder.setMessage("Don't know which host to contact");
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.show();
        }

    }


    static void doFileRequest(final Context cntxt, final String library, final String hostname, final String port, final String localLocation, final String username, String password, final String[] files, final boolean tryOpen) {
       final int portNo;

        if (port.equals("")) portNo = 22;
        else portNo = Integer.parseInt(port);

        if (password == null || password.equals("")) {
            UserHost uh = new UserHost(username, hostname);
            (UserInfo.getInstance()).getPassword(cntxt, uh, new DialogListener() {
                @Override
                public void result(Object item) {
                    if (item != null)
                        doFileRequestWithPassword(cntxt, library, hostname, portNo,  username, localLocation, (String) item, files, tryOpen);
                }
            });
        }
        else doFileRequestWithPassword(cntxt, library, hostname, portNo, username, localLocation, password, files, tryOpen);
    }

    static void doFileRequestWithPassword(Context cntxt, String library, String hostname, int portNo, String username, String localLocation, String password, String[] files, boolean tryOpen) {
            if (password != null && !password.equals("")) {
                SharedPreferences appPrefs = cntxt.getSharedPreferences("appData", 0);
                int requestNo = appPrefs.getInt("transferCounter", 0);
                SharedPreferences.Editor edit = appPrefs.edit();
                edit.putInt("transferCounter", requestNo % 32000);
                edit.apply();
                FileTransferRequest ftr = new FileTransferRequest(library, hostname, portNo, username, password, files, tryOpen, localLocation, requestNo);
                System.out.println("making intent");
                Intent intent = new Intent(cntxt, FileService.class);
                intent.putExtra("fred.docapp.FileTransferRequest", ftr);
                System.out.println("intent prepared");
                cntxt.startService(intent);
            }

    }

    static String find_host(String library, SharedPreferences prefs) {
        String location = prefs.getString("library_host", "");
        if (location.equals(""))
            location = prefs.getString("db_host", "");
        return location;
    }

    static String find_port(String library, SharedPreferences prefs) {
        String location = prefs.getString("library_port", "");
        if (location.equals(""))
            location = prefs.getString("db_port", "22");
        return location;
    }

    static String find_username(String library, SharedPreferences prefs) {
        String username = prefs.getString("library_username", "");
        if (username.equals(""))
            username = prefs.getString("db_username", "");
        return username;
    }

    static String find_password(String library, SharedPreferences prefs) {
        String password = prefs.getString("library_password", "");
        if (password.equals(""))
            password = prefs.getString("db_password", "");
        return password;
    }

}
