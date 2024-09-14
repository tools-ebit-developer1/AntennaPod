package de.danoeh.antennapod.ui.screen.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.net.download.serviceinterface.FeedUpdateManager;
import de.danoeh.antennapod.storage.preferences.UserPreferences;
import de.danoeh.antennapod.ui.preferences.screen.downloads.ChooseDataFolderDialog;

import java.io.File;


public class DownloadsPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String PREF_SCREEN_AUTODL = "prefAutoDownloadSettings";
    private static final String PREF_SCREEN_AUTO_DELETE = "prefAutoDeleteScreen";
    private static final String PREF_PROXY = "prefProxy";
    private static final String PREF_CHOOSE_DATA_DIR = "prefChooseDataDir";
    private static final String PREF_NETWORK_CONSTRAINTS_DISABLED = "prefNetworkConstraintsDisabled";
    public static final String PREF_NETWORK_CONSTRAINTS_DISABLED_TIMESPAN_MINUTES = "prefNetworkConstraintsDisabledTimespanMinutes";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_downloads);
        setupNetworkScreen();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.downloads_pref);
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDataFolderText();
    }

    private void setupNetworkScreen() {
        findPreference(PREF_SCREEN_AUTODL).setOnPreferenceClickListener(preference -> {
            ((PreferenceActivity) getActivity()).openScreen(R.xml.preferences_autodownload);
            return true;
        });
        findPreference(PREF_SCREEN_AUTO_DELETE).setOnPreferenceClickListener(preference -> {
            ((PreferenceActivity) getActivity()).openScreen(R.xml.preferences_auto_deletion);
            return true;
        });
        // validate and set correct value: number of downloads between 1 and 50 (inclusive)
        findPreference(PREF_PROXY).setOnPreferenceClickListener(preference -> {
            ProxyDialog dialog = new ProxyDialog(getActivity());
            dialog.show();
            return true;
        });
        findPreference(PREF_CHOOSE_DATA_DIR).setOnPreferenceClickListener(preference -> {
            ChooseDataFolderDialog.showDialog(getContext(), path -> {
                UserPreferences.setDataFolder(path);
                setDataFolderText();
            });
            return true;
        });
        findPreference(PREF_NETWORK_CONSTRAINTS_DISABLED).setOnPreferenceClickListener(preference -> {
            boolean value = UserPreferences.getPrefNetworkConstraintsDisabled();
            UserPreferences.setPrefNetworkConstraintsDisabled(value);
            Preference p =  findPreference(PREF_NETWORK_CONSTRAINTS_DISABLED_TIMESPAN_MINUTES);
            p.setEnabled(value);
            p.setShouldDisableView(!value);
            return true;
        });
    }

    private void setDataFolderText() {
        File f = UserPreferences.getDataFolder(null);
        if (f != null) {
            findPreference(PREF_CHOOSE_DATA_DIR).setSummary(f.getAbsolutePath());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (UserPreferences.PREF_UPDATE_INTERVAL.equals(key)) {
            FeedUpdateManager.getInstance().restartUpdateAlarm(getContext(), true);
        }
    }
}
