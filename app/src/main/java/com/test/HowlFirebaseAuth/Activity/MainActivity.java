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
import com.test.HowlFirebaseAuth.ValueObject.Member;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 10;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;

    // FIXME: editTextEmail -> mEditTextEmail
    private EditText mEditTextEmail;
    // FIXME: editTextPassword -> mEditTextPassword
    private EditText mEditTextPassword;

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


        // TODO: 昨日お伝えしましたが、DataBindingLibraryを使うとfindViewByIdが要らなくなるので使った方がよいです
        // (修正は必須ではないです)
        mEditTextEmail = (EditText) findViewById(R.id.editText_email);
        mEditTextPassword = (EditText) findViewById(R.id.editText_password);

        Button emailLoginButton = (Button) findViewById(R.id.email_login_button);
        emailLoginButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                createUser(mEditTextEmail.getText().toString(), mEditTextPassword.getText().toString());
            }
        });


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


        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //FirebaseDBからMemberObjectValueを全部引き出す。
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                mFirebaseDatabase.getReference().child("members").addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        memberList.clear();
                        for (DataSnapshot snapShot : dataSnapshot.getChildren()) {
                            Member member = snapShot.getValue(Member.class);
                            memberList.add(member);
                        }

                        //Loginをしたのか、まず確認。
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(user != null){
                            //認証した状態

                            //自分のEmailでMemberObjectを探す
                            Member searchMember = null;
                            for (Member member : memberList) {
                                if (member.getMemberEmail().equals(user.getEmail())) {
                                    searchMember = member;
                                    break;
                                }
                            }

                            if(searchMember != null){
                                //すでに登録した場合、Move HomeActivity
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish(); //현재 Activity 사라짐*/
                            }else{
                                //登録しなった場合、Move MemberActivity
                                Intent intent = new Intent(MainActivity.this, MemberActivity.class);
                                startActivity(intent);
                                finish(); //현재 Activity 사라짐
                            }

                        }else{
                            //認証していない状態

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });




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

    // FIXME: ここもコメントを日本語にして欲しいです
    private void createUser(final String email, final String password){
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loginUser(email, password);
                        } else {
                            // FIXME: 失敗しているのに「Register Successfully」は不適切です
                            // If sign in fails, display a message to the user.
                        }

                        // ...
                    }
                });
    }

    private void loginUser(String email, String password){
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                    } else {
                        // FIXME: 失敗しているのに「Login Successfully」は不適切です
                    }

                    // ...
                }
            });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Complete", Toast.LENGTH_LONG).show();
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