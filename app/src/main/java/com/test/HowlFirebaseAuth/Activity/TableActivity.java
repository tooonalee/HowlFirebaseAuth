package com.test.HowlFirebaseAuth.Activity;

import android.content.Context;
import android.graphics.Color;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.design.widget.NavigationView;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.test.HowlFirebaseAuth.R;
import com.test.HowlFirebaseAuth.Utility.CustomDocumentPrintAdapter;
import com.test.HowlFirebaseAuth.ValueObject.WorkInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TableActivity extends AppCompatActivity {
    private static final String TABLE_HEADER_NUM = "NUM";
    private static final String TABLE_HEADER_NAME = "名前";
    private static final String TABLE_HEADER_ONWORKDATE = "出勤時間";
    private static final String TABLE_HEADER_OFFWORKDATE = "退勤時間";
    private static final String TABLE_HEADER_WORKINGTIME = "勤務時間";

    private FirebaseDatabase mFirebaseDatabase;

    private float mx, my;
    private float curX, curY;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;

    private TableLayout tableLayout;
    private List<WorkInfo> workInfoList = new ArrayList<>();

    private Button printButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        vScroll = (ScrollView) findViewById(R.id.vScroll);
        hScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
        printButton = (Button) findViewById(R.id.print_Button);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPrint();
            }
        });

        tableLayout = (TableLayout) findViewById(R.id.tableLayout);


        mFirebaseDatabase.getReference().child("workinfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                workInfoList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    WorkInfo workInfo = snapshot.getValue(WorkInfo.class);
                    workInfoList.add(workInfo);
                }
                updateUI();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    public void updateUI(){
        TableRow.LayoutParams rowSpanLayout = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1);
        rowSpanLayout.span = 1;

        TableRow headerRow = new TableRow(this);
        TableRow contentRow[] = new TableRow[workInfoList.size()];

        TextView headerTextView[] = new TextView[5];
        TextView contentTextView[][] = new TextView[workInfoList.size()][5];

        //textView Instance
        for(int i=0; i < 5; i++){
            headerTextView[i] = new TextView(this);
            headerTextView[i].setGravity(Gravity.CENTER);
            headerTextView[i].setPadding(15,5,15,5);
        }
        //Table Header Title
        headerTextView[0].setText(TABLE_HEADER_NUM);
        headerTextView[1].setText(TABLE_HEADER_NAME);
        headerTextView[2].setText(TABLE_HEADER_ONWORKDATE);
        headerTextView[3].setText(TABLE_HEADER_OFFWORKDATE);
        headerTextView[4].setText(TABLE_HEADER_WORKINGTIME);
        for(int i=0; i < headerTextView.length; i++){
            headerRow.addView(headerTextView[i], rowSpanLayout);
        }

        //ContentRow, ContentTextView getInstance
        for(int i=0; i < workInfoList.size() ; i++){
            contentRow[i] = new TableRow(this);
        }
        //Cell内容のTextViewをセッティング、もし10時間が過ぎたCellの部分は赤く表示する。
        for(int i=0; i < workInfoList.size() ; i++){
            for(int j=0;j<5;j++){
                contentTextView[i][j] = new TextView(this);
                contentTextView[i][j].setGravity(Gravity.CENTER);
                contentTextView[i][j].setPadding(15,5,15,5);
                if(j==4 && workInfoList.get(i).getWorkingTime() >= 10){
                    contentTextView[i][j].setTextColor(Color.RED);
                }
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd(E) HH:mm", new Locale("en", "US"));

        try{
            for(int i=0;i<workInfoList.size();i++){
                contentTextView[i][0].setText(""+(i+1));
                contentTextView[i][1].setText(workInfoList.get(i).getName());
                contentTextView[i][2].setText(dateFormat.format(workInfoList.get(i).getCreateOnWorkDate()) );
                contentTextView[i][3].setText(dateFormat.format(workInfoList.get(i).getCreateOffWorkDate()) );
                contentTextView[i][4].setText(workInfoList.get(i).getWorkingTime()+" Hours");
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        //Table Content

        for(int i=0; i < workInfoList.size() ; i++){
            for(int j=0 ;j<5; j++){
                contentRow[i].addView(contentTextView[i][j], rowSpanLayout);
            }
        }

        //Add Row
        tableLayout.addView(headerRow);
        for(int i=0; i< workInfoList.size() ; i++){
            tableLayout.addView(contentRow[i]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX, curY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mx = event.getX();
                my = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                mx = curX;
                my = curY;
                break;
            case MotionEvent.ACTION_UP:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                break; }
        return true;
    }

    private void doPrint(){
        CustomDocumentPrintAdapter adapter =
                new CustomDocumentPrintAdapter(TableActivity.this, workInfoList);

        printWithAdapter("custom.pdf", adapter);
    }

    private void printWithAdapter(String jobName,
                                  PrintDocumentAdapter adapter) {

        if (PrintHelper.systemSupportsPrint()) {

            PrintManager printManager =
                    (PrintManager) getSystemService(Context.PRINT_SERVICE);
            printManager.print(jobName, adapter, null);

        } else {
            Toast.makeText(this,
                    "この端末では印刷をサポートしていません",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
