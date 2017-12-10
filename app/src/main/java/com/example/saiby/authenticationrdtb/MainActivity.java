package com.example.saiby.authenticationrdtb;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int SIGN_IN_CODE =666 ;
    private ImageView ivPhoto;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvID;
    private Button btnLogout;
    String mCode;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    /////////////////
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private ListView database_list_view;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView database_list_view_2;
    private ArrayList<String> arrayList2 = new ArrayList<>();
    private ArrayAdapter<String> adapter2;
    String nametext;
    String textData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPhoto =(ImageView)findViewById(R.id.iv_photo);
        tvName = (TextView)findViewById(R.id.tv_name);
        tvEmail = (TextView)findViewById(R.id.tv_email);
        tvID =(TextView)findViewById(R.id.tv_id);
        btnLogout = (Button)findViewById(R.id.btn_logout);


        //////////////////////

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrayList);
        database_list_view = (ListView)findViewById(R.id.tv_showname);
        database_list_view.setAdapter(adapter);
        database_list_view.setVisibility(View.VISIBLE);

        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrayList2);
        database_list_view_2 = (ListView)findViewById(R.id.tv_showtext);
        database_list_view_2.setAdapter(adapter2);
        database_list_view_2.setVisibility(View.VISIBLE);

        //////////////////////


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        firebaseAuth =FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    setUserData(user);


                }else{
                    GoLogInScrean();
                }
            }
        };



    }

    public void UpdateButton(View v){
        arrayList2.clear();
        arrayList.clear();
        updateName();
        database_list_view.setVisibility(View.VISIBLE);
        database_list_view_2.setVisibility(View.VISIBLE);
    }

    public void updateName(){

        mRootRef.child("codes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot shopSnapshot: dataSnapshot.getChildren()) {

                    final String codeId = shopSnapshot.getKey().toString();

                    mRootRef.child("records").child(codeId).child("Name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            nametext = dataSnapshot.getValue().toString();
                            arrayList.add("Name = " + nametext );
                            adapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
                    mRootRef.child("records").child(codeId).child("Text").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            textData = dataSnapshot.getValue().toString();
                            arrayList2.add("Message = " + textData );
                            adapter2.notifyDataSetChanged();

                        }
                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    public void saveText(View view){

        database_list_view.setVisibility(View.GONE);
        database_list_view_2.setVisibility(View.GONE);
        EditText inputName = (EditText) findViewById(R.id.et_user);
        EditText inputText = (EditText) findViewById(R.id.et_message);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String stringName = inputName.getText().toString();
        String stringText = inputText.getText().toString();
        String stringCode = user.getUid().toString();

        if(stringName.length()==0){
            stringName = "No Name";
        }
        if(stringText.length()==0){
            stringText = "Empty";
        }
        DatabaseReference mNameRef = mRootRef.child("records").child(stringCode).child("Name");
        DatabaseReference mMessageRef = mRootRef.child("records").child(stringCode).child("Text");
        DatabaseReference mCodeId = mRootRef.child("codes").child(stringCode);
        mCodeId.setValue(stringCode+"");
        mNameRef.setValue(stringName+"");
        mMessageRef.setValue(stringText+"");

    }


    private void setUserData(FirebaseUser user) {
        tvName.setText(user.getDisplayName());
        tvEmail.setText(user.getEmail());
        tvID.setText(user.getUid());
        mCode = user.getUid();
        Glide.with(this).load(user.getPhotoUrl()).into(ivPhoto);
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);

    }

    private void goMainScreen() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logOut(View view){
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    GoLogInScrean();
                }else{
                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void revoke(View view){
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    GoLogInScrean();
                }else{
                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void GoLogInScrean() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuthListener!=null){
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

}
