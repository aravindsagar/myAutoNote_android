package paradigm.shift.myautonote.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import paradigm.shift.myautonote.R;
import paradigm.shift.myautonote.data_model.Directory;

/**
 * Created by aravind on 11/21/17.
 */

public class CurPathAdapter extends RecyclerView.Adapter<CurPathAdapter.ViewHolder> {
    private List<Directory> myDataset;
    private CurPathItemClickListener myListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        ViewHolder(TextView v) {
            super(v);
            myTextView = v;
        }
    }

    public CurPathAdapter(final List<Directory> dataset,
                          final CurPathItemClickListener listener) {
        myDataset = dataset;
        myListener = listener;
    }

    public void setDataset(final List<Directory> dataset) {
        this.myDataset = dataset;
        notifyDataSetChanged();
    }

    @Override
    public CurPathAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.cur_path_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.myTextView.setText(myDataset.get(position).getName());
        if (myListener != null) {
            holder.myTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myListener.onItemClick(myDataset.subList(0, holder.getAdapterPosition()+1));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return myDataset.size();
    }
}


