package fred.docapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import org.json.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SavedSearchData {
    List<Entry> entries = null;
    ArrayList<DirView> stack;
    Map<String, Entry> toDownload;
    MLocate mloc;
}

public class SearchableActivity extends AppCompatActivity {
    // pointer to GUI list
    //private ListView listView1 = null;

    List<Entry> entries = null;
    ArrayList<DirView> stack = null;
    static Spinner spinner = null;
    String currentLibrary = null;
    Map<String, Entry> toDownload;
    private SaveFragment saveFragment;
    private MLocate mloc = null;

    final int PERMISSION_REQUEST_CODE = 111;

    private boolean checkReadExternalStoragePermission() {
        int result = ContextCompat.checkSelfPermission(SearchableActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkWriteExternalStoragePermission() {
        int result = ContextCompat.checkSelfPermission(SearchableActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("search activitity was created");
        System.out.flush();
        super.onCreate(savedInstanceState);

        final int PERMISSION_REQUEST_CODE = 111;

        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkWriteExternalStoragePermission() || !checkReadExternalStoragePermission()) {
                requestPermission();
            }
        }

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

        SharedPreferences appData = getSharedPreferences("appData", 0);
        currentLibrary = appData.getString("default_library", null);

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        saveFragment = (SaveFragment) fm.findFragmentByTag("data");

        if (saveFragment == null) {
            // First time activity is started
            System.out.println("external storage dir=" + Environment.getExternalStorageDirectory());
            System.out.println("external public storage dir (downloads)=" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
            System.out.println("pictures=" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));

            toDownload = new HashMap<>();
            stack = new ArrayList<DirView>();
            entries = new ArrayList<Entry>();

            saveFragment = new SaveFragment();
            fm.beginTransaction().add(saveFragment, "data").commit();

            // Get the intent, verify the action and get the query
            System.out.println("before intent");
            System.out.flush();
            Intent intent = getIntent();
            System.out.println("after intent");
            System.out.flush();
            handleIntent(intent);
        } else {
            // Activitiy restart (e.g., due to rotation)
            SavedSearchData sd = saveFragment.getData();
            toDownload = sd.toDownload;
            entries = sd.entries;
            stack = sd.stack;
            mloc = sd.mloc;
        }

        setContentView(R.layout.mylist);
        EntryAdapter adapter = new EntryAdapter(SearchableActivity.this,
                R.layout.listview_item_row, entries);
        final ListView listView1 = (ListView) findViewById(android.R.id.list);
        final ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        setImageButtonEnabled(SearchableActivity.this, false, backButton, R.mipmap.ic_navigate_before);
        backButton.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("back button clicked");
                int position = stack.size()-2;
                System.out.println("backButton: position="+position);
                Log.i("setOnItemSelected", "correct position; will get");
                DirView dv = stack.get(position);
                Log.i("setOnItemSelected", "correct position; will setAdapter for listview");
                ListView listView1 = (ListView) findViewById(android.R.id.list);
                EntryAdapter adapter = (EntryAdapter) listView1.getAdapter();
                entries = dv.entries;
                //Collections.sort(entries, new Comparator<Entry>() {
                 //   @Override
                 //   public int compare(Entry lhs, Entry rhs) {
                 //       return lhs.normalizedName.compareTo(rhs.normalizedName);
                  //  }
                //});
                adapter.clear();
                adapter.addAll(entries);
                adapter.notifyDataSetChanged();
                Log.i("setOnItemSelected", "before new spinneradapter on create optionsmenu, item selected");
                //spinnerAdapter = new OurSpinnerAdapter(SearchableActivity.this,
                //android.R.layout.simple_spinner_item,
                //R.layout.spinner_item,
                //R.layout.spinner_item_row,
                //R.id.spinnerText,
                //stack);
                Log.i("setOnItemSelected", "setSelection");
                spinner.setSelection(position);
                Log.i("setOnItemSelected", "correct position; will setAdapter for spinner");
                //spinner.setAdapter(spinnerAdapter);
                getSpinnerAdapter(spinner, stack).notifyDataSetChanged();
                final ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
                System.out.println("Stack size is "+stack.size());
                boolean enabled = stack.size() > 2;
                setImageButtonEnabled(SearchableActivity.this,enabled,backButton,R.mipmap.ic_navigate_before);
            }
        });

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("onItemClick position=" + position + " found size=" +
                        ((entries == null) ? -1 : entries.size()));
                System.out.flush();
                final Entry entry = entries.get(position);
                System.out.println("item " + position + " was clicked=" + entries.get(position));
                if (entry.entryType != Entry.EntryType.File) {
                    System.out.println("will try to read " + entry);
                    List<Entry> dirEntries = mloc.read_dir(entry);
                    if (dirEntries != null) { //&& dirEntries.length > 0) {
                        entries = dirEntries;
                        System.out.println("numer of entries are :" + dirEntries.size());
                        System.out.flush();
                        // EntryAdapter adapter = new EntryAdapter(SearchableActivity.this, R.layout.listview_item_row, dirEntries);
                        EntryAdapter adapter = (EntryAdapter) listView1.getAdapter();
                        adapter.clear();
                        Collections.sort(dirEntries, new Comparator<Entry>() {
                            @Override
                            public int compare(Entry lhs, Entry rhs) {
                                return lhs.normalizedName.compareTo(rhs.normalizedName);
                            }
                        });
                        adapter.addAll(dirEntries);
                        adapter.notifyDataSetChanged();
                        //listView1.setAdapter(adapter);
                        DirView dv = new DirView(entry.fileName, dirEntries);
                        stack.add(dv);
                        System.out.println("pushed");
                        System.out.flush();
                        spinner.setSelection(stack.size() - 1);
                        setImageButtonEnabled(SearchableActivity.this, true, backButton, R.mipmap.ic_navigate_before);
                        //spinner.setAdapter(spinnerAdapter);
                        getSpinnerAdapter(spinner, stack).notifyDataSetChanged();
                    }
                } else {
                    AlertDialog.Builder infoDialog = new AlertDialog.Builder(SearchableActivity.this);
                    infoDialog.setTitle("File information");
                    infoDialog.setMessage("Name: " + entry.fileName + "\ndirectory: " + entry.dirName +
                            "\nsize: " + size_to_string(entry.size));

                    infoDialog.setNegativeButton("Download", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if (currentLibrary != null) {
                                String files[] = new String[1];
                                files[0] = entry.dirName + "/" + entry.fileName;
                                entry.isEnabled = false;
                                EntryAdapter adapter = (EntryAdapter) listView1.getAdapter();
                                adapter.notifyDataSetChanged();
                                GetFile.doFileRequest(currentLibrary, files, false, SearchableActivity.this);
                            }
                            dialog.dismiss();
                        }
                    });

                    infoDialog.setPositiveButton("Open", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if (currentLibrary != null) {
                                String files[] = new String[1];
                                files[0] = entry.dirName + "/" + entry.fileName;
                                entry.isEnabled = false;
                                EntryAdapter adapter = (EntryAdapter) listView1.getAdapter();
                                adapter.notifyDataSetChanged();
                                GetFile.doFileRequest(currentLibrary, files, true, SearchableActivity.this);
                            }
                            dialog.dismiss();
                        }
                    });

                    infoDialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = infoDialog.create();
                    alert.show();

                }
            }

        });

        listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = entries.get(position);
                System.out.println("item " + position + " was longclicked=" + entry);
                if (entry.entryType == Entry.EntryType.File) {
                    entry.isEnabled = !entry.isEnabled;
                    EntryAdapter.EntryHolder holder = (EntryAdapter.EntryHolder) view.getTag();
                    String fullPath = entry.dirName + "/" + entry.fileName;
                    if (toDownload.containsKey(fullPath))
                        toDownload.remove(fullPath);
                    else
                        toDownload.put(fullPath, entry);
                    if (entry.isEnabled)
                        holder.txtTitle.setBackgroundResource(android.R.color.holo_blue_light);
                    else
                        holder.txtTitle.setBackgroundResource(android.R.color.transparent);
                    System.out.flush();
                }


                return true;
            }

        });

        listView1.setAdapter(adapter);


    }

