package paradigm.shift.myautonote.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import paradigm.shift.myautonote.R;
import paradigm.shift.myautonote.WorkActivity;
import paradigm.shift.myautonote.data_model.DataItem;
import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.File;
import paradigm.shift.myautonote.data_model.SearchResult;

/**
 * Created by aravind on 11/29/17.
 */

public class SearchListAdapter extends BaseAdapter {

    private static class ViewHolder {

        ImageView imageView;
        TextView pathView, matchView;

        public ViewHolder(ImageView imageView, TextView pathView, TextView matchView) {
            this.imageView = imageView;
            this.pathView = pathView;
            this.matchView = matchView;
        }
    }

    private List<SearchResult> myResults;
    private Context myContext;
    private LayoutInflater myInflater;
    private String myQuery;

    public SearchListAdapter(final List<SearchResult> results, final Context context, final String query) {
        myInflater = LayoutInflater.from(context);
        myContext = context;
        myResults = results;
        myQuery = query.toLowerCase();
    }

    @Override
    public int getCount() {
        return myResults.size();
    }

    @Override
    public Object getItem(int position) {
        return myResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Create or reuse the layout objects as required.
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.list_item_search_result, parent, false);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (holder == null) {
            holder = new ViewHolder((ImageView) convertView.findViewById(R.id.img_search_list),
                    (TextView) convertView.findViewById(R.id.path_view_search_list),
                    (TextView) convertView.findViewById(R.id.match_view_search_list));
        }

        SearchResult result = (SearchResult) getItem(position);

        StringBuilder pathTextBldr = new StringBuilder(result.getItemPath().size()*2);
        for (DataItem d : result.getItemPath()) {
            pathTextBldr.append(d.getName());
            pathTextBldr.append('/');
        }
        String pathText = pathTextBldr.toString();
        pathText = highlightText(pathText, myQuery);

        holder.pathView.setText(Html.fromHtml(pathText));
        if (result.getMatchingText() != null) {
            String resultText = "..." + result.getMatchingText() + "...";
            resultText = highlightText(resultText, myQuery);
            holder.matchView.setText(Html.fromHtml(resultText));
            holder.matchView.setVisibility(View.VISIBLE);
        } else {
            holder.matchView.setVisibility(View.GONE);
        }

        if (result.isResultDir()) {
            holder.imageView.setImageResource(R.drawable.ic_folder);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_file);
        }

        return convertView;
    }

    public void itemClick(int position) {
        SearchResult result = (SearchResult) getItem(position);
        DataItem item = result.getItemPath().get(result.getItemPath().size() - 1);
        String[] curPath = new String[result.getItemPath().size() - 1];
        for (int i = 0; i < result.getItemPath().size() - 1; i++) {
            curPath[i] = result.getItemPath().get(i).getName();
        }
        if (result.isResultDir()) {
            // TODO
        } else {
            File f = (File) item;
            myContext.startActivity(new Intent(myContext, WorkActivity.class)
                    .putExtra(WorkActivity.NOTE_TITLE, f.getName())
                    .putExtra(WorkActivity.NOTE_CONTENT, f.getFileContents())
                    .putExtra(WorkActivity.CUR_DIR, curPath));
        }
    }

    private String highlightText(String s, String toHighlight) {
        String sl = s.toLowerCase();
        int sIdx = sl.indexOf(toHighlight);
        if (sIdx == -1) {
            return s;
        }
        return s.substring(0, sIdx) + "<font color='red'>" + s.substring(sIdx, sIdx + toHighlight.length())
                + "</font>" + highlightText(s.substring(sIdx + toHighlight.length()), toHighlight);
    }
}