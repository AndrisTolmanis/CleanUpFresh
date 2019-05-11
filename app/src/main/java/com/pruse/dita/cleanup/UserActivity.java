package com.pruse.dita.cleanup;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.service.autofill.UserData;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.widget.TextView.BufferType.EDITABLE;

public class UserActivity extends Activity {

    String user_id,user_name, username, password, tempName, nfc = "", goodie_code = "";
    int balance;
    int tempPrice = 0;
    // BEF9A755  MAR60TS

    // NFC taga adapteris
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    // Meklē tagu
    boolean nfcSearch = false;
    // Uztvertā taga dati
    Tag nfcTag;
    // Taga bitu virkne
    byte[] nfcTagID;
    // Taga hex vērtība
    String nfcTagSerialNumber;
    // Kontextx
    Context context;

    // APAKŠĒJĀS POGAS
    //teksti, kas parādīsies spiežot uz pogām
    TextView question;
    TextView goodies;
    TextView top;
    TextView info;

    //apakšējās pogas
    Button ok;
    Button btnNfc;
    Button btnShowGoodies;
    Button btnShowTop;
    Button btnShowInfo;

    // Firebase mainīgie
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    int usersBalance = 0;
    TextView txtBalance;
    Users userdata = new Users();
    List<RewardsData> rewards = new ArrayList<>();
    List<Users> usersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;

        // Pārbauda vai ir NFC telefonam
        if (nfcAdapter == null) {
            Toast.makeText(this, "Ierīcei nav NFC! Tas ir nepieceišams.", Toast.LENGTH_LONG).show();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.NFC)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Pieslēdzoties saņem lietotāja vārdu
        Intent intent = getIntent();
        user_id = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        user_name = user_id.replace(".","-");

        txtBalance = findViewById(R.id.txtBalance);

