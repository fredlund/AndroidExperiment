package fred.docapp;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;

public class FileService extends IntentService {
    public FileService() {
        super("hello");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("handle intent");
        FileTransferRequest ftr = intent.getParcelableExtra("fred.docapp.FileTransferRequest");
        System.out.println("got a file transfer request "+ftr);
        ScpFromJava scp = new ScpFromJava();
        boolean failure = false;
        for (int i=0; i<ftr.files.length && !failure; i++) {
            String file = ftr.files[i];
            ScpReturnStatus ret = scp.transfer(this, ftr.userName, ftr.passWord, ftr.host, file, ftr.localDir);
            failure = ret.is_ok;
            Intent statusIntent = new Intent("file_transfer");
            statusIntent.putExtra("requestNo",ftr.requestNo);
            statusIntent.putExtra("file",ftr.files[i]);
            statusIntent.putExtra("status",!failure);
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
        }
        Intent statusIntent = new Intent("request_transfer");
        statusIntent.putExtra("requestNo",ftr.requestNo);
        statusIntent.putExtra("status",!failure);
        System.out.println("status = "+!failure);
        LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
    }
}
