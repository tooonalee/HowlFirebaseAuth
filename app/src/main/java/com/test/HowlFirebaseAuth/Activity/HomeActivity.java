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
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import com.test.HowlFirebaseAuth.ValueObject.Member;
import com.test.HowlFirebaseAuth.ValueObject.WorkInfo;
import com.test.HowlFirebaseAuth.dao.MemberDAO;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private DatePicker datePicker;
    private TimePicker timePicker;

    private Button checkInOutButton;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog.Builder imageDialogBuilder;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleApiClient mGoogleApiClient;

    private Date currentDate;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private String datePickerMsg;
    private String timePickerMsg;

    private Member searchMember;

    private LayoutInflater factory;
    private ImageView imageView;
    private View customView;

    private Toolbar toolbar;

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null) //2번째 인자값이 this인 이유는 GoogleApiClient.OnConnectionFailedListener 상속
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();

        initFirebaseDB();

        currentDate = new Date();

        datePicker = (DatePicker) findViewById(R.id.datePicker);
        datePicker.init(datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int argYear, int argMonth, int argDay) {
                        // TODO Auto-generated method stub
                        year = argYear;
                        month = argMonth;
                        day = argDay;
                        datePickerMsg = String.format("%d/%d/%d", argYear , argMonth+1, argDay);

                        updateUI();
                    }
                });

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int argHour, int argMinute) {
                hour = argHour;
                minute = argMinute;
                timePickerMsg = String.format("%d:%d", hour , minute);

                updateUI();
            }
        });

        year = datePicker.getYear();
        month = datePicker.getMonth();
        day = datePicker.getDayOfMonth();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        }else{
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        datePickerMsg = String.format("%d/%d/%d", year , month, day);
        timePickerMsg = String.format("%d:%d", hour , minute);

        checkInOutButton = (Button) findViewById(R.id.checkInOut_button);
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        updateUI();






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

        if (id == R.id.nav_home) {
            // Handle the camera action
            startActivity(new Intent(this, HomeActivity.class));
        } else if (id == R.id.nav_print) {
            startActivity(new Intent(this, TableActivity.class));
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

            }
        }

        Bundle extras = data.getExtras();
        if (extras == null || extras.get("data") == null) return;

        if(resultCode == RESULT_CANCELED){
            finish();
            Toast.makeText(HomeActivity.this, "BACK",Toast.LENGTH_LONG).show();
        }

    }



    public void initFirebaseDB(){
        mFirebaseDatabase.getReference().child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                searchMember = null;
                ArrayList<Member> memberList = new ArrayList<>();

                memberList.clear();
                for (DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    Member member = snapShot.getValue(Member.class);
                    member.setKey(snapShot.getKey());
                    memberList.add(member);
                }
                if(memberList.isEmpty()){
                    return;
                }
                //自分のEmailでMemberObjectを探す
                for (Member member : memberList) {
                    if (member.getMemberEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        Log.d("More", member.getMemberEmail());
                        searchMember = member;
                        MemberDAO.getInstance().returnMember = member;
                        Singleton.getInstance().connectedMember = member;
                        break;
                    }
                }

                if(searchMember.isWorkingFlag()){
                    checkInOutButton.setText("退勤");
                    checkInOutButton.setBackgroundColor(Color.parseColor("#33B5E5"));
                }else{
                    checkInOutButton.setText("出勤");
                    checkInOutButton.setBackgroundColor(Color.parseColor("#FF4081"));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateFirebaseDB(){
        if(searchMember != null){
            if(searchMember.isWorkingFlag()){
                //退勤プロセス
                searchMember.setWorkingFlag(false);
                checkInOutButton.setText("出勤");
                checkInOutButton.setBackgroundColor(Color.parseColor("#FF4081"));
                //33B5E5 FF4081

                //退勤のボタンを活性化

            }else{
                //出勤プロセス
                searchMember.setWorkingFlag(true);
                checkInOutButton.setText("退勤");
                checkInOutButton.setBackgroundColor(Color.parseColor("#33B5E5"));
                //出勤のボタンを活性化

            }

            //DB key
            String key = searchMember.getKey();
            //修正したMemberのData
            Map<String, Object> memberValues = searchMember.toMap();
            //FirebaseDBにDataを入れるプロセス
            Map<String, Object> childUpdates = new HashMap<>();
            //   /members/-Kw98VPQ1hhHkKQEFSmh/{memberEmail : "ascomjapan@gmail.com", workingFlag = TRUE}
            childUpdates.put("/members/" + key, memberValues);
            mFirebaseDatabase.getReference().updateChildren(childUpdates);
        }else{  }

    }


    public void updateUI(){
        //Dialog Setting
        alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
        if(Singleton.getInstance().connectedMember.isWorkingFlag()){
            alertDialogBuilder
                    .setTitle("退勤")
                    .setMessage(mFirebaseAuth.getCurrentUser().getDisplayName()+"様、" +
                            datePickerMsg + " " + timePickerMsg +"に退勤しますか？");

        }else{
            alertDialogBuilder
                    .setTitle("出勤")
                    .setMessage(mFirebaseAuth.getCurrentUser().getDisplayName()+"様、" +
                            datePickerMsg + " " + timePickerMsg +"に出勤しますか？");
        }
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //OK
                                insertUpdateWorkInfo();
                                //workingFlag変換
                                updateFirebaseDB();
                                displayImage();
                            }
                        })
                .setNegativeButton("Cancle",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Cancle
                                dialog.cancel();
                            }
                        });
    }


    public void insertUpdateWorkInfo(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String dateString = year+"/"+(month+1)+"/"+day+" "+hour+":"+minute;

        try{
            currentDate = dateFormat.parse(dateString);
        }catch (ParseException e){
            e.printStackTrace();
        }

        /*currentDate = new Date();
        currentDate.setYear(year);
        currentDate.setMonth(month+1);
        currentDate.setDate(day);
        currentDate.setHours(hour);
        currentDate.setMinutes(minute);*/

        if(searchMember.isWorkingFlag()){
            //退勤時間登録
            mFirebaseDatabase.getReference().child("workinfo").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<WorkInfo> infoList = new ArrayList<>();
                    infoList.clear();

                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        WorkInfo info = snapshot.getValue(WorkInfo.class);
                        infoList.add(info);
                    }

                    //AllWorkInfoListByEmail
                    List<WorkInfo> subInfoList = new ArrayList<>();
                    subInfoList.clear();
                    for(WorkInfo info:infoList){
                        if(info.getMemberEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                            subInfoList.add(info);
                        }
                    }

                    //Last index
                    WorkInfo searchInfo;
                    if(subInfoList.isEmpty()){
                        return;
                    }else if(subInfoList.size() == 1){
                        searchInfo = subInfoList.get(0);
                    }else{
                        searchInfo = subInfoList.get(subInfoList.size() - 1);
                    }

                    searchInfo.setCreateOffWorkDate(currentDate);
                    searchInfo.setWorkingTime(
                            (int)(searchInfo.getCreateOffWorkDate().getTime() - searchInfo.getCreateOnWorkDate().getTime() ) / (3600 * 1000)
                    );

                    String key = searchInfo.getKey();
                    mFirebaseDatabase.getReference().child("workinfo");
                    Map<String, Object> infoValues = searchInfo.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/workinfo/"+ key , infoValues);
                    mFirebaseDatabase.getReference().updateChildren(childUpdates);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            //出勤時間登録
            WorkInfo info = new WorkInfo();
            info.setName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            info.setCreateOnWorkDate(currentDate);
            info.setMemberEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            mFirebaseDatabase.getReference().child("workinfo").push().setValue(info)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFirebaseDatabase.getReference().child("workinfo").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    List<WorkInfo> infoList = new ArrayList<>();
                                    infoList.clear();

                                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                        WorkInfo info = snapshot.getValue(WorkInfo.class);
                                        info.setKey(snapshot.getKey());
                                        infoList.add(info);
                                    }

                                    //AllWorkInfoListByEmail
                                    List<WorkInfo> subInfoList = new ArrayList<>();
                                    subInfoList.clear();
                                    for(WorkInfo info:infoList){
                                        if(info.getMemberEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                                            subInfoList.add(info);
                                        }
                                    }

                                    //Last index
                                    WorkInfo searchInfo;
                                    if(subInfoList.size() == 1){
                                        searchInfo = subInfoList.get(0);
                                    }else{
                                        searchInfo = subInfoList.get(subInfoList.size() - 1);
                                    }

                                    String key = searchInfo.getKey();
                                    mFirebaseDatabase.getReference().child("workinfo");
                                    Map<String, Object> infoValues = searchInfo.toMap();
                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/workinfo/"+ key , infoValues);
                                    mFirebaseDatabase.getReference().updateChildren(childUpdates);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });
        }
    }

    public void displayImage(){

        final StorageReference storageReference = mFirebaseStorage.getReferenceFromUrl("gs://howlfirebaseauth-24336.appspot.com");


        final StorageReference riversRef = storageReference.child("imageList/checkIn/dog.jpg");

        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //Image 적용
                factory = LayoutInflater.from(HomeActivity.this);
                customView = factory.inflate(R.layout.dialog_custom, null);
                imageView = (ImageView) customView.findViewById(R.id.dialog_imageView);

                Uri imageURI_1 = Uri.parse("https://firebasestorage.googleapis.com/v0/b/howlfirebaseauth-24336.appspot.com/o/imageList%2FcheckIn%2Fdog.jpg?alt=media&token=07a4a407-2d8c-412b-8d39-ce151482e33b");
                Uri imageURI_2 = Uri.parse("https://firebasestorage.googleapis.com/v0/b/howlfirebaseauth-24336.appspot.com/o/imageList%2FcheckOut%2Fdog2.jpg?alt=media&token=f042b32f-69d9-4ac7-ae13-955a04f3bcca");
                //imageView.setImageURI(tmpURI);

                Log.d("URI", uri.getPath());

                imageDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                imageDialogBuilder
                        //.setTitle("")
                        //.setMessage("")
                        .setCancelable(true)
                        .setView(customView)
                        .setPositiveButton("Confirm",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //OK

                                    }
                                }).show();

                if(Singleton.getInstance().connectedMember.isWorkingFlag()){
                    //退勤
                    Glide.with(customView)
                            .load(imageURI_1)
                            .into(imageView);
                }else{
                    //出勤
                    Glide.with(customView)
                            .load(imageURI_2)
                            .into(imageView);
                }

            }
        });



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
