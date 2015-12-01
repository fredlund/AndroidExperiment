package fred.docapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


    public class TransferStatusActivity extends AppCompatActivity {

        private List<Map<String, Object>> listValues;
        private ListView listView = null;
        List<Entry> found = null;
        private ListView listView1 = null;
        private MenuItem spinnerItem = null;
        private Menu menu;
        ArrayList<DirView> stack = null;
        TransferStatusActivity myself = null;
        UserInfo ui;
        String currentLibrary = null;
        Map<String, Entry> toDownload;
        List<Transfer> transfers = new ArrayList<Transfer>();


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            System.out.println("transfer status activitity was created");
            System.out.flush();
            myself = TransferStatusActivity.this;
            super.onCreate(savedInstanceState);

            // Instantiates a new DownloadStateReceiver
            ResponseReceiver mDownloadStateReceiver =
                    new ResponseReceiver();
            IntentFilter request_transfer_filter = new IntentFilter(
                    "request_transfer");
            IntentFilter file_transfer_filter = new IntentFilter(
                    "file_transfer");
            // Registers the DownloadStateReceiver and its intent filters
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mDownloadStateReceiver,
                    request_transfer_filter);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mDownloadStateReceiver,
                    file_transfer_filter);

            setContentView(R.layout.mylist);
            listView1 = (ListView) findViewById(android.R.id.list);
            transfers = TransferDB.getInstance(TransferStatusActivity.this).getAll();
            TransferAdapter adapter = new TransferAdapter(myself, R.layout.listview_item_row_transfer, transfers);
            System.out.println("computed new adapter");
            System.out.flush();
            listView1.setAdapter(adapter);

        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            this.menu = menu;
            System.out.println("onCreateOptionsMenu");
            System.out.flush();
            // Inflate the options menu from XML
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.transfer_status_menu, menu);
            myself = this;
            return true;
        }

        void doFileRequest(String library, String host, String username, String password) {
            SharedPreferences appPrefs = getSharedPreferences("appData", 0);
            int requestNo = appPrefs.getInt("transferCounter", 0);
            SharedPreferences.Editor edit = appPrefs.edit();
            edit.putInt("transferCounter", requestNo % 32000);
            String files[] = new String[toDownload.size()];
            int i = 0;
            for (Entry entry : toDownload.values()) {
                System.out.println("Entry=" + entry);
                files[i++] = entry.dirName + "/" + entry.fileName;
            }
            File localFile = Environment.getExternalStorageDirectory();
            File myDir = new File(localFile.getAbsolutePath() + "/Billy/");
            myDir.mkdir();
            FileTransferRequest ftr = new FileTransferRequest(library, host, username, password, files, myDir.getAbsolutePath(), requestNo);
            System.out.println("making intent");
            Intent intent = new Intent(TransferStatusActivity.this, FileService.class);
            intent.putExtra("fred.docapp.FileTransferRequest", ftr);
            System.out.println("intent prepared");
            startService(intent);
        }



        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection
            switch (item.getItemId()) {
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        //@Override
        protected void onNewIntent(Intent intent) {
        }

        private void handleIntent(Intent intent, List<Map<String, Object>> listValues) {
        }


    }

    // Broadcast receiver for receiving status updates from the IntentService
    class ResponseReceiver extends BroadcastReceiver {
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {
            System.out.println("got an intent " + intent);
        }

    }


class Transfer {
    String file;
    int transferStatus;
    long transferred;
    String library;

    Transfer(String file, String library, int transferStatus, long transferred) {
            this.file = file;
        this.library = library;
            this.transferStatus = transferStatus;
            this.transferred = transferred;
    }


    public String transferStatusToString() {
        if (transferStatus == 0) return "waiting";
        else if (transferStatus == 1) return "progressing";
        else if (transferStatus == 2) return "failed";
        else if (transferStatus == 3) return "finished";
        else return "bad";
    }

    public String toString() {
        return "{" + file + "," + library + "," + transferStatusToString() + "," + transferred + "}";
    }

    static int waiting() {
        return 0;
    }
    static int progressing() {
        return 1;
    }
    static int failed() {
        return 2;
    }
    static int finished() {
        return 3;
    }
}
