package fred.docapp;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashSet;
import java.util.Set;

public class AddLibrary extends AppCompatActivity {
    boolean is_new = true;
    String libraryName;

    String editTextToString(int id)  {
        EditText editText = (EditText) findViewById(id);
        return editText.getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
if (extras != null) {
   is_new = false;
}
        System.out.println("in onCreate; is_new = "+is_new);
        if (!is_new) {
            putStringToEditText(extras.getString("libraryName"), R.id.libraryName);
            putStringToEditText(extras.getString("db_host"), R.id.db_host);
            putStringToEditText(extras.getString("db_port"), R.id.db_port);
            putStringToEditText(extras.getString("db_location"), R.id.db_location);
            putStringToEditText(extras.getString("db_username"), R.id.db_username);
            putStringToEditText(extras.getString("db_password"), R.id.db_password);
            putStringToEditText(extras.getString("library_host"), R.id.library_host);
            putStringToEditText(extras.getString("library_port"), R.id.library_port);
            putStringToEditText(extras.getString("library_username"), R.id.library_username);
            putStringToEditText(extras.getString("library_password"), R.id.library_password);
        }

        final Button create_button = (Button) findViewById(R.id.create_library);
        create_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String library = editTextToString(R.id.libraryName);

                boolean result =
                        AddLibraryInformation.add(AddLibrary.this,is_new,
                                editTextToString(R.id.libraryName),
                                editTextToString(R.id.db_host),
                                editTextToString(R.id.db_port),
                                editTextToString(R.id.db_location),
                                editTextToString(R.id.db_username),
                                editTextToString(R.id.db_password),
                                editTextToString(R.id.library_host),
                                editTextToString(R.id.library_port),
                                editTextToString(R.id.library_username),
                                editTextToString(R.id.library_password));

                if (!result) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddLibrary.this);
                    builder.setTitle("Error");
                    builder.setMessage("Could not add library "+library);
                    builder.setPositiveButton("OK", null);
                    AlertDialog dialog = builder.show();
                } else {
                    finish();
                }
            }
        });


        final Button cancel_button = (Button) findViewById(R.id.cancel_add_library);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    finish();
            }
        });
    }

    void putStringToEditText(String string, int id) {
        EditText editText = (EditText) findViewById(id);
        editText.setText(string);
    }
}
