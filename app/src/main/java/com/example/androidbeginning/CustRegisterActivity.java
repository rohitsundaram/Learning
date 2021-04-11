package com.example.androidbeginning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class CustRegisterActivity extends AppCompatActivity {
    private Button LoginCustomerBtn;
    private Button RegisterCustomerBtn;
    private TextView LoginCustomerText;
    private TextView DontHaveAnAccount;
    private EditText EmailCustomer;
    private EditText PasswordCustomer;
    private ProgressDialog loading;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cust_register);

        RegisterCustomerBtn=(Button) findViewById(R.id.register_btn_cust);
        LoginCustomerBtn=(Button)findViewById(R.id.cust_login_btn);
        LoginCustomerText=(TextView)findViewById(R.id.cust_register_text);
        DontHaveAnAccount=(TextView)findViewById(R.id.dont_have_an_acc_cust);
        EmailCustomer=(EditText)findViewById(R.id.email_cust);
        PasswordCustomer=(EditText)findViewById(R.id.password_cust);

        mAuth=FirebaseAuth.getInstance();
        loading= new ProgressDialog(this);

        RegisterCustomerBtn.setVisibility(View.INVISIBLE);
        RegisterCustomerBtn.setEnabled(false);

        DontHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginCustomerBtn.setVisibility(View.INVISIBLE);
                DontHaveAnAccount.setVisibility(View.INVISIBLE);
                LoginCustomerText.setText("Customer Registration");

                RegisterCustomerBtn.setVisibility(View.VISIBLE);
                RegisterCustomerBtn.setEnabled(true);

            }
        });


        RegisterCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailCustomer.getText().toString();
                String password=PasswordCustomer.getText().toString();

                RegisterCustomer(email,password);
            }
        });

        LoginCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailCustomer.getText().toString();
                String password=PasswordCustomer.getText().toString();

                LoginCustomer(email,password);
            }
        });

    }

    private void LoginCustomer(String email,String password)
    {
        if (TextUtils.isEmpty(email))
            Toast.makeText(CustRegisterActivity.this, "Please write Email", Toast.LENGTH_SHORT).show();

        else if (TextUtils.isEmpty(password))
            Toast.makeText(CustRegisterActivity.this, "Please write Password", Toast.LENGTH_SHORT).show();

        else
        {
            loading.setTitle("Customer Login");
            loading.setMessage("Please wait while we verify your credentials");
            loading.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CustRegisterActivity.this, "Customer Login Successful", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }

                    else
                    {
                        Toast.makeText(CustRegisterActivity.this, "Customer Login Unsuccessful. Please Try again", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
            });
        }

    }

    private void RegisterCustomer(String email,String password) {
        if (TextUtils.isEmpty(email))
            Toast.makeText(CustRegisterActivity.this, "Please write Email", Toast.LENGTH_SHORT).show();

        else if (TextUtils.isEmpty(password))
            Toast.makeText(CustRegisterActivity.this, "Please write Password", Toast.LENGTH_SHORT).show();

        else
            {
                loading.setTitle("Customer Registration");
                loading.setMessage("Please wait while we register your data");
                loading.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CustRegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }

                    else
                     {
                        Toast.makeText(CustRegisterActivity.this, "Registration Unsuccessful. Please Try again", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
            });
        }
    }

}