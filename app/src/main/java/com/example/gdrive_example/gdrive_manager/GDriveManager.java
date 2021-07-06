package com.example.gdrive_example.gdrive_manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.gdrive_example.views.MyTv;
import com.example.gdrive_example.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class GDriveManager {
    /*
    This class handles the GDrive process such as download/upload.
     */
    private Context context;
    private Activity activity;
    private Token token;

    public GDriveManager(Context context,Activity activity, Token token) {
        this.context = context;
        this.activity=activity;
        this.token=token;
    }

    public void upload(String fileUploadName, String path) {
        File fileMetaData = new File();
        fileMetaData.setName(fileUploadName); // File will be named as fileUploadName in GDrive.
        fileMetaData.setParents(Collections.singletonList("appDataFolder"));

        java.io.File file = new java.io.File(path);

        FileContent mediaContent = new FileContent("application/zip", file);

        File myFile = null;

        try {
            myFile = token.getDriveService().files().create(fileMetaData, mediaContent).execute();
            shw("Uploaded");
        }
        // If application has no permission to access GDrive
        //  or access was revoked that is used by the valid token.
        catch (UserRecoverableAuthIOException e) {
            Intent intent=e.getIntent();
            context.startActivity(intent);

            /*
             This message can be shown to tell the user to repeat the process
             or this method can be called after access gained by adding case
             and the launcher into the openActivityForResult method.
             */
            shw("Try Again After This Process");

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (java.lang.IllegalArgumentException e) {//Email permission is required
            signInFirst();
            e.printStackTrace();
        }
        catch (NullPointerException e) {// When token is null.
            signInFirst();
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (myFile == null) {
            shw("NULL GDRIVE FILE!");
            try {
                throw new IOException("null result when requesting file creation");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Downloads specific file by its name.
    public void download(String dwnldPath, String fileName) {
        // Creating the download folder.
        new java.io.File(context.getFilesDir().getPath()+ java.io.File.separator+"downloads").mkdir();
        OutputStream out = null;
        try {
            // The downloaded File will be named as "fileName".
            out = new FileOutputStream(new java.io.File(dwnldPath,"d_"+fileName));
            FileList fileList=token.getDriveService().files().list().setSpaces("appDataFolder").execute();

            if(fileList.getFiles().size()!=0) {
                for (File file : fileList.getFiles()) {
                    if (file.getName().equals(fileName)) {
                        token.getDriveService().files().get(file.getId()).executeMediaAndDownloadTo(out);
                        shw("downloded");
                        break;
                    } else {
                        shw("No File!");
                    }
                }
            }else {
                shw("No File in GDrive");
            }
        }
        // If application has no permission to access GDrive
        //  or access was revoked that is used by the valid token.
        catch (UserRecoverableAuthIOException e) {
            shw("Try Again After Permision Process");
            Intent requestAgainGDrive = e.getIntent();
            activity.startActivity(requestAgainGDrive);// Requesting user for accessing GDrive.
        }catch (IOException e) {
            e.printStackTrace();
        }catch (java.lang.IllegalArgumentException e) { //Email permission is required
            signInFirst();
            e.printStackTrace();
        }catch (NullPointerException e) { // When token elements are null (Not Signed In).
            signInFirst();
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // List files in the "appDataFolder" folder in the GDrive that accessed by only this application.
    public void list() {
        try {
            FileList fileList = token.getDriveService().files().list().setSpaces("appDataFolder").execute();
            if(fileList.getFiles().size()!=0) {
                cleanPanel();
                for (File file : fileList.getFiles()) {
                    System.out.println(file.getName());
                    printOut(file.getName());
                }
            }else {
                shw("No File");
            }
        }
        // If application has no permission to access GDrive
        //  or access was revoked that is used by the valid token.
        catch (UserRecoverableAuthIOException e) {
            shw("Try Again After This Process");
            Intent requestAgainGDrive = e.getIntent();
            activity.startActivity(requestAgainGDrive); // Requesting user for accessing GDrive.
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (java.lang.IllegalArgumentException e) {//Email permission is required
            signInFirst();
            e.printStackTrace();
        }catch (NullPointerException e) {
            signInFirst();
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cleans everything in GDrive to avoid file conflict.
    // Instead of this file picking view can be created to let user choose the file.
    public void clean() {
        try {
            FileList fileList=token.getDriveService().files().list().setSpaces("appDataFolder").execute();
            for(File file: fileList.getFiles()) {
                token.getDriveService().files().delete(file.getId()).execute();
            }
            shw("CLEANED");
        }
        // If application has no permission to access GDrive
        //  or access was revoked that is used by the valid token.
        catch (UserRecoverableAuthIOException e) {
        shw("Try Again After This Process");
        Intent requestAgainGDrive = e.getIntent();
        activity.startActivity(requestAgainGDrive);// Requesting user for accessing GDrive.
        e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }catch (java.lang.IllegalArgumentException e) {//Email permission is required
            signInFirst();
            e.printStackTrace();
        }catch (NullPointerException e) {
            signInFirst();
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Instead of this method, email/permission can be requested.
    private void signInFirst() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Sign In First!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Show toast messages in ui thread.
    private void shw(String str) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Prints out in linear layout in the main activity layout.
    private void printOut(String str) {
        LinearLayout ll=activity.findViewById(R.id.llOutput);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ll.addView(new MyTv(context, str));
            }
        });
    }

    // It cleans linear layout in the main activity layout.
    private void cleanPanel() {
        LinearLayout ll=activity.findViewById(R.id.llOutput);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ll.removeAllViews();
            }
        });
    }
}