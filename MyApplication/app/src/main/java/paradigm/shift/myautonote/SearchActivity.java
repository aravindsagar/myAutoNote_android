package paradigm.shift.myautonote;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import paradigm.shift.myautonote.adapter.SearchListAdapter;
import paradigm.shift.myautonote.data_model.SearchResult;
import paradigm.shift.myautonote.util.MySuggestionsProvider;
import paradigm.shift.myautonote.util.Searcher;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private SearchListAdapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Get the intent, verify the action and get the query
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            getSupportActionBar().setTitle("Results for \"" + query + "\"");

            // Save this query to enable search suggestions.
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionsProvider.AUTHORITY, MySuggestionsProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            List<SearchResult> results = Searcher.getSearchResults(query, this);
            displayResults(query, results);
        }
    }

    private void displayResults(String query, List<SearchResult> results) {
        ListView searchResults = findViewById(R.id.list_search_results);
        TextView noResults = findViewById(R.id.text_view_no_results);

        if (results.size() <= 0) {
            noResults.setVisibility(View.VISIBLE);
            searchResults.setVisibility(View.GONE);
        } else {
            noResults.setVisibility(View.GONE);
            searchResults.setVisibility(View.VISIBLE);
        }

        myAdapter = new SearchListAdapter(results, this, query);
        searchResults.setAdapter(myAdapter);
        searchResults.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        myAdapter.itemClick(position);
    }
}
