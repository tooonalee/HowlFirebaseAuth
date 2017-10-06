package com.test.HowlFirebaseAuth.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.HowlFirebaseAuth.BuildConfig;
import com.test.HowlFirebaseAuth.Utility.Singleton;
import com.test.HowlFirebaseAuth.ValueObject.ImageDTO;
import com.test.HowlFirebaseAuth.R;
import com.test.HowlFirebaseAuth.Utility.ProgressDialogTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static int GALLERY_CODE = 10;

    // FIXME: この辺りのメンバ変数についても頭にmをつけた形で名前を修正してください
    private ImageView imageView;
    private EditText title;
    private EditText description;
    // FIXME: もっと具体的な変数名をつけてください
    private Button button;
    private String imagePath;

    private TextView nameTextView;
    private TextView emailTextView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    // FIXME: ここはprivate化しなくても大丈夫ですか？
    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleApiClient mGoogleApiClient;

    Toolbar toolbar;

    private EditText errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        remoteConfig();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null) //2번째 인자값이 this인 이유는 GoogleApiClient.OnConnectionFailedListener 상속
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // FIXME: Exceptionだとアバウトすぎるので、
                FirebaseCrash.report(new Exception("My first Android non-fatal error"));
            }
        });
*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        nameTextView = (TextView) headerView.findViewById(R.id.header_name_textView);
        emailTextView = (TextView) headerView.findViewById(R.id.header_email_textView);

        nameTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        emailTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());



        /////////////////////////////////////////////////
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseStorage = mFirebaseStorage.getInstance();

        imageView = (ImageView) findViewById(R.id.imageView);
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        button = (Button) findViewById(R.id.button);

        //
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                upload(imagePath);
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });


    }

    // FIXME: ) と { の間には半角スペースを入れてください
    private void remoteConfig(){

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        //디버깅 테스트를 할때 사용
        //1분에 3번이상 요청하면 서버에 과부하가 걸리기 때문에 Failed발생
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        // FIXME: remote_config_defaultsがコミット漏れしていませんか？
        //서버에 매칭되는 값이 없을때 참조
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Fetch Succeeded",
                                    Toast.LENGTH_SHORT).show();

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Toast.makeText(HomeActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        displayWelcomeMessage();
                    }
                });
    }

    private void displayWelcomeMessage(){

        String toolBarColor = mFirebaseRemoteConfig.getString("toolBarColor");
        Boolean aBoolean = mFirebaseRemoteConfig.getBoolean("welcome_message_caps");
        String message = mFirebaseRemoteConfig.getString("welcome_message");

        toolbar.setBackgroundColor(Color.parseColor(toolBarColor));

        if(aBoolean){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message).setPositiveButton("Confirm", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //서버점검
                    //HomeActivity.this.finish();
                }
            });
            builder.create().show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            startActivity(new Intent(this, HomeActivity.class));
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, GALLERY_CODE);
        } else if (id == R.id.nav_slideshow) {
            startActivity(new Intent(this, BoardActivity.class));

        } else if (id == R.id.nav_manage) {

/*
        } else if (id == R.id.nav_share) {
*/

        } else if (id == R.id.nav_send) {
            sendNotification("Hello", "FIREBASE");
        } else if (id == R.id.nav_logout){

            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            mFirebaseAuth.signOut();
                            finish();
                            startActivity(new Intent(HomeActivity.this, MainActivity.class));
                        }
                    });


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == GALLERY_CODE){
            if(data.getData() != null){
                imagePath = getPath(data.getData());
                File f = new File(imagePath);
                //Image 적용
                imageView.setImageURI(Uri.fromFile(f));
            }
        }

        Bundle extras = data.getExtras();
        if (extras == null || extras.get("data") == null) return;

        if(resultCode == RESULT_CANCELED){
            finish();
            Toast.makeText(HomeActivity.this, "BACK",Toast.LENGTH_LONG).show();
        }

    }


    private void upload(String uri) {
        //Storage Server
        // FIXME: "gs://howlfirebaseauth-24336.appspot.com"を定数化してください
        StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl("gs://howlfirebaseauth-24336.appspot.com");

        final Uri file = Uri.fromFile( new File(uri) );
        //long dateはFileNameを区別させるためのID
        long date = new Date().getTime();
        // FIXME: "images/"を定数化してください

        StorageReference riversRef = storageRef.child("images/" + file.getLastPathSegment() + date);
        UploadTask uploadTask = riversRef.putFile(file);

        final ProgressDialogTask task = new ProgressDialogTask(HomeActivity.this);
        task.execute();


        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(HomeActivity.this, "Handle unsuccessful uploads", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests")
                Uri downloadUrl = taskSnapshot.getDownloadUrl();


                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setImageUrl(downloadUrl.toString());
                //Image Name
                imageDTO.setImageName(file.getLastPathSegment());
                imageDTO.setTitle(title.getText().toString());
                imageDTO.setDescription(description.getText().toString());
                imageDTO.setUid(mFirebaseAuth.getCurrentUser().getUid());
                imageDTO.setUserId(mFirebaseAuth.getCurrentUser().getEmail());

                //push메소드를 추가하면 상위에 Sequence Key 생성됌
                // FIXME: "images"を定数化してください
                mFirebaseDatabase.getReference()
                        .child("images")
                        .push()
                        .setValue(imageDTO)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(HomeActivity.this, "Firebase : Insert Data Successfully", Toast.LENGTH_LONG).show();
                                sendNotification(Singleton.connectedMember.getName(), title.getText().toString());
                                task.dismissDialog();

                                Intent intent = new Intent(HomeActivity.this, BoardActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(HomeActivity.this, "Firebase : Cannot insert data Failed", Toast.LENGTH_LONG).show();
                                task.dismissDialog();
                            }
                        });
            }
        });
    }

    public String getPath(Uri uri){
        String [] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    public void sendNotification(String connectedMemberName, String titleName){
        OkHttpClient client = new OkHttpClient();

        String to = "/topics/all";
        String name = connectedMemberName;
        String title = titleName;

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\r\n  \"to\": \"" + to + "\",\r\n  \"data\": {\r\n  \t\"name\" :\"" + name + "\",\r\n    \"title\" :\"" + title + "\"\r\n   }\r\n}");
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "key=AAAAjK4zVoE:APA91bHStDhnWFSZzspIvS45nvlb7M4z8rzXnWBvrkQnDwyHe9mwwpIo7EqE2Uqy8kZ1sX5DY_oli_2uVAoSfsOUw9iJyrcMcLVKsrKP6Y9Kj2bi-8aEy1t2nANEVs6q0T6f7hicQMV5")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "dac049ee-ed75-0861-6517-173fee88deae")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HomeActivity.this, "Send Failed ", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String myResponse = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HomeActivity.this, "Send Successfully", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }



}
