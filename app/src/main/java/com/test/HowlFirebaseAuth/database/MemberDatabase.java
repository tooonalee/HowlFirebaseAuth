package com.test.HowlFirebaseAuth.database;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.test.HowlFirebaseAuth.Utility.Singleton;
import com.test.HowlFirebaseAuth.ValueObject.Member;
import com.test.HowlFirebaseAuth.dao.MemberDAO;

import java.util.ArrayList;

/**
 * Created by admin on 2017/10/10.
 */

public class MemberDatabase {
    private FirebaseDatabase mFirebaseDatabase;

    public MemberDatabase(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    public void selectMemberByEmail(final String email){
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
