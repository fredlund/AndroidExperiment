package fred.docapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fred on 30/11/15.
 *
 */
public class TransferDB extends SQLiteOpenHelper {

    private static TransferDB mInstance = null;

    public TransferDB(Context ctx) {
        super(ctx, "billyDB", null, 2);
        Context mCtx = ctx;
    }

    public static TransferDB getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new TransferDB(ctx.getApplicationContext());
        }
        return mInstance;
    }
    private static final String CREATE_ENTRIES =
            "CREATE TABLE transfers ( file TEXT PRIMARY KEY, library TEXT, status INTEGER, fileSize INTEGER, transferred INTEGER )";
     private static final String DELETE_ENTRIES =
             "DROP TABLE IF EXISTS transfers";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ENTRIES);
    }

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
        values.put("status", transfer.transferStatus);
        values.put("fileSize",transfer.fileSize);
        values.put("transferred", transfer.transferred);
        (mInstance.getWritableDatabase()).insertWithOnConflict("transfers", "null", values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void clearDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DELETE_ENTRIES);
        db.execSQL(CREATE_ENTRIES);
    }
    public void updateTransfer(Transfer transfer) {
        ContentValues values = new ContentValues();
        values.put("status", transfer.transferStatus);
        values.put("transferred", transfer.transferred);
        values.put("fileSize",transfer.fileSize);
        System.out.println("sql quotes is " + DatabaseUtils.sqlEscapeString(transfer.file));
        (mInstance.getWritableDatabase()).update("transfers", values, "file = " + DatabaseUtils.sqlEscapeString(transfer.file), null);
    }

    public boolean deleteTransfer(Transfer transfer)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("transfers", "file" + "=" + DatabaseUtils.sqlEscapeString(transfer.file), null) > 0;
    }

    public List<Transfer> getAll() {
        List<Transfer> transfers = new ArrayList<Transfer>();
        String selectQuery = "SELECT  * FROM " + "transfers";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Transfer td = new Transfer(c.getString(0), c.getString(1),
                                            c.getInt(2), c.getLong(3), c.getLong(4));
                transfers.add(td);
            } while (c.moveToNext());
        }
        return transfers;
    }
}
