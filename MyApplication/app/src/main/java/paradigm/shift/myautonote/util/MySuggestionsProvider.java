package paradigm.shift.myautonote.util;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Suggestions provider for our search.
 * Created by aravind on 11/29/17.
 */

public class MySuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "paradigm.shift.myautonote.util.MySuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
