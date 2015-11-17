package fred.docapp;

import android.app.IntentService;
import android.content.Intent;

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
        for (String file : ftr.files)
             scp.transfer(this,ftr.userName,ftr.passWord,ftr.host,file);
    }
}
