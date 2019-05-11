package com.pruse.dita.cleanup;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.database.Cursor;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends Activity {
    public static final String EXTRA_MESSAGE = "Tev sanāks!";
    EditText txtUsername;
    EditText txtPassword;
    ImageView hiddenFrame;
    Button btnLogin;
    private FirebaseAuth mAuth;
    int adminClicks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        txtUsername = findViewById(R.id.editText_name);
        txtPassword = findViewById(R.id.editText_password);
        hiddenFrame = findViewById(R.id.imgHidden);
        btnLogin = findViewById(R.id.button_login);

        hiddenFrame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                adminClicks++;
                if(adminClicks > 10){
                    goToAdmin();
                }
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                userLogin();
            }
        });
    }

    public void userLogin(){
        final String username = txtUsername.getText().toString().replace(" ","");
        String password = txtPassword.getText().toString();

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
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            goToUser(username);
//                            Toast.makeText(getApplicationContext(), getString(R.string.loginSuccess),  Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.loginFailed),  Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void goToUser(String username){
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(EXTRA_MESSAGE, username);
        startActivity(intent);
    }

    private void goToAdmin(){
        Intent intent = new Intent(this, AdminLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
