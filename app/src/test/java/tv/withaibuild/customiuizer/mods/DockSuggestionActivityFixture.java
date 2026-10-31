package tv.withaibuild.customiuizer.mods;

import java.lang.ref.WeakReference;

/** Exact two-field Java ROM worker shape; no Compose compiler inference metadata. */
public final class DockSuggestionActivityFixture {
    public static final class MatchingWorker implements Runnable {
        public final WeakReference<DockSuggestionActivityFixture> owner;
        public final boolean loading = false;
        public MatchingWorker(DockSuggestionActivityFixture owner) { this.owner = new WeakReference<>(owner); }
        @Override public void run() { }
    }
    public static final class WrongWorker implements Runnable {
        public final DockSuggestionActivityFixture owner;
        public WrongWorker(DockSuggestionActivityFixture owner) { this.owner = owner; }
        @Override public void run() { }
    }
}
