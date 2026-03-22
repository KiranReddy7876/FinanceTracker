package com.financetracker.ui.sync;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;
import com.financetracker.R;

public class SyncFragment extends Fragment {

    private static final int RC_SIGN_IN = 9001;
    private SyncViewModel viewModel;
    private GoogleSignInClient signInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sync, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SyncViewModel.class);

        TextView tvStatus = view.findViewById(R.id.tv_sync_status);
        TextView tvLastSync = view.findViewById(R.id.tv_last_sync);
        Button btnSync = view.findViewById(R.id.btn_sync_now);
        ProgressBar progressBar = view.findViewById(R.id.progress_sync);
        SignInButton btnSignIn = view.findViewById(R.id.btn_google_sign_in);

        tvLastSync.setText("Last synced: " + viewModel.getLastSyncTime());

        viewModel.getSyncStatus().observe(getViewLifecycleOwner(), status -> tvStatus.setText(status));
        viewModel.getIsSyncing().observe(getViewLifecycleOwner(), syncing -> {
            progressBar.setVisibility(syncing ? View.VISIBLE : View.GONE);
            btnSync.setEnabled(!syncing);
        });

        btnSync.setOnClickListener(v -> viewModel.triggerManualSync());

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
            .build();
        signInClient = GoogleSignIn.getClient(requireActivity(), gso);

        btnSignIn.setOnClickListener(v -> startActivityForResult(signInClient.getSignInIntent(), RC_SIGN_IN));

        // Check existing sign-in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            viewModel.saveGoogleAccount(account.getEmail());
            btnSignIn.setVisibility(View.GONE);
            tvStatus.setText("Signed in as " + account.getEmail());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(account -> {
                    viewModel.saveGoogleAccount(account.getEmail());
                    Toast.makeText(requireContext(), "Signed in as " + account.getEmail(), Toast.LENGTH_SHORT).show();
                    requireView().findViewById(R.id.btn_google_sign_in).setVisibility(View.GONE);
                })
                .addOnFailureListener(e ->
                    Toast.makeText(requireContext(), "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}
