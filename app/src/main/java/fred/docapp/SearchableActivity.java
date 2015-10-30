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
        setContentView(R.layout.mylist);
        listView = (ListView) findViewById(android.R.id.list);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("item "+position+" was clicked="+listValues.get(position));
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("item "+position+" was longclicked="+listValues.get(position));
                return true;
            }

        });

        SimpleAdapter adapter =
                new SimpleAdapter(this, listValues, android.R.layout.simple_list_item_1,
                        new String[]{"1"}, new int[]{android.R.id.text1});
        listView.setAdapter(adapter);


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

    void prepareData(List<Map<String,Object>> listValues) {
        Map<String,Object> item;
        item = new HashMap<String,Object>();
        item.put("1","A");
        listValues.add(item);
        item = new HashMap<String,Object>();
        item.put("1","B");
        listValues.add(item);
    }

    //@Override
    protected void onNewIntent(Intent intent) {
        System.out.println("onNewIntent");
        System.out.flush();
        handleIntent(intent,listValues);
    }

 private void handleIntent(Intent intent,List<Map<String,Object>> listValues) {
        System.out.println("handleIntent "+intent);
        System.out.flush();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            doMySearch(query,listValues);
            //finish();
        }
    }

    void doMySearch(String query,List<Map<String,Object>> listValues) {
      System.out.println("will search for "+query);
        System.out.flush();
        Entry[] found = MLocate.find(query,SearchableActivity.this);
        System.out.println("size of found: "+found.length);
        System.out.flush();
        for (Entry entry : found) {
            Map<String,Object> item = new HashMap<String,Object>();
            if (entry.isDir)
                item.put("1",entry.dirName);
            else
                item.put("1",entry.fileName);
            listValues.add(item);
        }
    }
}
