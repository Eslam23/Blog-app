package com.example.blog.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.blog.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText password;
    private Button createacount;
    private DatabaseReference mDataBaseRefernce;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private Uri resultUri=null;
    private StorageReference mFirebaseStorage;
    private ProgressDialog mProgressDialog;
    private ImageButton profilePicture;
    private static final int GALLERY_CODE =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        firstName =(EditText)findViewById(R.id.firstnameAct);
        lastName =(EditText)findViewById(R.id.lastnameAct);
        email =(EditText)findViewById(R.id.emailAct);
        password =(EditText)findViewById(R.id.passwordAct);
        createacount=(Button)findViewById(R.id.createacountAct);
        mDatabase =FirebaseDatabase.getInstance();
        mDataBaseRefernce =mDatabase.getReference().child("MUsers");
        mAuth =FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance().getReference().child("MBlog_profile_pics");
        mProgressDialog =new ProgressDialog(this);
        profilePicture =(ImageButton)findViewById(R.id.profilepic);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent ();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_CODE);
            }
        });
        createacount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

    }

    private void createNewAccount() {
        final String fname=firstName.getText().toString().trim();
        final  String lname=lastName.getText().toString().trim();
        String em =email.getText().toString().trim();
        String pwd =password.getText().toString().trim();
        if(!TextUtils.isEmpty(fname)&&!TextUtils.isEmpty(lname)&&!TextUtils.isEmpty(em)&&!TextUtils.isEmpty(pwd))
        {
            mProgressDialog.setMessage("Create Account.......");
            mProgressDialog.show();
            mAuth.createUserWithEmailAndPassword(em,pwd)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if(authResult!=null)
                            {
                                StorageReference imagePath =mFirebaseStorage.child("MBlog_profile_pics")
                                        .child(resultUri.getLastPathSegment());
                                imagePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String userId=mAuth.getCurrentUser().getUid();
                                        DatabaseReference currentUserDb=mDataBaseRefernce.child(userId);
                                        currentUserDb.child("firstname").setValue(fname);
                                        currentUserDb.child("lastname").setValue(lname);
                                        currentUserDb.child("image").setValue(resultUri.toString());
                                        mProgressDialog.dismiss();
                                        Intent intent =new Intent(CreateAccountActivity.this,PostListActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();

                                    }
                                });
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE&&resultCode==RESULT_OK)
        {
            Uri mImageuri =data.getData();
            CropImage.activity(mImageuri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(CreateAccountActivity.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                 resultUri = result.getUri();
                profilePicture.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
