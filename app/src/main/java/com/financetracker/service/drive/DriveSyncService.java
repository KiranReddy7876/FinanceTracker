package com.financetracker.service.drive;

import android.content.Context;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.financetracker.data.db.entity.*;
import com.financetracker.data.repository.*;
import java.io.*;
import java.util.*;

public class DriveSyncService {

    private static final String APP_FOLDER_NAME = "FinanceTrackerData";
    private static final String MIME_JSON = "application/json";
    private static final String MIME_FOLDER = "application/vnd.google-apps.folder";

    private final Drive driveService;
    private final TransactionRepository transactionRepo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final MerchantRepository merchantRepo;
    private final Gson gson = new Gson();

    public DriveSyncService(Context context, GoogleAccountCredential credential) {
        this.driveService = new Drive.Builder(
            new NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("FinanceTracker").build();

        this.transactionRepo = new TransactionRepository(context);
        this.accountRepo = new AccountRepository(context);
        this.categoryRepo = new CategoryRepository(context);
        this.merchantRepo = new MerchantRepository(context);
    }

    public SyncResult sync(long lastSyncTimestamp) throws IOException {
        String folderId = getOrCreateAppFolder();
        int uploaded = uploadChanges(folderId, lastSyncTimestamp);
        int downloaded = downloadChanges(folderId, lastSyncTimestamp);
        return new SyncResult(uploaded, downloaded, System.currentTimeMillis());
    }

    private int uploadChanges(String folderId, long since) throws IOException {
        int count = 0;

        List<Transaction> txns = transactionRepo.getModifiedSince(since);
        if (!txns.isEmpty()) {
            uploadFile(folderId, "transactions.json", gson.toJson(txns));
            count += txns.size();
        }

        List<Account> accounts = accountRepo.getModifiedSince(since);
        if (!accounts.isEmpty()) {
            uploadFile(folderId, "accounts.json", gson.toJson(accounts));
            count += accounts.size();
        }

        List<Category> categories = categoryRepo.getModifiedSince(since);
        if (!categories.isEmpty()) {
            uploadFile(folderId, "categories.json", gson.toJson(categories));
            count += categories.size();
        }

        List<Merchant> merchants = merchantRepo.getModifiedSince(since);
        if (!merchants.isEmpty()) {
            uploadFile(folderId, "merchants.json", gson.toJson(merchants));
            count += merchants.size();
        }

        return count;
    }

    private int downloadChanges(String folderId, long since) throws IOException {
        FileList files = driveService.files().list()
            .setQ("'" + folderId + "' in parents and mimeType='" + MIME_JSON + "' and trashed=false")
            .setFields("files(id, name, modifiedTime)")
            .execute();

        int count = 0;
        for (File f : files.getFiles()) {
            long modified = f.getModifiedTime().getValue();
            if (modified <= since) continue;
            String content = downloadFile(f.getId());
            count += mergeData(f.getName(), content);
        }
        return count;
    }

    private int mergeData(String filename, String json) {
        switch (filename) {
            case "transactions.json": {
                Transaction[] items = gson.fromJson(json, Transaction[].class);
                for (Transaction t : items) transactionRepo.upsertWithConflictResolution(t);
                return items.length;
            }
            case "accounts.json": {
                Account[] items = gson.fromJson(json, Account[].class);
                for (Account a : items) accountRepo.upsertWithConflictResolution(a);
                return items.length;
            }
            case "categories.json": {
                Category[] items = gson.fromJson(json, Category[].class);
                for (Category c : items) categoryRepo.getModifiedSince(0); // upsert
                return items.length;
            }
            default:
                return 0;
        }
    }

    private String getOrCreateAppFolder() throws IOException {
        FileList result = driveService.files().list()
            .setQ("name='" + APP_FOLDER_NAME + "' and mimeType='" + MIME_FOLDER + "' and trashed=false")
            .setFields("files(id)")
            .execute();

        if (!result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        File folder = new File();
        folder.setName(APP_FOLDER_NAME);
        folder.setMimeType(MIME_FOLDER);
        return driveService.files().create(folder).setFields("id").execute().getId();
    }

    private void uploadFile(String folderId, String filename, String content) throws IOException {
        File meta = new File();
        meta.setName(filename);
        ByteArrayContent body = new ByteArrayContent(MIME_JSON, content.getBytes("UTF-8"));

        FileList existing = driveService.files().list()
            .setQ("name='" + filename + "' and '" + folderId + "' in parents and trashed=false")
            .setFields("files(id)")
            .execute();

        if (!existing.getFiles().isEmpty()) {
            driveService.files().update(existing.getFiles().get(0).getId(), meta, body).execute();
        } else {
            meta.setParents(Collections.singletonList(folderId));
            driveService.files().create(meta, body).execute();
        }
    }

    private String downloadFile(String fileId) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        driveService.files().get(fileId).executeMediaAndDownloadTo(out);
        return out.toString("UTF-8");
    }

    public static class SyncResult {
        public final int uploaded;
        public final int downloaded;
        public final long timestamp;

        public SyncResult(int uploaded, int downloaded, long timestamp) {
            this.uploaded = uploaded;
            this.downloaded = downloaded;
            this.timestamp = timestamp;
        }
    }
}
