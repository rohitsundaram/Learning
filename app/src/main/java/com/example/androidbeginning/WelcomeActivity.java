package com.example.androidbeginning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {
    private Button iAmDriver;
    private Button iAmCustomer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        iAmDriver=(Button) findViewById(R.id.welcome_driver_btn);
        iAmCustomer=(Button) findViewById(R.id.welcome_customer_btn);

        iAmDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent LoginCustomerRegisterIntent=new Intent(WelcomeActivity.this,DriverRegisterActivity.class);
                startActivity(LoginCustomerRegisterIntent);
            }
        });

        iAmCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent LoginCustRegisterIntent=new Intent(WelcomeActivity.this,CustRegisterActivity.class);
                startActivity(LoginCustRegisterIntent);
            }
        });
    }
}