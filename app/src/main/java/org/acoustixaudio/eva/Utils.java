package org.acoustixaudio.eva;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    MainActivity mainActivity ;
    public static String TAG = "Utils" ;
    Utils (MainActivity _mainActivity) {
        mainActivity = _mainActivity;
    }

    public JSONObject loadJSONFromAsset (String filename) {
        String json = null;
        JSONObject obj = null;
        try {
            InputStream is = mainActivity.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            obj = new JSONObject(json);
            // Now you can work with the JSONObject
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static JSONObject convertIniToJson(InputStream inputStream) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject currentSection = null;
        String currentSectionName = null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        Pattern sectionPattern = Pattern.compile("^\\[(.*?)\\]$");
        Pattern keyValuePattern = Pattern.compile("^(.*?)=(.*)$");

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                // Skip empty lines and comments
                continue;
            }

            Matcher sectionMatcher = sectionPattern.matcher(line);
            if (sectionMatcher.matches()) {
                currentSectionName = sectionMatcher.group(1);
                currentSection = new JSONObject();
                jsonObject.put(currentSectionName, currentSection);
//                Log.d(TAG, String.format ("%s: %s", currentSectionName, currentSection));
            } else {
                Matcher keyValueMatcher = keyValuePattern.matcher(line);
                if (keyValueMatcher.matches() && currentSection != null) {
                    String key = keyValueMatcher.group(1).trim();
                    String value = keyValueMatcher.group(2).trim();
                    currentSection.put(key, value);
//                    Log.d(TAG, String.format ("%s: %s", key, value));
                }
            }
        }
        reader.close();
        return jsonObject;
    }

    public static void extractZip(InputStream zipInputStream, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new IOException("Failed to create directory " + destDir.getAbsolutePath());
            }
        }

        ZipInputStream zis = new ZipInputStream(zipInputStream);
        ZipEntry zipEntry = zis.getNextEntry();
        byte[] buffer = new byte[1024];

        while (zipEntry != null) {
            File newFile = new File(destDirectory + File.separator + zipEntry.getName());
            Log.d(TAG, "Unzipping to " + newFile.getAbsolutePath());

            // Create directories for subdirectories in zip
            if (zipEntry.isDirectory()) {
                if (!newFile.mkdirs()) {
                    Log.w(TAG, "Failed to create directory " + newFile.getAbsolutePath());
                }
            } else {
                // Create parent directories if not exists
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zis.closeEntry();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }


    public void deleteRecursive(File destDir) {
        if (destDir.isDirectory()) {
            for (File child : destDir.listFiles()) {
                deleteRecursive(child);
            }
        }
        if (!destDir.delete()) {
            Log.e(TAG, "Failed to delete " + destDir.getAbsolutePath());
        }
    }

    public static File downloadFile(String fileURL, String saveDir) throws IOException {
        Log.d(TAG, "downloadFile() called with: fileURL = [" + fileURL + "], saveDir = [" + saveDir + "]");
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10, disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
            }

//            fileName = fileName.toLowerCase();
            // Opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
            Log.d(TAG, String.format ("[extract]: %s", saveFilePath));

            // Opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            Log.i(TAG, "File downloaded to " + saveFilePath);
            return new File(saveFilePath);
        } else {
            Log.e(TAG, "No file to download. Server replied HTTP code: " + responseCode);
            httpConn.disconnect();
            throw new IOException("Server replied HTTP code: " + responseCode);
        }
    }

    public static void writeToFile(List<Song> playlist, String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        for (Song line : playlist) {
            content.append(line.getUri()).append("\n");
        }

        File file = new File(filePath);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(content.toString().getBytes());
        fileOutputStream.close();
    }

    public static ArrayList<String> readLinesFromFile(String filePath) throws IOException {
        File file = new File(new File(filePath).getAbsolutePath());
        BufferedReader reader = new BufferedReader(new FileReader(file));
        ArrayList <String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }

}
