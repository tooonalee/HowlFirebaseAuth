package com.test.HowlFirebaseAuth.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
// FIXME: 不要なインポート文は消してください
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.test.HowlFirebaseAuth.R;
import com.test.HowlFirebaseAuth.Utility.ProgressDialogTask;
import com.test.HowlFirebaseAuth.Utility.Singleton;
import com.test.HowlFirebaseAuth.ValueObject.Member;
import com.test.HowlFirebaseAuth.dao.MemberDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 10;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;

    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;
    private FirebaseDatabase mFirebaseDatabase;

    //--------------------------------------------------
    // FirebaseDB DAO
    //--------------------------------------------------
    private List<Member> memberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FIXME: このコメントを日本語で書いて欲しいです
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // FIXME: mGoogleApiClientはgoogleLoginButton.setOnClickListenerの中でしか使っていないようなので、
        // ローカル変数化した方が良いかと思います
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // FIXME: ここのコメントは現実装と合っていないようなので消した方が良いです
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();


        SignInButton googleLoginButton = (SignInButton) findViewById(R.id.login_button);
        googleLoginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });



        // FIXME: ここもコメントを日本語にして欲しいです
        //FirebaseAuthListener
        //로그인 상태 변화에 응답한다.
        //onStart, onStop 시작하면 리스너를 달아주고 끝나면 리스너를 뗀다.


        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //自分のEmailで
                //Member searchMember = MemberDAO.getInstance().selectMemberByEmail(firebaseAuth.getCurrentUser().getEmail());
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

                        if(memberList.isEmpty() || FirebaseAuth.getInstance().getCurrentUser().getEmail() == null ){
                            return;
                        }
                        //自分のEmailでMemberObjectを探す
                        for (Member member : memberList) {
                            if (member.getMemberEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                Log.d("More", member.getMemberEmail());
                                MemberDAO.getInstance().returnMember = member;
                                Singleton.getInstance().connectedMember = member;
                                break;
                            }
                        }

                        if(searchMember != null){
                            //あるなら
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            //ないなら

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



/*                Intent intent1 = new Intent(MainActivity.this, CheckActivity.class);
                startActivity(intent1);
                finish();*/


            }
        };

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // FIXME: ここもコメントを日本語にして欲しいです
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mFirebaseDatabase.getReference().child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Member searchMember = null;
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

                                    // Sign in success, update UI with the signed-in user's information
                                    if(searchMember != null){
                                        Toast.makeText(MainActivity.this, "Go to HomeActivity", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish(); //현재 Activity 사라짐*/
                                    }else{
                                        Member member = new Member();
                                        member.setMemberEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                        member.setWorkingFlag(false);
                                        mFirebaseDatabase.getReference().child("members").push().setValue(member).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MainActivity.this, "Go to HomeActivity", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                                finish(); //현재 Activity 사라짐*/
                                            }
                                        });
                                    }

                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onStart(){
        // FIXME: ここもコメントを日本語にして欲しいです
        //시작할 때 귀를 달아주고
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthListener);
    }

    @Override
    public void onStop(){
        // FIXME: ここもコメントを日本語にして欲しいです
        // 끝나면 귀를 떼는...
        super.onStop();
        if(mFirebaseAuthListener != null){
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthListener);
        }
    }
}
