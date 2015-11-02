package fred.docapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Stack;

/**
 * Created by fred on 2/11/15.
 */
public class SpinnerAdapter extends ArrayAdapter<DirView> {
    Context context;
    int layoutResourceId;
    DirView[] dirs = null;

    public SpinnerAdapter(Context context, int layoutResourceId, Stack<DirView> stack) {
        super(context, layoutResourceId, stack);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.dirs = stack.toArray(new DirView[stack.size()]);
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
            holder.txtTitle = (TextView)row.findViewById(R.id.listText);

            row.setTag(holder);
        }
        else
        {
            holder = (SpinnerHolder)row.getTag();
        }

        DirView dirView = dirs[position];
        holder.txtTitle.setText(dirView.dirName);
        return row;
    }

    static class SpinnerHolder
    {
        TextView txtTitle;
    }
}
