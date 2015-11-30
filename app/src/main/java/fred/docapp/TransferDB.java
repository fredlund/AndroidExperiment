package fred.docapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    "CREATE TABLE transfers ( file TEXT PRIMARY KEY, library TEXT, status TEXT, transferred INTEGER )";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS transfers");

        // Create tables again
        onCreate(db);
    }
}
