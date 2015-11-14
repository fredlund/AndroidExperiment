package fred.docapp;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddLibrary extends AppCompatActivity {
    boolean is_new = true;
    String libraryName;

    String editTextToString(int id)  {
        EditText editText = (EditText) findViewById(R.id.libraryName);
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
    libraryName = extras.getString("libraryName");
   is_new = false;
}
        if (!is_new) {
            EditText libraryNameView = (EditText) findViewById(R.id.libraryName);
            libraryNameView.setText(libraryName);
            newItent.putExtra("db_location", editTextToString
                    (R.id
                            .db_location));
            editor.putString("db_username", editTextToString(R.id.db_username));
            editor.putString("db_password", editTextToString(R.id.db_password));
            editor.putString("library_password", editTextToString(R.id.library_password));
            editor.putString("library_username", editTextToString(R.id.library_username));
        }


        final Button create_button = (Button) findViewById(R.id.create_library);
        create_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String library = editTextToString(R.id.libraryName);
                SharedPreferences libraryPreferences = getSharedPreferences(library, 0);
                if (libraryPreferences.contains("is_created")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddLibrary.this);
                    builder.setTitle("Error");
                    builder.setMessage("Library " + library + " already exists");
                    builder.setPositiveButton("OK", null);
                    AlertDialog dialog = builder.show();
                } else {
                    SharedPreferences.Editor editor = libraryPreferences.edit();
                    editor.putBoolean("is_created", true);
                    editor.putString("db_location", editTextToString(R.id.db_location));
                    editor.putString("db_username", editTextToString(R.id.db_username));
                    editor.putString("db_password", editTextToString(R.id.db_password));
                    editor.putString("library_password", editTextToString(R.id.library_password));
                    editor.putString("library_username", editTextToString(R.id.library_username));
                    editor.commit();
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
}
