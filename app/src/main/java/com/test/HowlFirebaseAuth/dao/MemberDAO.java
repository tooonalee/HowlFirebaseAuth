package com.test.HowlFirebaseAuth.dao;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.test.HowlFirebaseAuth.Activity.HomeActivity;
import com.test.HowlFirebaseAuth.Activity.MainActivity;
import com.test.HowlFirebaseAuth.Activity.MemberActivity;
import com.test.HowlFirebaseAuth.Utility.Singleton;
import com.test.HowlFirebaseAuth.ValueObject.Member;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

/**
 * Created by admin on 2017/10/10.
 */

public class MemberDAO {

    private static Member returnMember;
    private static MemberDAO _shared;
    public MemberDAO(){}

    private static MemberDAO getInstance(){
        if(_shared == null)
            _shared = new MemberDAO();
        return _shared;
    }

    public Member selectMemberByEmail(String email){
        Member searchMember = null;

        try{
            loadModelWithDataFromFirebase(email);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        Log.d("returnMember :  ", returnMember.getMemberEmail());

        return returnMember;
    }

    public void loadModelWithDataFromFirebase(final String email) throws InterruptedException{
        FirebaseDatabase mFirebaseDatabase;
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mFirebaseDatabase.getReference().child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Member searchMember = null;
                ArrayList<Member> memberList = new ArrayList<>();

                memberList.clear();
                for (DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    Member member = snapShot.getValue(Member.class);
                    memberList.add(member);
                }

                //自分のEmailでMemberObjectを探す
                for (Member member : memberList) {
                    if (member.getMemberEmail().equals(email)) {
                        Log.d("More", member.getMemberEmail());
                        MemberDAO.getInstance().returnMember = member;
                        Singleton.getInstance().connectedMember = member;
                        break;
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


}
