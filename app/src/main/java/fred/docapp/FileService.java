package fred.docapp;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.net.URLConnection;

public class FileService extends IntentService {
    public FileService() {
        super("hello");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder nb = new Notification.Builder(this);

    if (ftr.files.length>0) {
        nb.setSmallIcon(android.R.drawable.stat_sys_download);
        nb.setTicker("Billy file transfers");
        nb.setContentTitle("See file transfers");
        nb.setContentText("Click to see file transfers");
          PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                  new Intent(this, TransferStatusActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        nb.setContentIntent(contentIntent);
        nm.notify(0, nb.build());
    }
        for (int i=0; i<ftr.files.length && !failure; i++) {
            String file = ftr.files[i];
            transfer = new Transfer(file,ftr.library,Transfer.connecting(),0,0);
            db.storeTransfer(transfer);
            Intent statusIntent = new Intent("file_transfer");
            statusIntent.putExtra("file", file);
            statusIntent.putExtra("library", ftr.library);
            statusIntent.putExtra("status", transfer.transferStatus);
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
            ScpReturnStatus ret = scp.setupTransfer(ftr.userName, ftr.passWord, ftr.host, ftr.port, file);
            System.out.println("ftr "+ftr+" connect returns "+ret);
            failure = !ret.is_ok;
            if (!failure) {
                if (!savedPassword) {
                    UserHost uh = new UserHost(ftr.userName, ftr.host);
                    UserInfo.getInstance().savePassword(uh,ftr.passWord);
                }
                transfer = new Transfer(file,ftr.library,Transfer.progressing(),scp.fileSize,0);
                db.updateTransfer(transfer);
                statusIntent = new Intent("file_transfer");
                statusIntent.putExtra("file", file);
                statusIntent.putExtra("library", ftr.library);
                statusIntent.putExtra("status", transfer.transferStatus);
                LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
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
            statusIntent.putExtra("library", ftr.library);
            statusIntent.putExtra("status", transfer.transferStatus);
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
            if (transfer.transferStatus == TransferStatusActivity.finished && ftr.tryOpen) {
                File myFile = new File(ftr.localDir + "/" + (new File(file)).getName());
                Uri uri = Uri.fromFile(myFile);
                String url = "file:"+myFile.toString();
                String mimeType = URLConnection.guessContentTypeFromName(url);
                System.out.println("the mimeType for "+url+" is "+mimeType+" uri is "+uri);
                if (mimeType == null)
                    mimeType = "*/*";
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                newIntent.setDataAndType(uri,mimeType);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    this.startActivity(newIntent);
                } catch (ActivityNotFoundException e) {
                     System.out.println("No handler for "+mimeType);
                }
            }
        }
        if (ftr.files.length > 0) nm.cancel(0);
    }
}
