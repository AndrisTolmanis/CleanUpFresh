package com.pruse.dita.cleanup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class EditGoodiesActivity extends Activity {
    DatabaseHelper myDb;
    EditText editName, editPrice, editNFC, editTextId;
    Button btnAddData, btnViewAll, btnDelete, btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_goodies);

        myDb = new DatabaseHelper(this);

        editName = findViewById(R.id.editText_name);
        editPrice = findViewById(R.id.editText_password);
        editNFC = findViewById(R.id.editText_Balance);
        editTextId = findViewById(R.id.editText_id);
        btnAddData = findViewById(R.id.button_add);
        btnViewAll = findViewById(R.id.button_viewAll);
        btnUpdate= findViewById(R.id.button_edit);
        btnDelete= findViewById(R.id.button_delete);
        AddData();
        viewAll();
        UpdateData();
        DeleteData();
    }

    //atpakaļ poga
    public void goBack(View view) {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //metode, lai dzēstu datubāzes ierakstu pēc norādītā ID
    public void DeleteData() {
        btnDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer deletedRows = myDb.deleteData2(editTextId.getText().toString());
                        if(deletedRows > 0)
                            Toast.makeText(EditGoodiesActivity.this,getString(R.string.delete_message),Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(EditGoodiesActivity.this,getString(R.string.delete_message2),Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    //metode, lai atjaunotu datubāzes ierakstu pēc norādītā ID
    public void UpdateData() {
        btnUpdate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isUpdate = myDb.updateData2(
                                editTextId.getText().toString(),
                                editName.getText().toString(),
                                Integer.parseInt(editPrice.getText().toString()),
                                editNFC.getText().toString());
                        if(isUpdate)
                            Toast.makeText(EditGoodiesActivity.this,getString(R.string.update_message),Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(EditGoodiesActivity.this,getString(R.string.update_message2),Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    //metode, lai pievienotu jaunu ieraktu DB, ar norādītajiem parametriem
    public  void AddData() {
        btnAddData.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isInserted = myDb.insertData2(
                                editName.getText().toString(),
                                Integer.parseInt(editPrice.getText().toString()),
                                editNFC.getText().toString()) ;
                        if(isInserted)
                            Toast.makeText(EditGoodiesActivity.this,getString(R.string.add_message),Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(EditGoodiesActivity.this,getString(R.string.add_message2),Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    //metode, lai apskatītu visus DB ierakstus
    public void viewAll() {
        btnViewAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDb.getAllGoodies();
                        if(res.getCount() == 0) {
                            // show message
                            showMessage(getString(R.string.error),getString(R.string.empty_db));
                            return;
                        }

                        StringBuilder allUsers = new StringBuilder();
                        while (res.moveToNext()) {
                            allUsers.append(getString(R.string.id)+" :"+ res.getString(0)+"\n");
                            allUsers.append(getString(R.string.name)+" :"+ res.getString(1)+"\n");
                            allUsers.append(getString(R.string.price)+" :"+ res.getString(2)+"\n");
                            allUsers.append(getString(R.string.nfc)+" :"+ res.getString(3)+"\n\n");
                        }

                        // Show all data
                        showMessage(getString(R.string.goodies),allUsers.toString());
                    }
                }
        );
    }

    public void showMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
}