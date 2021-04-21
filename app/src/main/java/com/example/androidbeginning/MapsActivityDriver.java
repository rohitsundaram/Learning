package com.example.androidbeginning;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.GestureOverlayView;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.internal.GoogleApiAvailabilityCache;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivityDriver extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button DriverSettingsBtn;
    private Button DriverLogoutBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Boolean currentLogoutDriverStatus=false;
    private DatabaseReference AssignedCustomerRef,AssignedCustomerPickUpRef;
    private String DriverID,customerID="";
    Marker PickUpMarker;

    private ValueEventListener AssignedCustomerPickUpRefListner;

    private TextView txtName,txtPhone;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_driver);

        DriverSettingsBtn=(Button)findViewById(R.id.driver_settings_btn);
        DriverLogoutBtn=(Button)findViewById(R.id.driver_logout_btn);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        DriverID=mAuth.getCurrentUser().getUid();

        txtName=findViewById(R.id.name_customer);
        txtPhone=findViewById(R.id.phone_customer);
        relativeLayout=findViewById(R.id.rel2);
        profilePic=findViewById(R.id.profile_image_customer);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DriverSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MapsActivityDriver.this,SettingsActivity.class);
                intent.putExtra("type","Drivers");
                startActivity(intent);
            }
        });

        DriverLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLogoutDriverStatus=true;
                DisconnectTheDriver();
                mAuth.signOut();
                LogoutDriver();
            }
        });

        GetAssignedCustomerRequest();
    }

    private void GetAssignedCustomerRequest()
    {
        AssignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverID).child("CustomerRideId");
        AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    customerID=snapshot.getValue().toString();
                    GetAssignedCustomerPickUpLocation();

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedCustomerInformation();
                }
                else
                {
                    customerID="";
                    if(PickUpMarker!=null)
                    {
                        PickUpMarker.remove();
                    }
                    if(AssignedCustomerPickUpRefListner!=null)
                    {
                        AssignedCustomerPickUpRef.removeEventListener(AssignedCustomerPickUpRefListner);
                    }

                    relativeLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetAssignedCustomerPickUpLocation()
    {
        AssignedCustomerPickUpRef=FirebaseDatabase.getInstance().getReference().child("Customer Requests").child(customerID).child("l");
        AssignedCustomerPickUpRefListner=AssignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    List<Object> customerLocationMap=(List<Object>)snapshot.getValue();
                    double LocationLat=0;
                    double LocationLng=0;

                    if(customerLocationMap.get(0)!=null)
                    {
                        LocationLat=Double.parseDouble(customerLocationMap.get(0).toString());
                    }

                    if(customerLocationMap.get(1)!=null)
                    {
                        LocationLng=Double.parseDouble(customerLocationMap.get(1).toString());
                    }
                    LatLng DriverLatLng=new LatLng(LocationLat,LocationLng);
                    PickUpMarker=mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Customer PickUp Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null)
        {
            lastLocation=location;
            LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

            String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference DriverAvailabilityRef= FirebaseDatabase.getInstance().getReference().child("Drivers Available");
            GeoFire geoFireAvailability=new GeoFire(DriverAvailabilityRef);


            DatabaseReference DriverWorkingRef=FirebaseDatabase.getInstance().getReference().child("Drivers Working");
            GeoFire geoFireWorking=new GeoFire(DriverWorkingRef);

            switch (customerID)
            {
                case "" :
                    geoFireWorking.removeLocation(userID, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });


                    geoFireAvailability.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }

                            });
                    break;

                default:
                    geoFireAvailability.removeLocation(userID, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }

                            });
                    break;

            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();

        if(!currentLogoutDriverStatus)
        {
            DisconnectTheDriver();
        }
    }

    private void DisconnectTheDriver()
    {
        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DriverAvailabilityRef= FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        GeoFire geoFire=new GeoFire(DriverAvailabilityRef);
        geoFire.removeLocation(userID, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                
            }
        });
    }

    private void LogoutDriver()
    {
        Intent WelcomeIntent=new Intent(MapsActivityDriver.this,WelcomeActivity.class);
        WelcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(WelcomeIntent);
        finish();
    }

    private void getAssignedCustomerInformation()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0)
                {
                    String name=snapshot.child("name").getValue().toString();
                    String phone=snapshot.child("phone").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);


                    if(snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}