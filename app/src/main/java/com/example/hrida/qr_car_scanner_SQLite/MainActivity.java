package com.example.hrida.qr_car_scanner_SQLite;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.app.AlertDialog.Builder;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    EditText ID,FName,LName,CarModule,CheckUp;
    Button Add,Delete,View,ViewAll,Scan,Modify;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ID = (EditText) findViewById(R.id.editID);
        FName = (EditText) findViewById(R.id.editFName);
        LName = (EditText) findViewById(R.id.editLName);
        CarModule = (EditText) findViewById(R.id.editCar);
        CheckUp = (EditText) findViewById(R.id.editLastCheck);

        Add = (Button) findViewById(R.id.btnAdd);
        Delete = (Button) findViewById(R.id.btnDelete);
        View = (Button) findViewById(R.id.btnView);
        ViewAll = (Button) findViewById(R.id.btnViewAll);
        Scan = (Button) findViewById(R.id.btnScan);
        Modify = (Button) findViewById(R.id.btnModify);

        Add.setOnClickListener(this);
        Delete.setOnClickListener(this);
        View.setOnClickListener(this);
        ViewAll.setOnClickListener(this);
        Scan.setOnClickListener(this);
        Modify.setOnClickListener(this);

        db = openOrCreateDatabase("CarDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS car(ID VARCHAR, FName VARCHAR, LNAME VARCHAR, CAR_MODULE VARCHAR, DATE_CHECKUP VARCHAR);");
    }

    public void showMessage(String title, String message){
        Builder builder = new Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private void clearText(){
        ID.setText("");
        FName.setText("");
        LName.setText("");
        CarModule.setText("");
        CheckUp.setText("");
        ID.requestFocus();
    }

    public void onClick(View v) {

        String QuerySelectID = "SELECT * FROM car where ID='"+ID.getText()+"'",
                QueryDeleteID = "DELETE FROM car WHERE ID='"+ ID.getText()+"'",
                QueryInsert = "INSERT INTO car VALUES('"+ID.getText()+"','"+FName.getText()+"','"+LName.getText()+"','"+CarModule.getText()+"','"+CheckUp.getText()+"');",
                QuerySelectAll ="SELECT * FROM car",
                QueryUpdate ="UPDATE car SET DATE_CHECKUP='"+CheckUp.getText()+"'WHERE ID='"+ID.getText()+"'";



        if(v == Add)
        {
            Cursor c = db.rawQuery(QuerySelectID,null );
            if(c.getCount()>0)
            {
                showMessage("Error : User Already Registered ", "The username is already registered");
                return;
            }
            if(ID.getText().toString().trim().length() == 0
                    || FName.getText().toString().trim().length() == 0
                    || LName.getText().toString().trim().length() == 0
                    || CarModule.getText().toString().trim().length() == 0
                    || CheckUp.getText().toString().trim().length() == 0)
            {
                showMessage("Error", "Please enter all values");
                return;
            }
            db.execSQL(QueryInsert);
            showMessage("Success", "Record added");
            clearText();
        }
        if(v == ViewAll)
        {
            Cursor c = db.rawQuery(QuerySelectAll, null);
            if(c.getCount() == 0)
            {
                showMessage("Error", "No records found");
                return;
            }
            StringBuffer buffer = new StringBuffer();
            while(c.moveToNext())
            {
                buffer.append("ID: "+c.getString(0)+"\n");
                buffer.append("FName: "+c.getString(1)+"\n");
                buffer.append("LName: "+c.getString(2)+"\n");
                buffer.append("Car Module: "+c.getString(3)+"\n");
                buffer.append("Checkup Date: "+c.getString(4)+"\n\n");
            }
            showMessage("Car Details", buffer.toString());
        }
        if(v == View)
        {
            if(ID.getText().toString().trim().length() == 0)
            {
                showMessage("Error", "Please enter ID");
                return;
            }
            Cursor c = db.rawQuery(QuerySelectID, null);
            if(c.moveToFirst())
            {
                FName.setText(c.getString(1));
                LName.setText(c.getString(2));
                CarModule.setText(c.getString(3));
                CheckUp.setText(c.getString(4));
            }
            else
            {
                showMessage("Error", "Invalid ID");
                clearText();
            }
        }
        if(v == Delete)
        {
            if(ID.getText().toString().trim().length() == 0)
            {
                showMessage("Error", "Please enter ID");
                return;
            }
            Cursor c = db.rawQuery(QuerySelectID, null);
            if(c.getCount() == 0)
            {
                showMessage("Error", "No records found");
                return;
            }
            if(c.moveToFirst())
            {
                db.execSQL(QueryDeleteID);
                showMessage("Success", "Record Deleted");
            }
            else
            {
                showMessage("Error", "Invalid ID");
                clearText();
            }
        }
        if(v == Scan)
        {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setPrompt("Scan");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
        }
        if(v == Modify)
        {
            if(ID.getText().toString().trim().length() == 0)
            {
                showMessage("Error", "Please enter ID");
                return;
            }
            Cursor c = db.rawQuery(QuerySelectID, null);
            if(c.moveToFirst())
            {
                db.execSQL(QueryUpdate);
                showMessage("Success", "Record Modified");
            }
            else
            {
                showMessage("Error", "Invalid ID");
            }
            clearText();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                String sentence = result.getContents();
                String[] words = sentence.split(" ");
                ID.setText(words[0]);
                FName.setText(words[1]);
                LName.setText(words[2]);
                CarModule.setText(words[3]);
                CheckUp.setText(words[4]);
                Toast.makeText(this, "Success",Toast.LENGTH_LONG).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
