package org.antrack.app.plugins;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderLongpollResult;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import org.antrack.app.C;
import org.antrack.app.libs.Utils;
import org.apache.http.HttpStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class Dropbox {
    final private String TAG = "Dropbox";

    private HashMap<String, String> cursors;
    private DbxClientV2 client;


    public Dropbox() {
    }

    public Dropbox(String token) {
        DbxRequestConfig config = new DbxRequestConfig("AnTrack/1.0");
        client = new DbxClientV2(config, token);
    }

    public void auth(Activity activity) {
        Auth.startOAuth2Authentication(activity, C.DROPBOX_KEY);
    }

    public String resume() {
        return Auth.getOAuth2Token();
    }

    public void putFile(final String lFile, final String rFile, boolean delete) {
        File file = new File(lFile);
        try {
            FileInputStream is = new FileInputStream(file);
            FileMetadata meta = client.files().uploadBuilder(rFile)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(is);
            is.close();
            Log.d(TAG, "PutFile: " + rFile + " " + meta.toString());
            if (delete) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "PutFile exception: " + e.toString());
        }
    }

    public void getFile(final String lFile, final String rFile) {
        File file = new File(lFile);
        try {
            FileOutputStream os = new FileOutputStream(file);
            FileMetadata meta = client.files().downloadBuilder(rFile).download(os);
            os.close();
            Log.d(TAG, "GetFile: " + rFile + " " + meta.toString());
        } catch (Exception e) {
            Log.e(TAG, "GetFile exception: " + e.toString());
        }
    }

    public void delete(final String path, boolean permanent) {
        try {
            if (permanent)
                client.files().permanentlyDelete(path);
            else
                client.files().delete(path);
        } catch (Exception e) {
            Log.e(TAG, "Delete exception: " + e.toString());
        }
    }

    // Return list of files in directory with full path
    public ArrayList<String> listDir(final String rDir) {
        return listDir(rDir, false);
    }

    public ArrayList<String> listDir(final String rDir, boolean withDeleted) {
        ArrayList<String> fileList = new ArrayList<>();

        try {
            ListFolderResult result = client.files().listFolderBuilder(rDir)
                    .withIncludeDeleted(withDeleted)
                    .start();
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    String path = metadata.getPathLower();
                    if (metadata instanceof FolderMetadata)
                        fileList.add(path + "/");
                    else
                        fileList.add(path);
                }

                if (!result.getHasMore()) {
                    break;
                }

                result = client.files().listFolderContinue(result.getCursor());
            }
        } catch (Exception e) {
            Log.e(TAG, "listDir exception: " + e.toString());
            return null;
        }

        return fileList;
    }


    private void listDirRecursive(String rDir, ArrayList<String> fileList) {
        ArrayList<String> fileList1 = listDir(rDir);
        ArrayList<String> fileList2 = new ArrayList<>();
        for(String file : fileList1) {
            if (file.endsWith("/"))
                listDirRecursive(file, fileList);
            fileList2.add(file);
        }
        fileList.addAll(fileList2);
    }

    // List dirs and sub dirs
    public ArrayList<String> listDirs(String rDir) {
        ArrayList<String> fileList = new ArrayList<>();
        listDirRecursive(rDir, fileList);
        return fileList;
    }

    // Get all files from dir
    public void getDir(String lDir, String rDir) {
        ArrayList<String> files = listDir(rDir);
        if (files == null) {
            Log.e(TAG, "GetFiles: dir is empty");
            return;
        }

        for (String file : files) {
            getFile(lDir + "/" + new File(file).getName(), file);
        }
    }

    public ArrayList<String> watchForChanges(String dir) {
        ListFolderLongpollResult longpollResult;
        ListFolderResult folderResult;

        ArrayList<String> fileList = null;

        if (cursors == null)
            cursors = new HashMap<>();

        try {
            if (!cursors.containsKey(dir)) {
                folderResult = client.files().listFolderBuilder(dir).withRecursive(true).start();
                cursors.put(dir, folderResult.getCursor());
            }

            longpollResult = client.files().listFolderLongpoll(cursors.get(dir), C.DB_LONGPOLL_TIMEOUT);

            if (longpollResult.getChanges()) {
                boolean hasMore = true;
                while (hasMore) {
                    folderResult = client.files().listFolderContinue(cursors.get(dir));
                    cursors.put(dir, folderResult.getCursor());
                    hasMore = folderResult.getHasMore();

                    fileList = new ArrayList<>();
                    for (Metadata md : folderResult.getEntries()) {
                        String changedFilePath = md.getPathLower();
                        Log.d(TAG, "watchForChanges: modified file: " + changedFilePath);
                        fileList.add(changedFilePath);
                    }
                }
            }
            return fileList;
        } catch (Exception e) {
            Log.e(TAG, "watchForChanges exception: " + e.toString());
            return null;
        }
    }

    private String longpollURL="https://notify.dropboxapi.com/2/files/list_folder/longpoll";

    private boolean longpoll(String cursor) {
        try {
            String charset = "UTF-8";

            URL url = new URL(longpollURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(C.DB_LONGPOLL_TIMEOUT * 1000);
            conn.setReadTimeout(C.DB_LONGPOLL_TIMEOUT * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Accept-Charset", charset);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            String query =
                    "{\"cursor\": \"" + cursor +
                    "\",\"timeout\": " + Integer.toString(C.DB_LONGPOLL_TIMEOUT) + "}";

            Log.e(TAG, "query: " + query);

            OutputStream os = conn.getOutputStream();
            os.write(query.getBytes());
            os.flush();

            int status = conn.getResponseCode();

            InputStream is;

            if(status >= HttpStatus.SC_BAD_REQUEST)
                is = conn.getErrorStream();
            else
                is = conn.getInputStream();

            String response = Utils.StreamToString(is);

            Log.e(TAG, "Longpoll result: " + response);
            return response.contains("true");
        } catch (Exception e) {
            Log.e(TAG, "Longpoll exception: " + e);
            return false;
        }
    }
}
