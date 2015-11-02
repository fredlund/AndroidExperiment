package fred.docapp;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;

public class NewApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_app);
    }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
   // Inflate the options menu from XML
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.my_menu, menu);

       System.out.println("NewApp running");
       System.out.flush();

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        System.out.println("item is "+menu.findItem(R.id.menu_search));
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        System.out.println("searchView=" + searchView);
        System.out.flush();
        ComponentName cn = new ComponentName(this, SearchableActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
       searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

      return true;
    }
}
