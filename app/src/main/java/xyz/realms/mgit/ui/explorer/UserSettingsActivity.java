package xyz.realms.mgit.ui.explorer;

import android.os.Bundle;

import xyz.realms.mgit.ui.fragments.SettingsFragment;
import xyz.realms.mgit.ui.fragments.SheimiFragmentActivity;

/**
 * Activity for user settings
 */
public class UserSettingsActivity extends SheimiFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new SettingsFragment())
            .commit();
    }
}
