package fred.docapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/**
 * Created by fred on 30/11/15.
 */
public class TransferDB extends SQLiteOpenHelper {

    private static TransferDB mInstance = null;
    private Context mCtx;

    public TransferDB(Context ctx) {
        super(ctx, "billyDB", null, 1);
        this.mCtx = ctx;
    }

    public static TransferDB getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new TransferDB(ctx.getApplicationContext());
        }
        return mInstance;
    }
    private static final String CREATE_ENTRIES =
            "CREATE TABLE transfers ( file TEXT PRIMARY KEY, library TEXT, status INTEGER, transferred INTEGER )";
     private static final String DELETE_ENTRIES =
             "DROP TABLE IF EXISTS transfers";

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void storeTransfer(Transfer transfer) {
        ContentValues values = new ContentValues();
        values.put("file", transfer.file);
        values.put("library", transfer.library);
        values.put("status", transfer.ts);
        values.put("transferred", transfer.transferred);
        (mInstance.getWritableDatabase()).insert("transfers", "null", values);
    }

    public List<Transfer> getAll() {
        List<Transfer> todos = new ArrayList<Todo>();
        String selectQuery = "SELECT  * FROM " + TABLE_TODO;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Transfer td = new Transfer();
                td.file = c.getString(1);
                td.
                td.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));

                // adding to todo list
                todos.add(td);
            } while (c.moveToNext());
        }

        return todos;
    }
}
