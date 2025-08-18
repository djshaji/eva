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
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import android.widget.EditText;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebResourceResponse;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.BufferedInputStream;
import android.Manifest;
import android.net.Uri;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    private static final String TAG = "MainActivity";
    Skin skin;
    String skinDir = null ;
    org.acoustixaudio.eva.UI ui;
    private static final int FILE_PICKER_REQUEST_CODE = 123;
    public static final int AUDIO_FILE_REQUEST_CODE = 124;
    public static final int AUDIO_FOLDER_REQUEST_CODE = 125;
    public static final int SAVE_PLAYLIST_REQUEST_CODE = 126;
    public static final int LOAD_PLAYLIST_REQUEST_CODE = 127;

    public ConstraintLayout root;
    private String eqPresetDir;
    Utils utils = new Utils(this);
//    WebView webView ;
    private PopupMenu popup;
    public Player player;
    private BillingClient billingClient;
    private String playlistDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        webView = new WebView(this);

        playlistDir = getFilesDir().getAbsolutePath() + "/playlists/";
        new File (playlistDir).mkdirs();
        eqPresetDir = getFilesDir().getAbsolutePath() + "/presets/";
        new File(eqPresetDir).mkdirs();
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
//                String link = getSkinLink(zipUri.toString());
//                Log.d(TAG, "onCreate: got skin link " + link);
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

