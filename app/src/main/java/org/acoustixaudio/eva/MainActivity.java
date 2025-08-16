package org.acoustixaudio.eva;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebResourceResponse;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.BufferedInputStream;
import android.Manifest;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Skin skin;
    String skinDir = null ;
    org.acoustixaudio.eva.UI ui;
    private static final int FILE_PICKER_REQUEST_CODE = 123;
    public static final int AUDIO_FILE_REQUEST_CODE = 124;
    public static final int AUDIO_FOLDER_REQUEST_CODE = 125;

    public ConstraintLayout root;
    Utils utils = new Utils(this);
    WebView webView ;
    private PopupMenu popup;
    public Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);

        skin = new Skin(this);
        skinDir = getFilesDir().getAbsolutePath() + "/skin/";
        ui = new UI(this);

        player = new Player (this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.d(TAG, "onCreate: action " + action);
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                Log.d(TAG, "onCreate: Received URI: " + uri.toString());
                // Handle the .wsz file URI here
                // For example, you can pass it to your handleSendZip method
                handleSendZip(uri);
            }
        } else if (Intent.ACTION_SEND.equals(action) && type != null) {
            Uri zipUri = null;
            if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                zipUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
            Log.d(TAG, String.format("[intent]: %s", zipUri));
            if (zipUri != null) {
                String link = getSkinLink(zipUri.toString());
                Log.d(TAG, "onCreate: got skin link " + link);
            }
        }

        ConstraintLayout.LayoutParams lparams = new ConstraintLayout.LayoutParams((int) (275 * skin.scale), (int) (100 * skin.scale));
        lparams.setMargins(0, 0,  0, 0);
        lparams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lparams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;

        File skinDirectory = new File(skinDir);
        if (skinDirectory.exists())
            skin.load(skinDir);
        else {
            Log.d(TAG, String.format ("no custom skin found: %s", skinDir));
            skin.load();
        }

        try {
            ui.create();
            ui.skin();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        ui.logoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                openFilePicker();
                popupMenu();
            }
        });

        root.addView(webView, lparams);
        webView.setVisibility(View.GONE);

        popup = new PopupMenu(this, ui.logoBtn);
        popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    default:
                        Log.d(TAG, String.format ("[menu]: %d", menuItem.getItemId()));
                        return false;
                }

            }
        });

        MenuItem loadFromFile = popup.getMenu().findItem(R.id.load_from_file_item);
        loadFromFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                openFilePicker(FILE_PICKER_REQUEST_CODE);
                return false;
            }
        });

        popup.getMenu().findItem(R.id.load_default_item).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                skin.load();
                try {
                    ui.skin();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        });

        ui.loadCurrentPlaylist();
    }

    public void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        if (requestCode == AUDIO_FOLDER_REQUEST_CODE) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        } else {
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*"); // Filter for zip files
        }
        try {
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Log.e(TAG, "openFilePicker: ", e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                android.net.Uri uri = data.getData();
                if (uri != null) {
                    handleSendZip(uri);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            skin.load(skinDir);
                            try {
                                ui.skin();
                            } catch (Exception e) {
                                Log.e(TAG, "run: failed to load skin", e);
                                Toast.makeText(MainActivity.this, "Failed to load skin", Toast.LENGTH_SHORT).show();
                                skin.load();
                                try {
                                    ui.skin();
                                } catch (JSONException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                    });
                }
            }
        }

        if (requestCode == AUDIO_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String filename = getFileNameFromUri(uri);
            ui.addToPlaylist(uri);
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return;
        }

        if (requestCode == AUDIO_FOLDER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // Assuming 'uri' is the URI of the selected folder
            DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);
            if (documentFile != null && documentFile.isDirectory()) {
                DocumentFile[] files = documentFile.listFiles();
                for (DocumentFile file : files) {
                    if (file.isFile()) {
                        // Handle each file, e.g., add to playlist
                        Log.d(TAG, "onActivityResult: File in folder: " + file.getName() + " URI: " + file.getUri());
                        ui.addToPlaylist(file.getUri());
                    }
                }
            }
            return;
        }
    }

    String getFileFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            json = String.valueOf(is.read(buffer));
            is.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    void handleSendZip(Uri zipUri) {
        Log.d(TAG, "handleSendZip() called with: zipUri = [" + zipUri + "]");
        try {
            InputStream inputStream = getContentResolver().openInputStream(zipUri);
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry zipEntry;
            File destDir = new File(skinDir);
            if (!destDir.exists()) destDir.mkdirs();
//            utils.deleteRecursive(destDir);
//            if (!destDir.exists()) destDir.mkdirs();

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName().toLowerCase();
                Log.d(TAG, "handleSendZip: extract file " + entryName);
                File newFile = new File(destDir, new File(entryName).getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    FileOutputStream fout = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = zipInputStream.read(buffer)) != -1) fout.write(buffer, 0, count);
                    fout.close();
                }
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (IOException e) {
            Log.d(TAG, "handleSendZip: Error processing zip file");
            e.printStackTrace();
        }
    }

    String getSkinLink (String url) {
        Log.d(TAG, "getSkinLink() called with: url = [" + url + "]");
        String link = null ;
        // Enable JavaScript if the page requires it
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String requestUrl = request.getUrl().toString();
//                Log.d(TAG, "shouldInterceptRequest: " + requestUrl);
                // Check if the request URL matches the pattern for the skin link
                if (requestUrl.endsWith(".wsz")) {
                    // This is the skin link
                    // You can now download the zip file or handle it as needed
                    Log.d(TAG, "Skin link found: " + requestUrl);
                    File file = null;
                    try {
                        file = Utils.downloadFile(requestUrl, String.valueOf(getFilesDir()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    handleSendZip(Uri.fromFile(file));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            skin.load(skinDir);
                            try {
                                ui.skin();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        webView.loadUrl(url);
        return link;
    }

    void popupMenu () {
        popup.show();
    }


    public String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) { // Check if column exists
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (uri.getScheme().equals("file")) {
            // For file URIs, the last segment of the path is usually the file name
            result = uri.getLastPathSegment();
        }
        return result;
    }

    public List<File> getFilesFromFolder(String directoryPath) {
        List<File> fileList = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) { // Check if it's a file (not a subdirectory)
                        fileList.add(file);
                    }
                    // If you also want to list files in subdirectories recursively:
                    // else if (file.isDirectory()) {
                    //     fileList.addAll(getFilesFromFolder(file.getAbsolutePath()));
                    // }
                }
            }
        } else {
            // Handle the case where the directory doesn't exist or is not a directory
            Log.e("FileUtils", "Directory not found or is not a directory: " + directoryPath);
        }
        return fileList;
    }
}