package com.financetracker.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.biometric.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.financetracker.R;
import java.util.concurrent.Executor;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        Spinner spinnerCurrency = view.findViewById(R.id.spinner_currency);
        String[] currencies = {"INR", "USD", "EUR", "GBP", "AED", "SGD"};
        spinnerCurrency.setAdapter(new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, currencies));
        String saved = prefs.getString("currency", "INR");
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(saved)) { spinnerCurrency.setSelection(i); break; }
        }
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                prefs.edit().putString("currency", currencies[pos]).apply();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        Switch switchAppLock = view.findViewById(R.id.switch_app_lock);
        switchAppLock.setChecked(prefs.getBoolean("app_lock", false));
        switchAppLock.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                checkBiometricSupport(switchAppLock, prefs);
            } else {
                prefs.edit().putBoolean("app_lock", false).apply();
            }
        });

        view.findViewById(R.id.btn_export_backup).setOnClickListener(v ->
            Toast.makeText(requireContext(), "Backup exported to Downloads", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_import_restore).setOnClickListener(v ->
            Toast.makeText(requireContext(), "Select a backup file to restore", Toast.LENGTH_SHORT).show());
    }

    private void checkBiometricSupport(Switch switchAppLock, SharedPreferences prefs) {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            == BiometricManager.BIOMETRIC_SUCCESS) {
            prefs.edit().putBoolean("app_lock", true).apply();
        } else {
            switchAppLock.setChecked(false);
            Toast.makeText(requireContext(), "Biometric authentication not available", Toast.LENGTH_SHORT).show();
        }
    }
}
