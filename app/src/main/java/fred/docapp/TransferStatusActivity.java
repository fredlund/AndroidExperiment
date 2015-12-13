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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;


    public class TransferStatusActivity extends AppCompatActivity {

        private List<Map<String, Object>> listValues;
        private ListView listView1 = null;
        private Menu menu;
        Map<String,Transfer> selected = null;
        TransferStatusActivity myself = null;
        UserInfo ui;
        String currentLibrary = null;
        List<Transfer> transfers;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            System.out.println("transfer status activitity was created");
            System.out.flush();
            myself = TransferStatusActivity.this;
            transfers = TransferDB.getInstance(TransferStatusActivity.this).getAll();
            selected = new HashMap<String,Transfer>();
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

            listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println("onItemClick position=" + position + " found size=" +
                            ((transfers == null) ? -1 : transfers.size()));
                    System.out.flush();
                    Transfer transfer = transfers.get(position);
                    System.out.println("item " + position + " was clicked=" + transfers.get(position));
                }

            });

            listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Transfer transfer = transfers.get(position);
                    System.out.println("item " + position + " was longclicked=" + transfer);
                    if (selected.containsKey(transfer.file))
                        selected.remove(transfer.file);
                    else
                        selected.put(transfer.file,transfer);
                    return true;
                }

            });
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


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection
            switch (item.getItemId()) {
                case R.id.menu_resume_selected_jobs:
                {
                    Collection<Transfer> values = selected.values();
                    for (Transfer transfer : values) {
                        GetFile.doFileRequest(transfer.library, transfer.file, TransferStatusActivity.this);
                        selected.remove(transfer.file);
                    }
                }
                    break;
                case R.id.menu_resume_failed_jobs:
                {
                    for (Transfer transfer : transfers)
                    if (Transfer.isFailure(transfer.transferStatus))
                        GetFile.doFileRequest(transfer.library, transfer.file, TransferStatusActivity.this);
                }
                    break;
                case R.id.menu_clear_all_jobs: {
                    TransferDB.getInstance(TransferStatusActivity.this).clearDB();
                    transfers = new ArrayList<Transfer>();
                    TransferAdapter adapter = new TransferAdapter(myself, R.layout.listview_item_row_transfer, transfers);
                    listView1.setAdapter(adapter);
                }
                    break;
                case R.id.menu_clear_finished_jobs:
                {
                    TransferDB db = TransferDB.getInstance(TransferStatusActivity.this);
                    ListIterator<Transfer> it = transfers.listIterator();
                    while (it.hasNext()) {
                        Transfer transfer = it.next();
                        if (transfer.transferStatus == Transfer.finished()) {
                            db.deleteTransfer(transfer);
                            selected.remove(transfer.file);
                            it.remove();
                        }
                    }
                    TransferAdapter adapter = new TransferAdapter(myself, R.layout.listview_item_row_transfer, transfers);
                    listView1.setAdapter(adapter);
                }
                    break;
                default:
                    return super.onOptionsItemSelected(item);
            }
            return true;
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
    long fileSize;
    long transferred;
    String library;

    Transfer(String file, String library, int transferStatus, long fileSize, long transferred) {
            this.file = file;
        this.library = library;
            this.transferStatus = transferStatus;
        this.fileSize = fileSize;
            this.transferred = transferred;
    }


    public String transferStatusToString() {
        if (transferStatus == 0) return "waiting";
        else if (transferStatus == 1) return "progressing";
        else if (transferStatus == 2) return "failed";
        else if (transferStatus == 3) return "finished";
        else if (transferStatus == 4) return "failed-connect";
        else return "bad";
    }

    public String toString() {
        return "{" + file + "," + library + "," + transferStatusToString() + "," + fileSize + "," + transferred + "}";
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
    static int failedConnect() { return 4; }
    static boolean isFailure(int status) { return status==failed() || status == failedConnect(); }
}