public static void setImageButtonEnabled(Context ctxt, boolean enabled, ImageButton item,
        int iconResId) {
    item.setEnabled(enabled);
    Drawable originalIcon = ctxt.getResources().getDrawable(iconResId);
    Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
    item.setImageDrawable(icon);
}

public static Drawable convertDrawableToGrayScale(Drawable drawable) {
    if (drawable == null) {
        return null;
    }
    Drawable res = drawable.mutate();
    res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
    return res;
}

    @Override
    protected void onDestroy() {
        System.out.println("onDestroy called");
        System.out.flush();
        super.onDestroy();

        SavedSearchData sd = new SavedSearchData();
        sd.entries = entries;
        sd.stack = stack;
        sd.toDownload = toDownload;
        sd.mloc = mloc;
        saveFragment.setData(sd);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        System.out.println("onCreateOptionsMenu");
        System.out.flush();
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);

        //(new SpeedTest()).test1(SearchableActivity.this);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        System.out.println("onCreateOptionsMenu: item is " + menu.findItem(R.id.menu_search));
         MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        searchMenuItem.expandActionView();
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
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
            } catch (InterruptedException ignored) {
            }

            new Exception().printStackTrace();
        }
        spinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        System.out.println("Spinner is " + spinner);
        System.out.flush();
        getSpinnerAdapter(spinner, stack);
        //spinner.setAdapter(spinnerAdapter); // set the adapter to provide layout of rows and
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
                                                      ListView listView1 = (ListView) findViewById(android.R.id.list);
                                                      EntryAdapter adapter = (EntryAdapter) listView1.getAdapter();
                                                      entries = dv.entries;
                                                      //Collections.sort(entries, new Comparator<Entry>() {
                                                       //   @Override
                                                       //   public int compare(Entry lhs, Entry rhs) {
                                                        //      return lhs.normalizedName.compareTo(rhs.normalizedName);
                                                        //  }
                                                      //});
                                                      adapter.clear();
                                                      adapter.addAll(entries);
                                                      adapter.notifyDataSetChanged();
                                                      Log.i("setOnItemSelected", "before new spinneradapter on create optionsmenu, item selected");
                                                      //spinnerAdapter = new OurSpinnerAdapter(SearchableActivity.this,
                                                      //android.R.layout.simple_spinner_item,
                                                      //R.layout.spinner_item,
                                                      //R.layout.spinner_item_row,
                                                      //R.id.spinnerText,
                                                      //stack);
                                                      Log.i("setOnItemSelected", "setSelection");
                                                      spinner.setSelection(position);
                                                      Log.i("setOnItemSelected", "correct position; will setAdapter for spinner");
                                                      //spinner.setAdapter(spinnerAdapter);
                                                      getSpinnerAdapter(spinner, stack).notifyDataSetChanged();
                                                      final ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
                                                      System.out.println("Stack size is "+stack.size());
                                                      boolean enabled = stack.size() > 2;
                                                      setImageButtonEnabled(SearchableActivity.this,enabled,backButton,R.mipmap.ic_navigate_before);
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
            case R.id.menu_settings: {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            }
            case R.id.menu_transfer_status: {
                Intent transferStatusIntent = new Intent(this, TransferStatusActivity.class);
                startActivity(transferStatusIntent);
                break;
            }
            case R.id.menu_download: {
                ListView listView1 = (ListView) findViewById(android.R.id.list);
                System.out.println("in download");
                if (toDownload.size() > 0) {
                    final String library = find_current_library();
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
                        toDownload = new HashMap<>();
                        GetFile.doFileRequest(library, files, false, SearchableActivity.this);
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
            case R.id.menu_download_library_definitions: {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);
                builder.setMessage("password");
                final EditText input = new EditText(SearchableActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString();
                        new DownloadLibraryInformationTask().execute(password);
                    }

                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.show();
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
                                appDataEditor.apply();
                                SharedPreferences.Editor libraryEditor = libraryPreferences.edit();
                                libraryEditor.clear();
                                libraryEditor.apply();
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
                            newIntent.putExtra("db_port", libraryPreferences.getString
                                    ("db_port", ""));
                            newIntent.putExtra("db_location", libraryPreferences.getString
                                    ("db_location", ""));
                            newIntent.putExtra("db_username", libraryPreferences.getString
                                    ("db_username", ""));
                            newIntent.putExtra("db_password", libraryPreferences.getString
                                    ("db_password", ""));
                            newIntent.putExtra("library_host", libraryPreferences
                                    .getString
                                            ("library_host", ""));
                            newIntent.putExtra("library_port", libraryPreferences
                                    .getString
                                            ("library_port", ""));
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
                edit_builder.setTitle("Download library index");
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
                            final String db_port = libraryPreferences.getString("db_port", "22");
                            System.out.println("db_host=" + db_host + " db_location=" + db_location + " db_user=" + db_user + " db_port=" + db_port);
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
                                GetFile.doFileRequest(SearchableActivity.this, library, db_host, db_port, SearchableActivity.this.getFilesDir().getAbsolutePath(),
                                        db_user, db_password, files, false);
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
            case R.id.menu_about: {
                final AlertDialog.Builder billy_builder = new AlertDialog.Builder(SearchableActivity.this);
                LayoutInflater inflater = this.getLayoutInflater();
                billy_builder.setView(inflater.inflate(R.layout.billy, null));
                billy_builder.show();
                break;
            }
            case R.id.menu_sort_alfa: {
                ListView listView1 = (ListView) findViewById(android.R.id.list);
                EntryAdapter adapter = (EntryAdapter) listView1.getAdapter();
                List<Entry> entries = adapter.data;
                Collections.sort(entries, new Comparator<Entry>() {
                    @Override
                    public int compare(Entry lhs, Entry rhs) {
                        return lhs.normalizedName.compareTo(rhs.normalizedName);
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            }
            case R.id.menu_set_default_library: {
                final SharedPreferences data = getSharedPreferences("appData", 0);
                Set<String> libraries = data.getStringSet("libraries", new HashSet<String>());
                final AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);

                final String[] librariesString = libraries.toArray(new String[libraries.size()]);
                System.out.println("have builder");
                System.out.flush();
                builder.setTitle("Set default library (" + find_current_library() + ")");
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
                        edit.apply();
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
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        System.out.println("handleIntent " + intent);
        System.out.flush();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            try {
                System.out.println("spinner in handleIntent is "+spinner);
                doMySearch(query);
            } catch (IOException ignored) {
            }
            //finish();
        }
    }

    void doMySearch(final String query) throws IOException {
        System.out.println("will search for " + query);
        System.out.flush();
        currentLibrary = find_current_library();

        if (currentLibrary != null) {
            mloc = new MLocate(currentLibrary, 8192, SearchableActivity.this);
            final ProgressDialog pd = new ProgressDialog(SearchableActivity.this);
            AsyncTask<Spinner, Void, Boolean> task = new AsyncTask<Spinner, Void, Boolean>() {
                Spinner spin;

                @Override
                protected void onPreExecute() {
                    pd.setTitle("Processing...");
                    pd.setMessage("Please wait.");
                    pd.setCancelable(false);
                    pd.setIndeterminate(true);
                    pd.show();
                }

                @Override
                protected Boolean doInBackground(Spinner... spinners) {
                    System.out.println("doInBackground: spinners[0]="+spinners[0]);
                    spin = spinners[0];
                    try {
                        entries = mloc.find(query);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    System.out.println("size of found: " + entries.size());
                    System.out.flush();

                    return true;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    if (pd != null) {
                        pd.dismiss();
                    }
                    if (result == true) {
                        System.out.println("before stack push");
                        System.out.flush();
                        stack.clear();
                        final ListView listView1 = (ListView) findViewById(android.R.id.list);
                        EntryAdapter adapter = (EntryAdapter) listView1.getAdapter();
                        adapter.clear();
                        Collections.sort(entries, new Comparator<Entry>() {
                            @Override
                            public int compare(Entry lhs, Entry rhs) {
                                return lhs.normalizedName.compareTo(rhs.normalizedName);
                            }
                        });
                        adapter.addAll(entries);
                        adapter.notifyDataSetChanged();
                        DirView root = new DirView("---", null);
                        stack.add(root);
                        DirView dv = new DirView(name(mloc.root), entries);
                        stack.add(dv);

                        System.out.println("after stack push");
                        System.out.flush();

                        System.out.println("spinner in asynctask is " + spin);
                    } else {
                        AlertDialog.Builder ok = new AlertDialog.Builder(SearchableActivity.this);
                        ok.setTitle("Missing Library Index");
                        ok.setMessage("The library index is missing; please download it");

                        ok.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }

                        });

                        AlertDialog alert = ok.create();
                        alert.show();
                    }
                    getSpinnerAdapter(spin, stack).notifyDataSetChanged();
                }

            };
            System.out.println("spinner when executing is "+spinner);
            task.execute(spinner);

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

    OurSpinnerAdapter getSpinnerAdapter(Spinner spinner, ArrayList<DirView> stack) {
        if (spinner == null) System.out.println("spinner is null -- about to crash!");
        OurSpinnerAdapter spinnerAdapter = (OurSpinnerAdapter) spinner.getAdapter();
        if (spinnerAdapter == null) {
            spinnerAdapter = new OurSpinnerAdapter(SearchableActivity.this, stack);
            spinner.setAdapter(spinnerAdapter);
        }
        return spinnerAdapter;
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

    class DownloadLibraryInformationTask extends AsyncTask<String,Void,String> {
        protected String doInBackground(String... strings) {
            String password = strings[0];

            StringBuffer sbuf = new StringBuffer();
            ScpFromJava scp = new ScpFromJava(SearchableActivity.this);
            ScpReturnStatus retStatus = scp.setupTransfer("fred", password, "tabitha.ls.fi.upm.es", 22, "/home/fred/private/locates/libraries.json");
            if (retStatus.is_ok) {
                retStatus = scp.doTransfer(sbuf);
            }

            if (!retStatus.is_ok) return null;
            else return sbuf.toString();
        }

        protected void onPostExecute(String result) {
            String errorString = null;
            JSONObject json = null;

            if (result != null) {
                try {
                    json = new JSONObject(result);
                } catch (JSONException exc) {
                    errorString = "cannot parse\n"+result;
                }
            } else errorString = "cannot download library information file";

            if (result == null || json == null) {
                AlertDialog.Builder ok = new AlertDialog.Builder(SearchableActivity.this);
                ok.setTitle("Download library information error");
                ok.setMessage("result = " + result);
                ok.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
                AlertDialog alert = ok.create();
                alert.show();
            } else {
                try {
                    String library = null;
                    String db_host = null;
                    String db_port = null;
                    String db_location = null;
                    String db_username = null;
                    String db_password = null;
                    String host = null;
                    String port = null;
                    String username = null;
                    String password = null;

                    JSONArray arr = json.getJSONArray("libraries");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject def = arr.getJSONObject(i);
                        if (def.has("name")) library = def.getString("name");
                        if (def.has("db_host")) db_host = def.getString("db_host");
                        if (def.has("db_port")) db_port = String.valueOf(def.getInt("db_port"));
                        if (def.has("db_username")) db_username = def.getString("db_username");
                        if (def.has("db_location")) db_location = def.getString("db_location");
                        if (def.has("db_password")) db_password = def.getString("db_password");
                        if (def.has("host")) host = def.getString("host");
                        if (def.has("port")) port = String.valueOf(def.getInt("port"));
                        if (def.has("username")) username = def.getString("username");
                        if (def.has("password")) password = def.getString("password");
                        AddLibraryInformation.add(SearchableActivity.this, true, library,
                                db_host, db_port, db_location, db_username, db_password, host, port, username,
                                password);
                    }
                } catch (JSONException e) {
                    AlertDialog.Builder ok = new AlertDialog.Builder(SearchableActivity.this);
                    ok.setTitle("Could not parse JSON data");
                    ok.setMessage("json data: " + json.toString());
                    ok.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    });
                    AlertDialog alert = ok.create();
                    alert.show();
                }
            }
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

