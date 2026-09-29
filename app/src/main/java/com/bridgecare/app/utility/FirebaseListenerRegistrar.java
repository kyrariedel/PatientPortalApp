package com.bridgecare.app.utility;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks Firebase listeners so fragments can detach them in onStop/onDestroyView.
 */
public class FirebaseListenerRegistrar {
    private final List<TrackedListener> trackedListeners = new ArrayList<>();

    public void add(Query query, ValueEventListener listener) {
        query.addValueEventListener(listener);
        trackedListeners.add(new TrackedListener(query, listener));
    }

    public void removeAll() {
        for (TrackedListener tracked : trackedListeners) {
            tracked.query.removeEventListener(tracked.listener);
        }
        trackedListeners.clear();
    }

    private static class TrackedListener {
        final Query query;
        final ValueEventListener listener;

        TrackedListener(Query query, ValueEventListener listener) {
            this.query = query;
            this.listener = listener;
        }
    }
}