//        root.addView(webView, lparams);
//        webView.setVisibility(View.GONE);

        popup = new PopupMenu(this, ui.logoBtn);
        popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
        popup.getMenu().findItem(R.id.buy_now).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                purchase();
                return false;
            }
        });

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

        popup.getMenu().findItem(R.id.no_dest).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                loadAlbum("No Destination");
                return false;
            }
        });

        popup.getMenu().findItem(R.id.gtit).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                loadAlbum("Give Time It's Time");
                return false;
            }
        });

        popup.getMenu().findItem(R.id.abs_inc).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                loadAlbum("Abstractions Inconclusive");
                return false;
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

                utils.deleteRecursive(new File(skinDir));
                return false;
            }
        });

        File playlist = new File(ui.defaultPlaylist);
        if (! playlist.exists())
            ui.addToPlaylist(Uri.parse("https://music.shaji.in/media/No%20Destination/Rock%20and%20Roll%20will%20never%20die.mp3"));
        ui.loadCurrentPlaylist();

        // Initialize BillingClient
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts() // For one-time products
                        .build()).build(); // enablePendingPurchases() is important.

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d(TAG, "BillingClient: Setup successful.");
                } else {
                    Log.e(TAG, "BillingClient: Setup failed. Response code: " + billingResult.getResponseCode());
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play by calling the startConnection() method.
                Log.w(TAG, "BillingClient: Service disconnected.");
            }
        });
    }

    public void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        String intentType = "*/*";

        if (requestCode == FILE_PICKER_REQUEST_CODE) {
            intent.setType(intentType);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else if (requestCode == AUDIO_FILE_REQUEST_CODE) {
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        if (requestCode == AUDIO_FOLDER_REQUEST_CODE) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        } else if (requestCode == LOAD_PLAYLIST_REQUEST_CODE) {
            intent.setType("application/x-eva-playlist"); // Filter for your custom playlist type
            intent.setType(intentType);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/x-eva-playlist", "audio/x-mpegurl", "audio/mpegurl"}); // Common M3U types
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else if (requestCode == SAVE_PLAYLIST_REQUEST_CODE) {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intentType = "application/x-eva-playlist"; // Custom MIME type for playlist
            intent.setType(intentType);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
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

        if (requestCode == SAVE_PLAYLIST_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        // TODO: Implement actual playlist saving logic here
                        // For now, let's just write a dummy string
                        String playlistContent = "This is a dummy playlist.";
                        outputStream.write(playlistContent.getBytes());
                        outputStream.close();
                        Toast.makeText(this, "Playlist saved to " + getFileNameFromUri(uri), Toast.LENGTH_LONG).show();
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found for saving playlist: ", e);
                    Toast.makeText(this, "Error: File not found", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Error writing playlist to file: ", e);
                    Toast.makeText(this, "Error saving playlist", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == LOAD_PLAYLIST_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    ui.loadPlaylist(String.valueOf(uri));
                    Toast.makeText(this, "Playlist loaded: " + getFileNameFromUri(uri), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error loading playlist: ", e);
                    Toast.makeText(this, "Error loading playlist", Toast.LENGTH_SHORT).show();
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

    /*
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


     */
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

    public void savePlaylist () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Playlist As");

        final EditText input = new EditText(this);
        input.setHint("playlist_name.m3u"); // .evpl or your custom extension
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String fileName = input.getText().toString();
            if (!fileName.isEmpty()) {
                ui.savePlaylist(playlistDir + fileName);
                Toast.makeText(this, "Playlist saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a filename", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    void loadPlaylist () {
        // Get the path to your app's internal storage directory for playlists
        List<File> playlistFiles = getFilesFromFolder(playlistDir);
        if (playlistFiles.isEmpty()) {
            Toast.makeText(this, "No playlists found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a list of playlist names to display in the dialog
        List<String> playlistNames = new ArrayList<>();
        for (File file : playlistFiles) {
            playlistNames.add(file.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Playlist");
        builder.setItems(playlistNames.toArray(new String[0]), (dialog, which) -> {
            File selectedPlaylistFile = playlistFiles.get(which);
            ui.loadPlaylist(selectedPlaylistFile.getAbsolutePath());
            Toast.makeText(this, "Loading " + selectedPlaylistFile.getName(), Toast.LENGTH_SHORT).show();
        });
        // Add a "Delete" option (optional, handle with care)
        builder.setNeutralButton("Delete", (dialog, which) -> {
            // This will be overridden later to show a sub-dialog for deletion
            // For now, it does nothing or you can prompt for confirmation directly
            showDeletePlaylistDialog(playlistFiles, playlistNames);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeletePlaylistDialog(List<File> playlistFiles, List<String> playlistNames) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Playlist")
                .setItems(playlistNames.toArray(new String[0]), (dialog, which) -> ui.deletePlaylist(playlistFiles.get(which)))
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    public void saveEqPreset () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save EQ Preset As");

        final EditText input = new EditText(this);
        input.setHint("preset_name.eqf"); // .eqf or your custom extension
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String fileName = input.getText().toString();
            if (!fileName.isEmpty()) {
                ui.savePresetToFile(eqPresetDir + fileName);
                Toast.makeText(this, "EQ Preset saved (not really)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a filename", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void loadEqPreset () {
        List<File> presetFiles = getFilesFromFolder(eqPresetDir);
        if (presetFiles.isEmpty()) {
            Toast.makeText(this, "No EQ presets found.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> presetNames = new ArrayList<>();
        for (File file : presetFiles) {
            presetNames.add(file.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select EQ Preset");
        builder.setItems(presetNames.toArray(new String[0]), (dialog, which) -> {
            File selectedPresetFile = presetFiles.get(which);
            ui.loadPresetFromFile(selectedPresetFile.getAbsolutePath());
            Toast.makeText(this, "Loading " + selectedPresetFile.getName(), Toast.LENGTH_SHORT).show();
        });
        builder.setNeutralButton("Delete", (dialog, which) -> {
            showDeleteEqPresetDialog(presetFiles, presetNames);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteEqPresetDialog(List<File> presetFiles, List<String> presetNames) {
        new AlertDialog.Builder(this)
                .setTitle("Delete EQ Preset")
                .setItems(presetNames.toArray(new String[0]), (dialog, which) -> ui.deleteEqPreset(presetFiles.get(which)))
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    void loadAlbum (String album) {
        String content = null;
        try {
            InputStream f = getAssets().open("shaji.json");
            final int size = f.available();
            final byte[] buffer = new byte[size];
            f.read(buffer);
            f.close();
            content = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(content);
            JSONArray array = jsonObject.getJSONArray(album);

            f.close();
            ui.clearPlaylist();
            for (int i = 0; i < array.length(); i++) {
                ui.addToPlaylist(Uri.parse(array.getString(i)));
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public void purchase () {
        if (billingClient.isReady()) {
            // Replace "your_product_id" with the actual product ID from Google Play Console
            String productId = "coffee"; // Or your actual product ID

            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(
                                    Arrays.asList(QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId(productId)
                                            .setProductType(BillingClient.ProductType.INAPP) // or SUBS
                                            .build()))
                            .build();

            billingClient.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    (billingResult, productDetailsList) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            ProductDetails productDetails = productDetailsList.getProductDetailsList().get(0); // Assuming you query for one product

                            BillingFlowParams.ProductDetailsParams productDetailsParams =
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .build();

                            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                            productDetailsParamsList.add(productDetailsParams);

                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList)
                                    .build();

                            BillingResult launchResult = billingClient.launchBillingFlow(this, billingFlowParams);
                            if (launchResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                                Log.e(TAG, "purchase: Failed to launch billing flow. Response code: " + launchResult.getResponseCode());
                                Toast.makeText(this, "Purchase failed: " + launchResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "purchase: Failed to query product details. Response code: " + billingResult.getResponseCode());
                            Toast.makeText(this, "Failed to load product details. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e(TAG, "purchase: BillingClient is not ready.");
            Toast.makeText(this, "Billing service not ready. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "onPurchasesUpdated: User cancelled the purchase.");
            Toast.makeText(this, "Purchase cancelled.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "onPurchasesUpdated: Error during purchase. Response code: " + billingResult.getResponseCode());
            Toast.makeText(this, "Purchase error: " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePurchase(Purchase purchase) {
        // Verify the purchase.
        // For non-consumables, acknowledge the purchase.
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // Grant entitlement to the user.
                        Log.d(TAG, "onPurchasesUpdated: Purchase acknowledged and successful: " + purchase.getSkus().get(0));
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Purchase Successful")
                                .setMessage("Thank you for your supporting this project!")
                                .setPositiveButton("You're welcome", (dialog, which) -> {
                                    // You can add any further actions here if needed
                                    dialog.dismiss();
                                })
                                .show();
                        // TODO: Update UI or app state to reflect the purchase
                    }
                });
            }
        }
    }
}