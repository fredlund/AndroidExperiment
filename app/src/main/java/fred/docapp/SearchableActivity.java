package fred.docapp;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchableActivity extends AppCompatActivity {

    private List<Map<String, Object>> listValues;
    private ListView listView = null;
    Entry found[] = null;
    private ListView listView1 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("search activitity was created");
        super.onCreate(savedInstanceState);
        listValues = new ArrayList<Map<String, Object>>();

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
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        System.out.println("item is " + menu.findItem(R.id.menu_search));
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        System.out.println("searchView=" + searchView);
        System.out.flush();
        ComponentName cn = new ComponentName(this, SearchableActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

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

    void doMySearch(String query,List<Map<String,Object>> listValues) {
      System.out.println("will search for "+ query);
        System.out.flush();
        MLocate mloc = new MLocate();
        found = mloc.find(query,SearchableActivity.this);
        System.out.println("size of found: "+found.length);
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
         EntryAdapter adapter = new EntryAdapter(this,
                R.layout.listview_item_row, found);

        setContentView(R.layout.mylist);
         listView1 = (ListView)findViewById(android.R.id.list);
         listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("item " + position + " was clicked=" + found[position]);
            }

        });

        listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = found[position];
                System.out.println("item " + position + " was longclicked=" + entry);
                entry.isEnabled = !entry.isEnabled;
                EntryAdapter.EntryHolder holder = (EntryAdapter.EntryHolder)view.getTag();
                if (entry.isEnabled)
                    holder.image.setImageResource(R.drawable.ic_done_white);
                else
                    holder.image.setImageBitmap(null);

                return true;
            }

        });
        listView1.setAdapter(adapter);
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
