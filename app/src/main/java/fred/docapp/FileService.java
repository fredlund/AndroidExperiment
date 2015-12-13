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
        Transfer transfer = null;
        boolean savedPassword = false;

        System.out.println("handle intent");
        FileTransferRequest ftr = intent.getParcelableExtra("fred.docapp.FileTransferRequest");
        System.out.println("got a file transfer request "+ftr);
        TransferDB db = TransferDB.getInstance(this);
        ScpFromJava scp = new ScpFromJava(this);
        boolean failure = false;
        for (int i=0; i<ftr.files.length && !failure; i++) {
            failure = false;
            String file = ftr.files[i];
            transfer = new Transfer(file,ftr.library,Transfer.connecting(),0,0);
            Intent statusIntent = new Intent("file_transfer");
            statusIntent.putExtra("file", file);
            statusIntent.putExtra("status", transfer.transferStatus);
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
            db.storeTransfer(transfer);
            ScpReturnStatus ret = scp.setupTransfer(ftr.userName, ftr.passWord, ftr.host, ftr.port, file);
            System.out.println("ftr "+ftr+" connect returns "+ret);
            failure = !ret.is_ok;
            if (!failure) {
                if (!savedPassword) {
                    UserHost uh = new UserHost(ftr.userName, ftr.host);
                    UserInfo.getInstance().savePassword(uh,ftr.passWord);
                }
                transfer = new Transfer(file,ftr.library,Transfer.progressing(),scp.fileSize,0);
                statusIntent = new Intent("file_transfer");
                statusIntent.putExtra("file", file);
                statusIntent.putExtra("status", transfer.transferStatus);
                LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
                db.updateTransfer(transfer);
                ret = scp.doTransfer(ftr.localDir);
                System.out.println("ftr "+ftr+" transmit returns "+ret);
                failure = !ret.is_ok;
                if (!failure)
                    transfer.transferStatus = Transfer.finished();
                else
                    transfer.transferStatus = Transfer.failed();
            } else
                transfer = new Transfer(file,ftr.library,Transfer.failedConnect(),0,0);
            db.updateTransfer(transfer);
            statusIntent = new Intent("file_transfer");
            statusIntent.putExtra("file", ftr.files[i]);
            statusIntent.putExtra("status", transfer.transferStatus);
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
        }
    }
}
