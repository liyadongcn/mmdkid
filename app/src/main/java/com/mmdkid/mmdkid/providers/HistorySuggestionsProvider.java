package com.mmdkid.mmdkid.providers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by LIYADONG on 2017/6/18.
 */

public class HistorySuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.mmdkid.mmdkid.providers.HistorySuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public HistorySuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
