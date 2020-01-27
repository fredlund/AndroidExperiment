package fred.docapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fred on 5/06/17.
 */

public class AddLibraryInformation {
    public static boolean add(Activity activity,
                                   boolean isNew,
                                   String library,String db_host, String db_port, String db_location,
                                 String db_username, String db_password, String host, String port, String username,
                                 String password) {
        SharedPreferences libraryPreferences = activity.getSharedPreferences(library, 0);
        if (isNew && libraryPreferences.contains("is_created")) {
            return false;
        } else {
            SharedPreferences.Editor editor = libraryPreferences.edit();
            editor.clear();
            editor.putBoolean("is_created", true);
            editor.putString("db_host", db_host);
            editor.putString("db_port", db_port);
            editor.putString("db_location", db_location);
            editor.putString("db_username", db_username);
            editor.putString("db_password", db_password);
            editor.putString("library_host", host);
            editor.putString("library_port", port);
            editor.putString("library_username", username);
            editor.putString("library_password", password);
            editor.apply();
            SharedPreferences appData = activity.getSharedPreferences("appData",0);
            Set<String> libraries = appData.getStringSet("libraries", new
                    HashSet<String>());
            Set<String> copiedLibraries = new HashSet<String>(libraries);
            SharedPreferences.Editor appDataEditor = appData.edit();
            appDataEditor.remove("libraries");
            copiedLibraries.add(library);
            appDataEditor.putStringSet("libraries",copiedLibraries);
            appDataEditor.apply();
        }
        return true;
    }
}
