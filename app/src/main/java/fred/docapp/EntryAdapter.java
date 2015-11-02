package fred.docapp;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fred on 30/10/15.
 */
public class EntryAdapter extends ArrayAdapter<Entry> {

    Context context;
    int layoutResourceId;
    Entry data[] = null;

    public EntryAdapter(Context context, int layoutResourceId, Entry[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        EntryHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new EntryHolder();
            holder.image = (ImageView)row.findViewById(R.id.imageView);
            holder.txtTitle = (TextView)row.findViewById(R.id.listText);

            row.setTag(holder);
        }
        else
        {
            holder = (EntryHolder)row.getTag();
        }

        Entry entry = data[position];
        String value;
        if (entry.isDir)
            value = entry.fileName + "/";
        else
            value = entry.fileName + " ("+size_to_string(entry.size)+")";
        holder.txtTitle.setText(value);
        if (entry.isEnabled)
            holder.image.setImageResource(R.drawable.ic_done_white);
        return row;
    }

    String size_to_string(long size) {
        if (size < 1024)
            return size+" bytes";
        else if (size < 1024*1024) {
            long KB = size / 1024;
            return KB+" KB";
        } else if (size < 1024*1024*1024) {
            long MB = size / (1024 * 1024);
            return MB + " MB";
        } else if (size < 1024*1024*1024*1024) {
            long GB= size / (1024 * 1024 * 1024);
            return GB + " MB";
        } else return "very big";
    }

    static class EntryHolder
    {
        ImageView image;
        TextView txtTitle;
    }
}