        // DB pieprasījums lai atgriestu lietotāja datus
        DatabaseReference ref = dbRef.child("userData").child(user_name);
        Query query = ref;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
                userdata = dataSnapshot.getValue(Users.class);
                balance = userdata.getBalance();
                txtBalance.setText(Integer.toString(balance));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // DB pieprasījums lai atgriestu apbalvojumu datus
        ref = dbRef.child("rewards");
        query = ref;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    RewardsData rews = ds.getValue(RewardsData.class);
                    rewards.add(rews);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // DB pieprasījums lai atgristu visu lietotāju datus
        ref = dbRef.child("userData");
        query = ref.orderByChild("balance");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Users user = ds.getValue(Users.class);
                    usersList.add(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        //te sākas apakšējās pogas
        //teksti, kas parādīsies spiežot uz pogām
        question = findViewById(R.id.editText_question);
        goodies = findViewById(R.id.editText_goodies);
        top = findViewById(R.id.editText_top);
        info = findViewById(R.id.editText_info);

        //apakšējās pogas
        ok = findViewById(R.id.button_ok);
        btnNfc = findViewById(R.id.button_NFC);
        btnShowGoodies = findViewById(R.id.button_showGoodies);
        btnShowTop = findViewById(R.id.button_showTop);
        btnShowInfo = findViewById(R.id.button_showInfo);

        //gadījumam, ja teksts ir garāks, pievienota iespēja "tīt" uz leju
        question.setMovementMethod(new ScrollingMovementMethod());
        top.setMovementMethod(new ScrollingMovementMethod());
        goodies.setMovementMethod(new ScrollingMovementMethod());
        info.setMovementMethod(new ScrollingMovementMethod());

        //sākumā visi teksi nav redzami
        question.setVisibility(View.GONE);
        goodies.setVisibility(View.GONE);
        top.setVisibility(View.GONE);
        info.setVisibility(View.GONE);
        ok.setVisibility(View.GONE);

        //poga "NFC"
        btnNfc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (nfcAdapter != null) {
                    nfcSearch = true;// Ļauj lietotnei reaģēt kad nolasakādu nfc // <--------  one NFC stuff here
                    question.setText(getString(R.string.nfc_waiting));
                    question.setVisibility(View.VISIBLE);
                    goodies.setVisibility(View.GONE);
                    top.setVisibility(View.GONE);
                    info.setVisibility(View.GONE);
                    ok.setVisibility(View.GONE);
                }
                else{
                    question.setText(getString(R.string.nfc_NoNfcOnDevice));
                    question.setVisibility(View.VISIBLE);
                    goodies.setVisibility(View.GONE);
                    top.setVisibility(View.GONE);
                    info.setVisibility(View.GONE);
                    ok.setVisibility(View.GONE);
                }
            }
        });

        //poga "Labi"
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (balance >= tempPrice) {
                     balance = balance-tempPrice;
//                    myDb.updateData(user_id, username, password, balance);
                    dbRef.child("userData").child(user_name).child("balance").setValue(balance);
                    question.setText(getString(R.string.goodie_code)+goodie_code);
                    // Atiestata vērtības
                    nfc = "";
                    tempName = "";
                    tempPrice = 0;
                } else {
                    question.setText(getString(R.string.expensive));
                }
                ok.setVisibility(View.GONE);
                txtBalance.setText(String.valueOf(balance));
            }
        });

        //poga "Balvas"
        btnShowGoodies.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //saglabā apbalvojums vērtību un nosaukumu
                StringBuilder allGoodies = new StringBuilder();
                for (RewardsData reward : rewards){
                    allGoodies.append(reward.getCost() + " punkti: " + reward.getName() +"\n\n");
                }
                goodies.setText(allGoodies.toString());

                question.setVisibility(View.GONE);
                ok.setVisibility(View.GONE);
                goodies.setVisibility(View.VISIBLE);
                top.setVisibility(View.GONE);
                info.setVisibility(View.GONE);
            }
        });

        //poga "Tops"
        btnShowTop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StringBuilder topUsers = new StringBuilder();
                topUsers.setLength(0);
                int i=0;
                for(int ii=usersList.size()-1; ii>=0; ii--){
                    i++;
                    topUsers.append(i+ ". "+ usersList.get(ii).getBalance() + " - " + usersList.get(ii).getUsersName() +"\n\n");
                }
                top.setText(topUsers.toString());

                question.setVisibility(View.GONE);
                ok.setVisibility(View.GONE);
                goodies.setVisibility(View.GONE);
                top.setVisibility(View.VISIBLE);
                info.setVisibility(View.GONE);
            }
        });

        //poga "Info"
        btnShowInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                question.setVisibility(View.GONE);
                ok.setVisibility(View.GONE);
                goodies.setVisibility(View.GONE);
                top.setVisibility(View.GONE);
                info.setVisibility(View.VISIBLE);
            }
        });
    }

    // metode priekš pogas "Atslēgties"
    public void goBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Nfc magic
    @Override
    protected void onNewIntent(Intent intent){
        if (nfcAdapter != null) {
            String action = intent.getAction();
            if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) && nfcSearch == true){
                getTagInfo(intent);
                nfc = nfcTagSerialNumber;
                nfcSearch = false;
                //nolasa visus datus atbilsotši getAllGoodies()

                for (RewardsData reward : rewards){
                    if(nfc.equals(reward.getNfcCode())){
                        tempName = reward.getName();
                        tempPrice = reward.getCost();
                        break;
                    }
                }

                //ja nolasīts apbalvojums
                if (tempPrice > 0 ) {
                    question.setText(getString(R.string.nfc_goodie) + tempName + "\n" + getString(R.string.nfc_points) + tempPrice);
                    ok.setVisibility(View.VISIBLE);
                } else {
                    question.setText(getString(R.string.nfc_fail));
                }

                question.setVisibility(View.VISIBLE);
                goodies.setVisibility(View.GONE);
                top.setVisibility(View.GONE);
                info.setVisibility(View.GONE);
            }
        }
    }

    private void getTagInfo(Intent intent) {
        if (nfcAdapter != null) {
            nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfcTagID = nfcTag.getId();
            nfcTagSerialNumber = bytesToHex(nfcTagID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
// Apbalvojum datu modelis
class RewardsData{
    private String nfcCode;
    private String name;
    private int cost;

    public RewardsData(){}
    public RewardsData(String nfcCode, String name, int cost){
        this.nfcCode = nfcCode;
        this.name = name;
        this.cost = cost;
    }

    public String getName(){
        return name;
    }
    public String getNfcCode(){
        return nfcCode;
    }
    public int getCost(){
        return cost;
    }
}
// Lietotāju datu modelis
class Users{
    private String usersName;
    private int balance;

    public String getUsersName(){
        return usersName;
    }
    public int getBalance(){
        return balance;
    }
}
