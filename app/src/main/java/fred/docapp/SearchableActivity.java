package fred.docapp;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class SearchableActivity extends AppCompatActivity {

    private List<Map<String, Object>> listValues;
    private ListView listView = null;
    Entry found[] = null;
    private ListView listView1 = null;
    private MenuItem spinnerItem = null;
    private Menu menu;
    ArrayList<DirView> stack = null;
    SearchableActivity myself = null;
    OurSpinnerAdapter spinnerAdapter = null;
    Spinner spinner = null;
    UserInfo ui;
    String locateDBlocation = null;
    String currentLibrary = null;
    Map<String,Entry> toDownload = new HashMap<String,Entry>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("search activitity was created");
        System.out.flush();
        super.onCreate(savedInstanceState);
        currentLibrary = null;

         Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        listValues = new ArrayList<Map<String, Object>>();
        ui = new UserInfo();

        // Get the intent, verify the action and get the query
        System.out.println("before intent");
        System.out.flush();
        Intent intent = getIntent();
        System.out.println("after intent");
        System.out.flush();
        handleIntent(intent, listValues);


        //prepareData(listValues);
        /*

        listView = (ListView) findViewById(android.R.id.list);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("item " + position + " was clicked=" + listValues.get(position));
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("item " + position + " was longclicked=" + listValues.get(position));
                return true;
            }

        });

        SimpleAdapter adapter =
                new SimpleAdapter(this, listValues, android.R.layout.simple_list_item_1,
                        new String[]{"1"}, new int[]{android.R.id.text1});
        listView.setAdapter(adapter);
        */


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

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        System.out.println("onCreateOptionsMenu: item is " + menu.findItem(R.id.menu_search));
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        System.out.println("searchView=" + searchView);
        System.out.flush();
        //try { wait(200); } catch (InterruptedException exc) {};
        ComponentName cn = new ComponentName(this, SearchableActivity.class);
       // try { wait(200); } catch (InterruptedException exc) {};
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        //try { wait(200); } catch (InterruptedException exc) {};
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        //try { wait(200); } catch (InterruptedException exc) {};
        System.out.println("before spinning");
        System.out.flush();
        //try { wait(200); } catch (InterruptedException exc) {};
        System.out.println("onCreateOptionsMenu: spinner item is " + menu.findItem(R.id.spinner));
        System.out.flush();
        //try { wait(200); } catch (InterruptedException exc) {};
        if (menu.findItem(R.id.spinner) == null) {
            try { wait(200); } catch (InterruptedException exc) {};
            new Exception().printStackTrace();
        }
       // try { wait(200); } catch (InterruptedException exc) {};
        spinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        //try { wait(200); } catch (InterruptedException exc) {};
        System.out.println("Spinner is " + spinner + " spinner adapter is " + spinnerAdapter);
        System.out.flush();
        spinner.setAdapter(spinnerAdapter); // set the adapter to provide layout of rows and
        // content
        System.out.println("stack is "+stack);
        System.out.flush();
        if (stack != null)
          spinner.setSelection(stack.size()-1);
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
        case R.id.menu_download: {
            if (toDownload.size()>0) {
                final String library = find_current_library(currentLibrary);
                final String username;
                final String location;
                if (library != null) {
                    if ((location = find_location(library)) != null) {
                        if ((username = find_username(library)) != null) {
                            final String password = find_password(library);
                            if (password == null) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);

                                builder.setTitle("password");
                                builder.setMessage("password");

                                final EditText input = new EditText(SearchableActivity.this);

                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                builder.setView(input);

                                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {

                                        String result = input.getText().toString();
                                        String files[] = new String[toDownload.size()];
                                        int i = 0;
                                        for (Entry entry : toDownload.values()) {
                                            files[i++] = entry.dirName + "/" + entry.fileName;
                                        }
                                        FileTransferRequest ftr = new FileTransferRequest(location,username,password,files);
                                        Intent intent = new Intent(SearchableActivity.this, FileService.class);
                                        intent.putExtra("fred.docApp.FileTransferRequest",ftr);
                                        startService(intent);



                                    }

                                });

                                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                            }
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);
                        builder.setTitle("Error");
                        builder.setMessage("Don't know which library to use");
                        builder.setPositiveButton("OK", null);
                        AlertDialog dialog = builder.show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchableActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage("Don't know where library is located");
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
                            return;
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
                            return;
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
                }});


            builder.setPositiveButton("cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.out.println("cancelling");
                            return;
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

 private void handleIntent(Intent intent,List<Map<String,Object>> listValues) {
        System.out.println("handleIntent " + intent);
        System.out.flush();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            doMySearch(query,listValues);
            //finish();
        }
    }

    void doMySearch(final String query,List<Map<String,Object>> listValues) {
      System.out.println("will search for " + query);
        System.out.flush();
	currentLibrary = find_current_library(currentLibrary);
	if (currentLibrary != null) {
        final MLocate mloc = new MLocate(currentLibrary);
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
            found = mloc.find(query,SearchableActivity.this);
            System.out.println("size of found: " + found.length);
            System.out.flush();

        /*for (Entry entry : found) {
            String value;
            Map<String,Object> item = new HashMap<String,Object>();
            if (entry.isDir)
                value = entry.dirName + "/";
            else
                value = entry.fileName + " ("+size_to_string(entry.size)+")";
            item.put("1",value);
            listValues.add(item);
        }*/

            return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (pd!=null) {
				pd.dismiss();

			}
            EntryAdapter adapter = new EntryAdapter(SearchableActivity.this,
                    R.layout.listview_item_row, found);

            System.out.println("before stack push");
            System.out.flush();
            stack = new ArrayList<DirView>();

            DirView root = new DirView("---",adapter,null);
            stack.add(root);
            DirView dv = new DirView(name(mloc.root),adapter,found);
            stack.add(dv);

            System.out.println("after stack push");
            System.out.flush();

            setContentView(R.layout.mylist);
            listView1 = (ListView)findViewById(android.R.id.list);
            listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println("onItemClick position="+position+" found size="+
                            ((found==null)?-1:found.length));
                    System.out.flush();
                    Entry entry = found[position];
                    System.out.println("item " + position + " was clicked=" + found[position]);
                    if (entry.entryType != Entry.EntryType.File) {
                        System.out.println("will try to read " + entry);
                        Entry[] dirEntries = mloc.read_dir(SearchableActivity.this, entry);
                        if (dirEntries != null) { //&& dirEntries.length > 0) {
                            found = dirEntries;
                            System.out.println("numer of entries are :" + dirEntries.length);
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
                            spinner.setSelection(stack.size()-1);
                            spinner.setAdapter(spinnerAdapter);
                        }
                    }
                }

            });

            listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Entry entry = found[position];
                    System.out.println("item " + position + " was longclicked=" + entry);
                    entry.isEnabled = !entry.isEnabled;
                    EntryAdapter.EntryHolder holder = (EntryAdapter.EntryHolder) view.getTag();
                    String fullPath = entry.dirName+"/"+entry.fileName;
                    if (toDownload.containsKey(fullPath))
                        toDownload.remove(fullPath);
                    else
                        toDownload.put(fullPath, entry);
                    if (entry.isEnabled)
                        holder.image.setImageResource(R.drawable.ic_done_white);
                    else
                        holder.image.setImageBitmap(null);
                    System.out.println("ui is "+ui);
                    System.out.flush();;

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

	}
	else {
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

    String find_current_library(String currentLibrary) {
	SharedPreferences data =
	    getSharedPreferences("appData", 0);
	Set<String> libraries =
	    data.getStringSet("libraries", new HashSet<String>());
	String DefaultLibrary =
	    data.getString("default_library", "");
	
	if (currentLibrary != "" && libraries.contains(currentLibrary))
	    return currentLibrary;
	if (DefaultLibrary != "" && libraries.contains(DefaultLibrary))
	    return DefaultLibrary;
	if (libraries.size() == 1) {
	    String[] libraryObjects = libraries.toArray(new String[1]);
	    return libraryObjects[0];
	}
	return null;
    }

    String find_location(String library) {
        SharedPreferences libraryPreferences = getSharedPreferences(library,0);
        String location = libraryPreferences.getString("library_location", null);
        return location;
    }

    String find_username(String library) {
        SharedPreferences libraryPreferences = getSharedPreferences(library,0);
        String username = libraryPreferences.getString("library_username", null);
        return username;
    }

    String find_password(String library) {
        SharedPreferences libraryPreferences = getSharedPreferences(library,0);
        String password = libraryPreferences.getString("library_password",null);
        return password;
    }

    String name(String path) {
        if (path==null) return null;
        File f = new File(path);
        return f.getName();
    }

    String size_to_string(long size) {
        if (size < 1024)
            return size+" bytes";
        else if (size < 1024*1024) {
            long KB = size / 1024;
            return KB+" KB";
        } else if (size < 1024*1024*1024) {
            long MB = size / (1024 * 1024);
            return MB + " MB";
        } else if (size < 1024*1024*1024*1024) {
            long GB= size / (1024 * 1024 * 1024);
            return GB + " MB";
        } else return "very big";
    }
}
