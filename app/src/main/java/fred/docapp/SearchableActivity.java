package fred.docapp;

import android.app.ListActivity;
import android.app.SearchManager;
import android.os.Bundle;
import android.content.Intent;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchableActivity extends ListActivity { //AppCompatActivity {

    private List<Map<String,Object>> listValues;
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("search activitity was created");
        super.onCreate(savedInstanceState);
        listValues = new ArrayList<Map<String,Object>>();
        prepareData(listValues);
        setContentView(R.layout.mylist);
        listView = (ListView) findViewById(android.R.id.list);

    SimpleAdapter adapter = new SimpleAdapter(this, listValues,android.R.layout.simple_list_item_1,new String[] { "1" },new int[] { android.R.id.text1 });
    listView.setAdapter(adapter);

        // Get the intent, verify the action and get the query
        System.out.println("before intent");
        System.out.flush();
        Intent intent = getIntent();
        System.out.println("after intent");
        System.out.flush();
        handleIntent(intent);
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
        handleIntent(intent);
    }

 private void handleIntent(Intent intent) {
        System.out.println("handleIntent "+intent);
        System.out.flush();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            doMySearch(query);
            //finish();
        }
    }

    void doMySearch(String query) {
      System.out.println("will search for "+query);
        System.out.flush();
    }
}
