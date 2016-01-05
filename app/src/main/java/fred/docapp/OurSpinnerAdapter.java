package fred.docapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by fred on 2/11/15.
 *
 */
public class OurSpinnerAdapter extends ArrayAdapter<DirView> implements SpinnerAdapter {//ArrayAdapter<DirView> {
    Context context;
    ArrayList<DirView> dirs = null;

    public OurSpinnerAdapter(Context context, ArrayList<DirView> stack) {
        super(context, R.layout.spinner_item, stack);
        this.context = context;
        this.dirs = stack;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SpinnerHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.spinner_item, parent, false);

            holder = new SpinnerHolder();
            //holder.txtTitle = (TextView)row.findViewById(R.id.spinnerText);
            //holder.txtTitle = (TextView)row.findViewById(android.R.id.text1);
            holder.txtTitle = (TextView)row.findViewById(R.id.spinnerTxt);

            row.setTag(holder);
        }
        else
        {
            holder = (SpinnerHolder)row.getTag();
        }

        DirView dirView = dirs.get(position);
        File f = new File(dirView.dirName);
        String str = f.getName();
        str = str.substring(0,Math.min(str.length(),20));

        holder.txtTitle.setText(str);
        System.out.println("SpinnerAdapter.getView(" + position + "); dirName="+str);
        return row;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SpinnerHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.spinner_item, parent, false);

            holder = new SpinnerHolder();
            //holder.txtTitle = (TextView)row.findViewById(R.id.spinnerText);
            //holder.txtTitle = (TextView)row.findViewById(android.R.id.text1);
            holder.txtTitle = (TextView)row.findViewById(R.id.spinnerTxt);

            row.setTag(holder);
        }
        else
        {
            holder = (SpinnerHolder)row.getTag();
        }

        DirView dirView = dirs.get(position);
        File f = new File(dirView.dirName);
        String str = f.getName();
        str = str.substring(0,Math.min(str.length(),20));

        holder.txtTitle.setText(str);
        System.out.println("SpinnerAdapter.getView(" + position + "); dirName="+str);
        return row;
    }

    static class SpinnerHolder
    {
        TextView txtTitle;
    }
}
