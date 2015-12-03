package fred.docapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fred on 8/11/15.
 */
public class UserInfo {
    Map<UserHost,UserData> userMap = null;
    String password;
    static UserInfo mInstance = null;

    public static UserInfo getInstance() {
        if (mInstance==null) {
            mInstance = new UserInfo();
        }
        return mInstance;
    }

    public UserInfo() {
        userMap = new HashMap<UserHost,UserData>();
    }

    public void getPassword(Context context, final UserHost uh, final DialogListener dl) {
        System.out.println("username check for "+uh+" usermap is "+userMap+" uh is "+uh);
        System.out.flush();
        if (userMap.containsKey(uh)) {
            dl.result(userMap.get(uh).password);
        } else {
            System.out.println("nope");
            System.out.flush();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            System.out.println("have builder"); System.out.flush();
            builder.setTitle("Password dialog");
            builder.setMessage("Password for " + uh);
            final EditText input = new EditText(context);
           input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            builder.setView(input);
            System.out.println("setView"); System.out.flush();



                    builder.setNegativeButton("ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                    password = input.getText().toString();
                                    System.out.println("user_text is "+password);
                                    dl.result(password);
                                    userMap.put(uh, new UserData(uh, password));
                                }
                            })
                    .setPositiveButton("cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dl.result(null);
                                }
                            });

            // create alert dialog
            //AlertDialog alertDialog = alertDialogBuilder.create();

                System.out.println("setbuttons done"); System.out.flush();
            // show it
            builder.show();
            System.out.println("show");
            System.out.flush();
        }
    }
}
