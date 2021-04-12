package com.example.androidbeginning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class DriverRegisterActivity extends AppCompatActivity {

    private Button LoginDriverBtn;
    private Button RegisterDriverBtn;
    private TextView LoginDriverText;
    private TextView DontHaveAnAccount;
    private EditText EmailDriver;
    private EditText PasswordDriver;
    private ProgressDialog loading;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        RegisterDriverBtn=(Button) findViewById(R.id.register_btn_driver);
        LoginDriverBtn=(Button)findViewById(R.id.driver_login_btn);
        LoginDriverText=(TextView)findViewById(R.id.login_text_driver);
        DontHaveAnAccount=(TextView)findViewById(R.id.dont_hava_an_acc);
        EmailDriver=(EditText)findViewById(R.id.email_driver);
        PasswordDriver=(EditText)findViewById(R.id.password_driver);

        mAuth=FirebaseAuth.getInstance();
        loading= new ProgressDialog(this);

        RegisterDriverBtn.setVisibility(View.INVISIBLE);
        RegisterDriverBtn.setEnabled(false);

        DontHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginDriverBtn.setVisibility(View.INVISIBLE);
                DontHaveAnAccount.setVisibility(View.INVISIBLE);
                LoginDriverText.setText("Driver Registration");

                RegisterDriverBtn.setVisibility(View.VISIBLE);
                RegisterDriverBtn.setEnabled(true);

            }
        });


        RegisterDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailDriver.getText().toString();
                String password=PasswordDriver.getText().toString();

                RegisterDriver(email,password);
            }
        });

        LoginDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailDriver.getText().toString();
                String password=PasswordDriver.getText().toString();

                LoginDriver(email,password);
            }
        });

    }

    private void LoginDriver(String email,String password)
    {
        if (TextUtils.isEmpty(email))
            Toast.makeText(DriverRegisterActivity.this, "Please write Email", Toast.LENGTH_SHORT).show();

        else if (TextUtils.isEmpty(password))
            Toast.makeText(DriverRegisterActivity.this, "Please write Password", Toast.LENGTH_SHORT).show();

        else
        {
            loading.setTitle("Driver Login");
            loading.setMessage("Please wait while we verify your credentials");
            loading.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(DriverRegisterActivity.this, "Driver Login Successful", Toast.LENGTH_SHORT).show();
                        loading.dismiss();

                        Intent DriverIntent=new Intent(DriverRegisterActivity.this,DriverMapActivity.class);
                        startActivity(DriverIntent);
                    }

                    else
                    {
                        Toast.makeText(DriverRegisterActivity.this, "Driver Login Unsuccessful. Please Try again", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
            });
        }
    }

    private void RegisterDriver(String email,String password) {
        if (TextUtils.isEmpty(email))
            Toast.makeText(DriverRegisterActivity.this, "Please write Email", Toast.LENGTH_SHORT).show();

        else if (TextUtils.isEmpty(password))
            Toast.makeText(DriverRegisterActivity.this, "Please write Password", Toast.LENGTH_SHORT).show();

        else
        {
            loading.setTitle("Driver Registration");
            loading.setMessage("Please wait while we register your data");
            loading.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(DriverRegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        loading.dismiss();

                        Intent DriverIntent=new Intent(DriverRegisterActivity.this,DriverMapActivity.class);
                        startActivity(DriverIntent);
                    }

                    else
                    {
                        Toast.makeText(DriverRegisterActivity.this, "Registration Unsuccessful. Please Try again", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
            });
        }
    }
}