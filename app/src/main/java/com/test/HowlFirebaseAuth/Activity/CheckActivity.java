package com.test.HowlFirebaseAuth.Activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.HowlFirebaseAuth.R;
import com.test.HowlFirebaseAuth.Utility.FirebaseImageLoader;
import com.test.HowlFirebaseAuth.Utility.ProgressDialogTask;
import com.test.HowlFirebaseAuth.Utility.Singleton;
import com.test.HowlFirebaseAuth.ValueObject.ImageDTO;
import com.test.HowlFirebaseAuth.ValueObject.Member;
import com.test.HowlFirebaseAuth.ValueObject.WorkInfo;
import com.test.HowlFirebaseAuth.dao.MemberDAO;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TimePicker timePicker;

    private Button checkInOutButton;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog.Builder imageDialogBuilder;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        initFirebaseDB();

        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null) //2번째 인자값이 this인 이유는 GoogleApiClient.OnConnectionFailedListener 상속
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();

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



    } //onCreate



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
                    checkInOutButton.setText("CHECK OUT");
                    checkInOutButton.setBackgroundColor(Color.parseColor("#33B5E5"));
                }else{
                    checkInOutButton.setText("CHECK IN");
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
                checkInOutButton.setText("CHECK IN");
                checkInOutButton.setBackgroundColor(Color.parseColor("#FF4081"));
                //33B5E5 FF4081

                //退勤のボタンを活性化

            }else{
                //出勤プロセス
                searchMember.setWorkingFlag(true);
                checkInOutButton.setText("CHECK OUT");
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
        alertDialogBuilder = new AlertDialog.Builder(CheckActivity.this);
        if(searchMember.isWorkingFlag()){
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
        currentDate = new Date();
        currentDate.setYear(year);
        currentDate.setMonth(month);
        currentDate.setDate(day);
        currentDate.setHours(hour);
        currentDate.setMinutes(minute);

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
                factory = LayoutInflater.from(CheckActivity.this);
                customView = factory.inflate(R.layout.dialog_custom, null);
                imageView = (ImageView) customView.findViewById(R.id.dialog_imageView);

                Uri imageURI_1 = Uri.parse("https://firebasestorage.googleapis.com/v0/b/howlfirebaseauth-24336.appspot.com/o/imageList%2FcheckIn%2Fdog.jpg?alt=media&token=07a4a407-2d8c-412b-8d39-ce151482e33b");
                Uri imageURI_2 = Uri.parse("https://firebasestorage.googleapis.com/v0/b/howlfirebaseauth-24336.appspot.com/o/imageList%2FcheckOut%2Fdog2.jpg?alt=media&token=f042b32f-69d9-4ac7-ae13-955a04f3bcca");
                //imageView.setImageURI(tmpURI);

                Log.d("URI", uri.getPath());

                imageDialogBuilder = new AlertDialog.Builder(CheckActivity.this);
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


}
