package com.pruse.dita.cleanup;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class EditUsersActivity extends Activity {
    EditText txtUsername,txtPassword, txtUsersName;
    Button btnAddData;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_users);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        txtUsername = findViewById(R.id.editText_name);
        txtPassword = findViewById(R.id.editText_password);
        txtUsersName = findViewById(R.id.txtUsersName);
        btnAddData = findViewById(R.id.button_add);

        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });
    }

    public void addUser(){
        final String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();
        final String usersName = txtUsersName.getText().toString();

        if (username.isEmpty()){
            txtUsername.setError(getString(R.string.badEmail));
            txtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()){
            txtPassword.setError(getString(R.string.badPass));
            txtPassword.requestFocus();
            return;
        }
        if (password.length() < 6){
            txtPassword.setError(getString(R.string.badPassShort));
            txtPassword.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), getString(R.string.userAdded),  Toast.LENGTH_SHORT).show();
                        mDatabase.child("userData").child(username.replace(".","-")).child("balance").setValue(0);
                        if(usersName == "" || usersName == null){
                            int random = new Random().nextInt();
                            mDatabase.child("userData").child(username.replace(".","-")).child("usersName").setValue("user"+random);
                        }else{
                            mDatabase.child("userData").child(username.replace(".","-")).child("usersName").setValue(usersName);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.userFailed),  Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}