package fred.docapp;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("search activitity was created");
        System.out.flush();
        super.onCreate(savedInstanceState);

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
        System.out.println("Spinner is " + spinner + " spinner adapter is "+spinnerAdapter);
        System.out.flush();
        spinner.setAdapter(spinnerAdapter); // set the adapter to provide layout of rows and
        // content
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                  System.out.println("onItemSelected position=" + position+" size="+stack.size());
                                                  if (position < stack.size()-1 && position > 0) {
                                                      for (int i = stack.size() - 1; i > position; i--) {
                                                          stack.remove(i);
                                                      }
                                                      DirView dv = stack.get(position);
                                                      listView1.setAdapter(dv.adapter);
                                                      found = dv.entries;
                                                      spinnerAdapter = new OurSpinnerAdapter(SearchableActivity.this,
                                                              android.R.layout.simple_spinner_item,
                                                              //R.layout.spinner_item_row,
                                                              //R.id.spinnerText,
                                                              stack);
                                                      spinner.setAdapter(spinnerAdapter);
                                                  }
                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> parent) {
                                                  System.out.println("onNothingSelected");
                                              }
                                          }

            );

        return true;
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
        final MLocate mloc = new MLocate();
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
                        if (dirEntries != null && dirEntries.length > 0) {
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
                    if (entry.isEnabled)
                        holder.image.setImageResource(R.drawable.ic_done_white);
                    else
                        holder.image.setImageBitmap(null);
                    System.out.println("ui is "+ui);
                    System.out.flush();
                    ui.getPassword(SearchableActivity.this,new UserHost("fred","tabitha"));

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
