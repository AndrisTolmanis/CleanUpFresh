package com.pruse.dita.cleanup;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class AdminLogin extends Activity {
    EditText editName, editPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_admin_login);
        editName = findViewById(R.id.editText_name);
        editPassword = findViewById(R.id.editText_password);
    }

    //poga "Pieslēgties", kur to var tikai admins
    public void loginAsAdmin(View view) {
        String name = editName.getText().toString();
        String password = editPassword.getText().toString();
        if (name.equals("admin")&&password.equals("admin")) {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Toast.makeText(AdminLogin.this,getString(R.string.is_admin),Toast.LENGTH_LONG).show();
        }
    }

    //poga "Atpakaļ"
    public void goBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}