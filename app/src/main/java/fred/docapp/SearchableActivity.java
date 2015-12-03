package fred.docapp;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class SearchableActivity extends AppCompatActivity {

    private List<Map<String, Object>> listValues;
    List<Entry> found = null;
    private ListView listView1 = null;
    private MenuItem spinnerItem = null;
    private Menu menu;
    ArrayList<DirView> stack = null;
    SearchableActivity myself = null;
    OurSpinnerAdapter spinnerAdapter = null;
    Spinner spinner = null;
    UserInfo ui;
    String currentLibrary = null;
    Map<String, Entry> toDownload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("search activitity was created");
        System.out.flush();
        super.onCreate(savedInstanceState);
        SharedPreferences appData = getSharedPreferences("appData", 0);
        currentLibrary = appData.getString("default_library", null);
        toDownload = new HashMap<>();

        System.out.println("external storage dir=" + Environment.getExternalStorageDirectory());
        System.out.println("external public storage dir (downloads)=" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        System.out.println("pictures=" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));


        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        listValues = new ArrayList<Map<String, Object>>();
        ui = new UserInfo();

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

        // Get the intent, verify the action and get the query
        System.out.println("before intent");
        System.out.flush();
        Intent intent = getIntent();
        System.out.println("after intent");
        System.out.flush();
        handleIntent(intent, listValues);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        this.menu = menu;
        System.out.println("onCreateOptionsMenu");
        System.out.flush();
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        myself = this;

        //(new SpeedTest()).test1(SearchableActivity.this);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        System.out.println("onCreateOptionsMenu: item is " + menu.findItem(R.id.menu_search));
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        System.out.println("searchView=" + searchView);
        System.out.flush();
        ComponentName cn = new ComponentName(this, SearchableActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        System.out.println("before spinning");
        System.out.flush();
        System.out.println("onCreateOptionsMenu: spinner item is " + menu.findItem(R.id.spinner));
        System.out.flush();
        if (menu.findItem(R.id.spinner) == null) {
            try {
                wait(200);
            } catch (InterruptedException exc) {
            }

            new Exception().printStackTrace();
        }
        spinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        System.out.println("Spinner is " + spinner + " spinner adapter is " + spinnerAdapter);
        System.out.flush();
        spinner.setAdapter(spinnerAdapter); // set the adapter to provide layout of rows and
        // content
        System.out.println("stack is " + stack);
        System.out.flush();
        if (stack != null)
            spinner.setSelection(stack.size() - 1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                  Log.v("setOnItemSelected", "stack=" + stack);
                                                  Log.v("setOnItemSelected", "ONITEMSELECTED!!! " +
                                                          "position=" + position + " size=" + stack.size());
                                                  System.out.flush();
                                                  if (position < stack.size() - 1 && position > 0) {
                                                      Log.i("setOnItemSelected", "correct position; will remove from stack");
                                                      for (int i = stack.size() - 1; i > position; i--) {
                                                          stack.remove(i);
                                                      }
                                                      Log.i("setOnItemSelected", "correct position; will get");
                                                      DirView dv = stack.get(position);
                                                      Log.i("setOnItemSelected", "correct position; will setAdapter for listview");
                                                      listView1.setAdapter(dv.adapter);
                                                      found = dv.entries;
                                                      Log.i("setOnItemSelected", "new spinneradapter");
                                                      spinnerAdapter = new OurSpinnerAdapter(SearchableActivity.this,
                                                              android.R.layout.simple_spinner_item,
                                                              //R.layout.spinner_item_row,
                                                              //R.id.spinnerText,
                                                              stack);
                                                      Log.i("setOnItemSelected", "setSelection");
                                                      spinner.setSelection(position);
                                                      Log.i("setOnItemSelected", "correct position; will setAdapter for spinner");
                                                      spinner.setAdapter(spinnerAdapter);
                                                  } else {
                                                      if (stack != null)
                                                          spinner.setSelection(stack.size() - 1);
                                                      Log.i("setOnItemSelected", "position wrong or 0");
                                                  }
                                                  Log.i("setOnItemSelected", "done");
                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> parent) {
                                                  System.out.println("ONNOTHINGSELECTED");
                                                  System.out.flush();
                                              }
                                          }

        );

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_transfer_status: {
                Intent transferStatusIntent = new Intent(this,TransferStatusActivity.class);
                startActivity(transferStatusIntent);
                break;
            }
            case R.id.menu_download: {
                System.out.println("in download");
                if (toDownload.size() > 0) {
                    final String library = find_current_library();
                    final String username;
                    final String host;
                    if (library != null) {
                        String files[] = new String[toDownload.size()];
                        int i = 0;
                        for (Entry entry : toDownload.values()) {
                            System.out.println("Entry=" + entry);
                            files[i++] = entry.dirName + "/" + entry.fileName;
                            entry.isEnabled = false;
                            EntryAdapter adapter = (EntryAdapter) listView1.getAdapter();
                            adapter.notifyDataSetChanged();
                        }
                        GetFile.doFileRequest(library,files,SearchableActivity.this);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);
                        builder.setTitle("Error");
                        builder.setMessage("Don't know in which library the items are located");
                        builder.setPositiveButton("OK", null);
                        AlertDialog dialog = builder.show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage("No files to download");
                    builder.setPositiveButton("OK", null);
                    AlertDialog dialog = builder.show();
                }
            }
            break;
            case R.id.menu_add_library: {
                System.out.println("creating new intent");
                Intent intent = new Intent(this, AddLibrary.class);
                startActivity(intent);
            }
            break;
            case R.id.menu_delete_library: {
                SharedPreferences data = getSharedPreferences("appData", 0);
                Set<String> libraries = data.getStringSet("libraries", new HashSet<String>());
                final AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);

                final String[] librariesString = libraries.toArray(new String[libraries.size()]);
                System.out.println("have builder");
                System.out.flush();
                builder.setTitle("Edit library spec");
                //builder.setMessage("Library to edit ");
                //final EditText input = new EditText(SearchableActivity.this);
                //input.setInputType(InputType.TYPE_CLASS_TEXT);
                for (String library : libraries)
                    System.out.println("library: " + library);
                System.out.println();

                //builder.setView(input);
                //System.out.println("setView"); System.out.flush();


                builder.setItems(librariesString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("which is " + which);
                        final String library = librariesString[which];
                        System.out.println("user_text is " + library);
                        final SharedPreferences libraryPreferences = getSharedPreferences(library, 0);
                        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(SearchableActivity.this);
                        confirmDelete.setTitle("Confirm");
                        confirmDelete.setMessage("Are you sure?");

                        confirmDelete.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences appData = getSharedPreferences("appData", 0);
                                Set<String> libraries = appData.getStringSet("libraries", new
                                        HashSet<String>());
                                SharedPreferences.Editor appDataEditor = appData.edit();
                                Set<String> copiedLibraries = new HashSet<String>(libraries);
                                copiedLibraries.remove(library);
                                appDataEditor.remove("libraries");
                                appDataEditor.putStringSet("libraries", copiedLibraries);
                                appDataEditor.commit();
                                SharedPreferences.Editor libraryEditor = libraryPreferences.edit();
                                libraryEditor.clear();
                                libraryEditor.commit();
                                dialog.dismiss();
                            }

                        });

                        confirmDelete.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = confirmDelete.create();
                        alert.show();

                    }
                });


                builder.setPositiveButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.out.println("cancelling");
                            }
                        });


                // create alert dialog
                //AlertDialog alertDialog = alertDialogBuilder.create();

                System.out.println("setbuttons done");
                System.out.flush();
                // show it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
            break;
            case R.id.menu_edit_library: {
                SharedPreferences edit_data = getSharedPreferences("appData", 0);
                Set<String> edit_libraries = edit_data.getStringSet("libraries", new HashSet<String>());
                final AlertDialog.Builder edit_builder = new AlertDialog.Builder(SearchableActivity.this);

                final String[] edit_librariesString = edit_libraries.toArray(new String[edit_libraries.size()]);
                System.out.println("have builder");
                System.out.flush();
                edit_builder.setTitle("Edit library spec");
                //builder.setMessage("Library to edit ");
                //final EditText input = new EditText(SearchableActivity.this);
                //input.setInputType(InputType.TYPE_CLASS_TEXT);
                for (String library : edit_libraries)
                    System.out.println("library: " + library);
                System.out.println();

                //builder.setView(input);
                //System.out.println("setView"); System.out.flush();


                edit_builder.setItems(edit_librariesString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("which is " + which);
                        String library = edit_librariesString[which];
                        System.out.println("user_text is " + library);
                        SharedPreferences libraryPreferences = getSharedPreferences(library, 0);
                        if (libraryPreferences.contains("is_created")) {
                            System.out.println("creating new intent for addlibrary");
                            System.out.println("db_location=" + libraryPreferences.getString("db_location", ""));
                            System.out.println("db_password=" + libraryPreferences.getString("db_password", ""));
                            Intent newIntent = new Intent(SearchableActivity.this, AddLibrary.class);
                            newIntent.putExtra("libraryName", library);
                            newIntent.putExtra("db_host", libraryPreferences.getString
                                    ("db_host", ""));
                            newIntent.putExtra("db_location", libraryPreferences.getString
                                    ("db_location", ""));
                            newIntent.putExtra("db_username", libraryPreferences.getString
                                    ("db_username", ""));
                            newIntent.putExtra("db_password", libraryPreferences.getString
                                    ("db_password", ""));
                            newIntent.putExtra("library_host", libraryPreferences
                                    .getString
                                            ("library_host", ""));
                            newIntent.putExtra("library_username", libraryPreferences
                                    .getString
                                            ("library_username", ""));
                            newIntent.putExtra("library_password", libraryPreferences
                                    .getString
                                            ("library_password", ""));
                            startActivity(newIntent);
                        }
                    }
                });


                edit_builder.setPositiveButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.out.println("cancelling");
                            }
                        });


                // create alert dialog
                //AlertDialog alertDialog = alertDialogBuilder.create();

                System.out.println("setbuttons done");
                System.out.flush();
                // show it
                AlertDialog edit_alertDialog = edit_builder.create();
                edit_alertDialog.show();
            }

            break;
            case R.id.menu_download_library_index: {
                SharedPreferences edit_data = getSharedPreferences("appData", 0);
                Set<String> edit_libraries = edit_data.getStringSet("libraries", new HashSet<String>());
                final AlertDialog.Builder edit_builder = new AlertDialog.Builder(SearchableActivity.this);

                final String[] edit_librariesString = edit_libraries.toArray(new String[edit_libraries.size()]);
                System.out.println("have builder");
                System.out.flush();
                edit_builder.setTitle("Edit library spec");
                //builder.setMessage("Library to edit ");
                //final EditText input = new EditText(SearchableActivity.this);
                //input.setInputType(InputType.TYPE_CLASS_TEXT);
                for (String library : edit_libraries)
                    System.out.println("library: " + library);
                System.out.println();

                //builder.setView(input);
                //System.out.println("setView"); System.out.flush();


                edit_builder.setItems(edit_librariesString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("which is " + which);
                        final String library = edit_librariesString[which];
                        System.out.println("user_text is " + library);
                        SharedPreferences libraryPreferences = getSharedPreferences(library, 0);
                        if (libraryPreferences.contains("is_created")) {
                            String localLibraryPath = MLocate.localLibraryFile(library);
                            StringBuffer errors = new StringBuffer();
                            final String db_host = libraryPreferences.getString("db_host", "");
                            final String db_location = libraryPreferences.getString("db_location", "");
                            final String db_user = libraryPreferences.getString("db_username", "");
                            System.out.println("db_host=" + db_host + " db_location=" + db_location + " db_user=" + db_user);
                            boolean has_error;
                            if (has_error = (db_host.equals(""))) {
                                errors.append("no db host specified\n");
                                has_error = true;
                            }
                            if (db_location.equals("")) {
                                errors.append("no db location specified\n");
                                has_error = true;
                            }
                            if (db_user.equals("")) {
                                errors.append("no db username specified\n");
                                has_error = true;
                            }
                            System.out.println("has_error=" + has_error);
                            if (has_error) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);
                                builder.setTitle("Error");
                                builder.setMessage(errors);
                                builder.setPositiveButton("OK", null);
                                System.out.println("will show");
                                AlertDialog aDialog = builder.show();
                            } else {
                                final String db_password = libraryPreferences.getString("db_password", "");
                                String files[] = new String[1];
                                files[0] = db_location + "/" + MLocate.localLibraryFile(library);
                                GetFile.doFileRequest(SearchableActivity.this, db_host, db_location,
                                        db_user, db_password,files);
                            }
                        }
                    }

            });


                edit_builder.setPositiveButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.out.println("cancelling");
                            }
                        });


                // create alert dialog
                //AlertDialog alertDialog = alertDialogBuilder.create();

                System.out.println("setbuttons done");
                System.out.flush();
                // show it
                AlertDialog edit_alertDialog = edit_builder.create();
                edit_alertDialog.show();
            }

            break;
            case R.id.menu_set_default_library: {
                final SharedPreferences data = getSharedPreferences("appData", 0);
                Set<String> libraries = data.getStringSet("libraries", new HashSet<String>());
                final AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);

                final String[] librariesString = libraries.toArray(new String[libraries.size()]);
                System.out.println("have builder");
                System.out.flush();
                builder.setTitle("Set default library");
                //builder.setMessage("Library to edit ");
                //final EditText input = new EditText(SearchableActivity.this);
                //input.setInputType(InputType.TYPE_CLASS_TEXT);
                for (String library : libraries)
                    System.out.println("library: " + library);
                System.out.println();

                //builder.setView(input);
                //System.out.println("setView"); System.out.flush();


                builder.setItems(librariesString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("which is " + which);
                        final String library = librariesString[which];
                        System.out.println("user_text is " + library);
                        SharedPreferences.Editor edit = data.edit();
                        edit.putString("default_library", library);
                        edit.commit();
                        currentLibrary = library;
                    }
                });


                builder.setPositiveButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.out.println("cancelling");
                            }
                        });


                // create alert dialog
                //AlertDialog alertDialog = alertDialogBuilder.create();

                System.out.println("setbuttons done");
                System.out.flush();
                // show it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();


                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    //@Override
    protected void onNewIntent(Intent intent) {
        System.out.println("onNewIntent");
        System.out.flush();
        handleIntent(intent, listValues);
    }

    private void handleIntent(Intent intent, List<Map<String, Object>> listValues) {
        System.out.println("handleIntent " + intent);
        System.out.flush();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            try {
                doMySearch(query, listValues);
            } catch (IOException exc) {
            }
            //finish();
        }
    }

    void doMySearch(final String query, List<Map<String, Object>> listValues) throws IOException {
        System.out.println("will search for " + query);
        System.out.flush();
        currentLibrary = find_current_library();

        if (currentLibrary != null) {
            final MLocate mloc = new MLocate(currentLibrary, 8192, SearchableActivity.this);
            final ProgressDialog pd = new ProgressDialog(SearchableActivity.this);
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    pd.setTitle("Processing...");
                    pd.setMessage("Please wait.");
                    pd.setCancelable(false);
                    pd.setIndeterminate(true);
                    pd.show();
                }

                @Override
                protected Void doInBackground(Void... arg0) {
                    try {
                        found = mloc.find(query);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("size of found: " + found.size());
                    System.out.flush();

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (pd != null) {
                        pd.dismiss();

                    }
                    EntryAdapter adapter = new EntryAdapter(SearchableActivity.this,
                            R.layout.listview_item_row, found);

                    System.out.println("before stack push");
                    System.out.flush();
                    stack = new ArrayList<DirView>();

                    DirView root = new DirView("---", adapter, null);
                    stack.add(root);
                    DirView dv = new DirView(name(mloc.root), adapter, found);
                    stack.add(dv);

                    System.out.println("after stack push");
                    System.out.flush();

                    setContentView(R.layout.mylist);
                    listView1 = (ListView) findViewById(android.R.id.list);
                    listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            System.out.println("onItemClick position=" + position + " found size=" +
                                    ((found == null) ? -1 : found.size()));
                            System.out.flush();
                            Entry entry = found.get(position);
                            System.out.println("item " + position + " was clicked=" + found.get(position));
                            if (entry.entryType != Entry.EntryType.File) {
                                System.out.println("will try to read " + entry);
                                List<Entry> dirEntries = mloc.read_dir(entry);
                                if (dirEntries != null) { //&& dirEntries.length > 0) {
                                    found = dirEntries;
                                    System.out.println("numer of entries are :" + dirEntries.size());
                                    System.out.flush();
                                    EntryAdapter adapter = new EntryAdapter(myself, R.layout.listview_item_row, dirEntries);
                                    System.out.println("computed new adapter");
                                    System.out.flush();
                                    listView1.setAdapter(adapter);
                                    System.out.println("set adapter");
                                    System.out.flush();
                                    DirView dv = new DirView(entry.fileName, adapter, dirEntries);
                                    stack.add(dv);
                                    System.out.println("pushed");
                                    System.out.flush();
                                    System.out.println("before spinner adapter");
                                    System.out.flush();

                                    spinnerAdapter = new OurSpinnerAdapter(SearchableActivity.this,
                                            android.R.layout.simple_spinner_item,
                                            //R.layout.spinner_item_row,
                                            //R.id.spinnerText,
                                            stack);

                                    System.out.println("after spinner adapter");
                                    System.out.flush();
                                    spinner.setSelection(stack.size() - 1);
                                    spinner.setAdapter(spinnerAdapter);
                                }
                            }
                        }

                    });

                    listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            Entry entry = found.get(position);
                            System.out.println("item " + position + " was longclicked=" + entry);
                            if (entry.entryType == Entry.EntryType.File) {
                            entry.isEnabled = !entry.isEnabled;
                            EntryAdapter.EntryHolder holder = (EntryAdapter.EntryHolder) view.getTag();
                            String fullPath = entry.dirName + "/" + entry.fileName;
                            if (toDownload.containsKey(fullPath))
                                toDownload.remove(fullPath);
                            else
                                toDownload.put(fullPath,entry);
                            if (entry.isEnabled)
                                holder.txtTitle.setBackgroundResource(android.R.color.holo_blue_light);
                            else
                                holder.txtTitle.setBackgroundResource(android.R.color.transparent);
                            System.out.println("ui is " + ui);
                            System.out.flush();
                            }


                            return true;
                        }

                    });
                    listView1.setAdapter(adapter);

                    System.out.println("before spinner adapter");
                    System.out.flush();

                    spinnerAdapter = new OurSpinnerAdapter(SearchableActivity.this,
                            android.R.layout.simple_spinner_item,
                            //R.id.spinnerText,
                            //R.layout.spinner_item_row,
                            stack);

                    System.out.println("after spinner adapter");
                    System.out.flush();
                }

            };
            task.execute((Void[]) null);

        } else {
            AlertDialog.Builder ok = new AlertDialog.Builder(SearchableActivity.this);
            ok.setTitle("Unspecified Library");
            ok.setMessage("Cannot figure out which library to use");

            ok.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });

            AlertDialog alert = ok.create();
            alert.show();
        }
    }

    String find_current_library() {
        SharedPreferences data =
                getSharedPreferences("appData", 0);
        Set<String> libraries =
                data.getStringSet("libraries", new HashSet<String>());

        if (currentLibrary != null && !currentLibrary.equals("") && libraries.contains(currentLibrary)) {
            System.out.println("currentLibrary is " + currentLibrary + " and is contained");
            return currentLibrary;
        }
        if (libraries.size() == 1) {
            String[] libraryObjects = libraries.toArray(new String[1]);
            System.out.println("current library does not exist; just one library " + libraryObjects[0]);
            return libraryObjects[0];
        }
        System.out.println("currentLibrary " + currentLibrary + " does not exist; size of libraries is " + libraries.size());
        return null;
    }


    String name(String path) {
        if (path == null) return null;
        File f = new File(path);
        return f.getName();
    }

    String size_to_string(long size) {
        if (size < 1024)
            return size + " bytes";
        else if (size < 1024 * 1024) {
            long KB = size / 1024;
            return KB + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            long MB = size / (1024 * 1024);
            return MB + " MB";
        } else {
            long GB = size / (1024 * 1024 * 1024);
            return GB + " GB";
        }
    }
}

// Broadcast receiver for receiving status updates from the IntentService
class TransferResponseReceiver extends BroadcastReceiver {
    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    public void onReceive(Context context, Intent intent) {
        System.out.println("got an intent " + intent);
    }
}