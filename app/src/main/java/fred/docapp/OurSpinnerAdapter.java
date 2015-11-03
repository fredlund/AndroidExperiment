package fred.docapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by fred on 2/11/15.
 */
public class OurSpinnerAdapter extends ArrayAdapter<DirView> implements SpinnerAdapter {//ArrayAdapter<DirView> {
    Context context;
    int layoutResourceId;
    ArrayList<DirView> dirs = null;

    public OurSpinnerAdapter(Context context, int layoutResourceId, ArrayList<DirView> stack) {
        super(context, layoutResourceId, stack);
        this.layoutResourceId = layoutResourceId;
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
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SpinnerHolder();
            //holder.txtTitle = (TextView)row.findViewById(R.id.spinnerText);
            holder.txtTitle = (TextView)row.findViewById(android.R.id.text1);

            row.setTag(holder);
        }
        else
        {
            holder = (SpinnerHolder)row.getTag();
        }

        DirView dirView = dirs.get(dirs.size()-(position+1));
        holder.txtTitle.setText(dirView.dirName);
        System.out.println("SpinnerAdapter.getView(" + position + "); dirName="+dirView.dirName);
        return row;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SpinnerHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SpinnerHolder();
            //holder.txtTitle = (TextView)row.findViewById(R.id.spinnerText);
            holder.txtTitle = (TextView)row.findViewById(android.R.id.text1);

            row.setTag(holder);
        }
        else
        {
            holder = (SpinnerHolder)row.getTag();
        }

        DirView dirView = dirs.get(dirs.size()-(position+1));
        holder.txtTitle.setText(dirView.dirName);
        System.out.println("SpinnerAdapter.getView(" + position + "); dirName="+dirView.dirName);
        return row;
    }

    static class SpinnerHolder
    {
        TextView txtTitle;
    }
}
