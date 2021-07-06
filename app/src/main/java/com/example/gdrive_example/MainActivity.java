package com.example.gdrive_example;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.gdrive_example.gdrive_manager.Token;
import com.example.gdrive_example.gdrive_manager.GDriveManager;
import com.example.gdrive_example.views.MyTv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static final int SIGN_IN=0;
    public static final String PROCESS_TYPE="pt";

    private Button btnSignIn, btnSignOut, btnDownload, btnUpload
            , btnListFiles, btnClean, btnCreateFile, btnListDownload;
    private Context context;

    private GDriveManager gDriveManager;
    private Token token;

    ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context=getBaseContext();

        attachLaunchers();
        token=new Token(this);
        token.initialize(this);
        gDriveManager=new GDriveManager(getApplicationContext(),MainActivity.this,token);

        btnCreateFile=findViewById(R.id.btnCreateFile);
        btnSignIn=findViewById(R.id.btnSignIn);
        btnSignOut=findViewById(R.id.btnSignOut);
        btnListFiles=findViewById(R.id.btnListFiles);
        btnDownload=findViewById(R.id.btnDownload);
        btnUpload=findViewById(R.id.btnUpload);
        btnClean=findViewById(R.id.btnCleanDrive);
        btnListDownload=findViewById(R.id.btnListDownload);

        // Creating file for testing.
        btnCreateFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile("MyFile1");
            }
        });

        // Signs in email account.
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //token.prepare();
                token.initialize(MainActivity.this);
                Intent signInIntent = token.getGoogleSignInClient().getSignInIntent();//googleSignInClient.getSignInIntent();
                signInIntent.putExtra(PROCESS_TYPE, SIGN_IN);
                openActivityForResult(signInIntent);
            }
        });

        // Signs out email and resets the token that is used for GDrive processes.
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                token.getGoogleSignInClient().signOut();
                token.getGoogleSignInClient().revokeAccess();
                token.resetAll();
                Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show();
            }
        });

        // Uploads the file that is created.
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Thread t2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gDriveManager.upload("MyFile1"
                                , getApplicationContext().getFilesDir().getPath()+File.separator+"MyFile1");
                    }
                });
                t2.start();
            }
        });

        // Downloads the file by its name if it exists.
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gDriveManager.download(context.getFilesDir().getPath()+File.separator+"downloads"
                                ,"MyFile1");
                    }
                });
                t2.start();
            }
        });

        // List the files in GDrive.
        btnListFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gDriveManager.list();
                    }
                });
                t2.start();
            }
        });

        // Cleans the gdrive.
        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gDriveManager.clean();
                    }
                });
                t2.start();
            }
        });

        // Prints out the files in "downloads" folder.
        btnListDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout ll=findViewById(R.id.llOutput);
                ll.removeAllViews();
                File[] files=new File(context.getFilesDir().getPath()+File.separator+"downloads").listFiles();
                for(File file:files)
                    ll.addView(new MyTv(context,file.getAbsolutePath()));
            }
        });
    }

    // Attaching launchers in onCreate or onAttach
    private void attachLaunchers() {
        // This method is used instead onActivityResult, onActivityResult is deprecated for newer versions.
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Thread t2 = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // Initalizes the token that is used for GDrive processes, after the success sign in.
                                    token.initialize(MainActivity.this);

                                    // silentSignIn method can be used in application start.
                                    String name= Objects.requireNonNull(
                                            token.getGoogleSignInClient().silentSignIn().getResult().getDisplayName());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, "Welcome "+name, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            t2.start();
                        } else {
                            System.out.println("--------------LOGGED FAILED");
                        }
                    }
                });
    }

    //Activity launcher.
    public void openActivityForResult(Intent intent) {
        switch (intent.getExtras().getInt(PROCESS_TYPE)) {
            case SIGN_IN:
                signInLauncher.launch(intent);
                break;
        }
    }

    // Creates file for testing the implementation.
    private boolean createFile(String fileName) {
            boolean result=false;
            try {
                result=new File(context.getFilesDir().getPath(), fileName).createNewFile();

                FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(String.valueOf(System.currentTimeMillis()).getBytes());
                fos.close();
                Toast.makeText(context,"Created",Toast.LENGTH_SHORT).show();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                result= false;
            }
            return result;
    }
}