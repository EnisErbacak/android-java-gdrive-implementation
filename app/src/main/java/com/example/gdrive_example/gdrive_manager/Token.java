package com.example.gdrive_example.gdrive_manager;

import android.app.Activity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class Token {
    /*
    Token class that contains necessary elements for GDrive prcesses.
     */
    private final Scope SCOPE_GDRIVE_APPDATA_READ=new Scope("https://www.googleapis.com/auth/drive.appdata");

    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    private Drive driveService;
    private GoogleAccountCredential googleAccountCredential;
    private GoogleSignInAccount account;

    Activity activity;

    public Token(Activity activity) {
        this.activity=activity;
    }

    // Initializes the elements, implementation of this method can be changed as intended.
    public void initialize(Activity activity) {
        googleSignInOptions= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient= GoogleSignIn.getClient(activity, googleSignInOptions);
        account = GoogleSignIn.getLastSignedInAccount(activity);
        googleAccountCredential = GoogleAccountCredential
                .usingOAuth2(activity, Collections.singleton(DriveScopes.DRIVE_APPDATA /*"https://www.googleapis.com/auth/drive.appdata"*/));
        driveService=getDrive(googleAccountCredential,account);
    }

    public void connectDrive() {
        getDrive(googleAccountCredential, account);
    }

    public Scope getSCOPE_GDRIVE_APPDATA_READ() {
        return SCOPE_GDRIVE_APPDATA_READ;
    }

    public GoogleSignInOptions getGoogleSignInOptions() {
        return googleSignInOptions;
    }

    public void setGoogleSignInOptions(GoogleSignInOptions googleSignInOptions) {
        this.googleSignInOptions = googleSignInOptions;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.googleSignInClient = googleSignInClient;
    }

    public Drive getDriveService() {
        return driveService;
    }

    public void setDriveService(Drive driveService) {
        this.driveService = driveService;
    }

    //Sets all elements null when user signs out.
    public void resetAll() {
        googleSignInClient=null;
        googleSignInOptions=null;
        driveService=null;
    }

    // Sets up the drive element.
    private Drive getDrive(GoogleAccountCredential googleAccountCredential, GoogleSignInAccount account) {
        try {
            googleAccountCredential.setSelectedAccount(account.getAccount());
        }catch (NullPointerException e) {
            //This message can be showed or sign in permission can be requested.
            //Toast.makeText(activity, "You Must Sign In To Upload/Download", Toast.LENGTH_SHORT).show();
            /*
            Intent intent=getGoogleSignInClient().getSignInIntent();//googleSignInClient.getSignInIntent();
            intent.putExtra(MainActivity.PROCESS_TYPE, MainActivity.SIGN_IN);
            ((MainActivity)activity).openActivityForResult(intent);

             */
            e.printStackTrace();
        }
        return  new Drive.Builder(
                new NetHttpTransport()
                ,new GsonFactory()
                ,googleAccountCredential)
                .setApplicationName("GDrive Example")
                .build();
    }
}