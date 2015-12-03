package fred.docapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by fred on 27/11/15.
 */
public class TransferAdapter extends ArrayAdapter<Transfer> {

    Context context;
    int layoutResourceId;
    List<Transfer> data = null;

    TransferAdapter(Context context, int layoutResourceId, List<Transfer> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        System.out.println("in getView: position=" + position);
        System.out.flush();
        View row = convertView;
        TransferHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new TransferHolder();
            holder.file = (TextView) row.findViewById(R.id.transferFileText);
            holder.library = (TextView) row.findViewById(R.id.transferLibrary);
            holder.status = (TextView) row.findViewById(R.id.transferStatus);

            row.setTag(holder);
        } else {
            holder = (TransferHolder) row.getTag();
        }

        Transfer transfer = data.get(position);
        holder.file.setText((new File(transfer.file)).getName());
        holder.library.setText(transfer.library);
        holder.status.setText(transfer.transferStatusToString());
        return row;
    }

    String size_to_string(long size) {
        if (size < 1024)
            return size + " bytes";
        else if (size < 1024 * 1024) {
            long KB = size / 1024;
            return KB + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            long MB = size / (1024 * 1024);
            return MB + " MB";
        } else {
            long GB = size / (1024 * 1024 * 1024);
            return GB + " GB";
        }
    }

    static class TransferHolder {
        TextView file;
        TextView library;
        TextView status;
    }
}

