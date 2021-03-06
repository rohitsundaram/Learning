package com.example.androidbeginning;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivityCustomer extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button CustomerSettingsBtn;
    private Button CustomerLogoutBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    Marker DriverMarker,PickUpMarker;
    private Button CustomerCallACabBtn;
    private String customerID;
    private DatabaseReference CustomerDatabaseRef;
    private LatLng CustomerPickUpLocation;
    private DatabaseReference DriverAvailableRef;
    private int radius= 500;
    private Boolean driverFound=false,requestType=false;
    private String driverFoundID;
    private DatabaseReference DriverRef;
    private DatabaseReference DriverLocationRef;
    GeoQuery geoQuery;
    private ValueEventListener DriverLocationRefListner;

    private TextView txtName,txtPhone,txtCarName;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;

    //New Addition
    private ArrayList<String> UserList=new ArrayList<String>();
    private int count=0;
    private HashMap<String,Marker>myMap=new HashMap<String, Marker>();
    Boolean myKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_customer);


        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        DriverAvailableRef=FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        DriverLocationRef=FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        CustomerLogoutBtn=(Button)findViewById(R.id.Customer_logout_btn);
        CustomerSettingsBtn=(Button)findViewById(R.id.Customer_Settings_btn);
        CustomerCallACabBtn=(Button)findViewById(R.id.Customer_call_a_cab_btn);

        txtName=findViewById(R.id.name_driver);
        txtPhone=findViewById(R.id.phone_driver);
        txtCarName=findViewById(R.id.car_name_driver);
        relativeLayout=findViewById(R.id.rell);
        profilePic=findViewById(R.id.profile_image_driver);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CustomerSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MapsActivityCustomer.this,SettingsActivity.class);
                intent.putExtra("type","Customers");
                startActivity(intent);
            }
        });

        CustomerLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                LogoutCustomer();
            }
        });

        CustomerCallACabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestType)
                {
                    requestType=false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListner);
                    if(driverFound!=null)
                    {
                        DriverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("CustomerRideId");
                        DriverRef.removeValue();
                        driverFoundID=null;
                    }
                    driverFound=false;
                    radius=1;
                    String customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire=new GeoFire(CustomerDatabaseRef);
                    geoFire.removeLocation(customerID, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    if(PickUpMarker!=null)
                    {
                        PickUpMarker.remove();
                    }

                    for(String driverFoundId:UserList) {
                        DriverMarker=myMap.get(driverFoundId);
                        if (DriverMarker != null) {
                            DriverMarker.remove();
                        }
                    }
                    CustomerCallACabBtn.setText("Call a Cab");
                    relativeLayout.setVisibility(View.GONE);

                }
                else
                {
                    requestType=true;
                    String customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire=new GeoFire(CustomerDatabaseRef);
                    geoFire.setLocation(customerID,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }

                            });
                    CustomerPickUpLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    PickUpMarker=mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                    CustomerCallACabBtn.setText("Getting your driver...");
                    GetCloseDriverCab();
                }

            }
        });
    }



    private void GetCloseDriverCab()
    {
        GeoFire geoFire=new GeoFire(DriverAvailableRef);
        geoQuery=geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude,CustomerPickUpLocation.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestType)
                {

                    //coooo**********************
                    //driverFound=true;
                    //cppp
                    driverFoundID=key;

                    //coooo
                    /*
                    DriverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    HashMap driverMap=new HashMap();
                    driverMap.put("CustomerRideId",customerID);
                    DriverRef.updateChildren(driverMap);
                    */
                    //cppppp

                    UserList.add(key);
                    count++;

                    //***************************
                    //GettingDriverLocation();

                    CustomerCallACabBtn.setText("Looking for Driver Location...");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                /*if(!driverFound)
                {
                    radius=radius+1;
                    GetCloseDriverCab();
                }
                //count=count+1;*/

                GettingDriverLocation();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingDriverLocation() {
        for (String driverFoundID : UserList) {
            DriverLocationRefListner = DriverAvailableRef.child(driverFoundID).child("l").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(myMap.containsKey(driverFoundID))
                    {
                        DriverMarker=myMap.get(driverFoundID);
                    }
                    else
                    {
                        DriverMarker=null;
                    }
                    if (snapshot.exists() && requestType) {
                        List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                        double LocationLat = 0;
                        double LocationLng = 0;
                        CustomerCallACabBtn.setText("Drivers Found");

                        //cooooo
                        //relativeLayout.setVisibility(View.VISIBLE);
                        //getAssignedDriverInformation();
                        //cpppp

                        if (driverLocationMap.get(0) != null) {
                            LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                        }

                        if (driverLocationMap.get(1) != null) {
                            LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                        }
                        LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);


                        if (DriverMarker != null) {
                            DriverMarker.remove();
                        }

                        Location location1 = new Location("");
                        location1.setLatitude(CustomerPickUpLocation.latitude);
                        location1.setLongitude(CustomerPickUpLocation.longitude);

                        Location location2 = new Location("");
                        location2.setLatitude(DriverLatLng.latitude);
                        location2.setLongitude(DriverLatLng.longitude);

                        float Distance = location1.distanceTo(location2);

                        //cooooooo****************
                        /*
                        if (Distance < 90) {
                            CustomerCallACabBtn.setText("Driver's Reached");
                        } else {
                            CustomerCallACabBtn.setText("Driver Found " + String.valueOf(Distance));
                        }*/


                        DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                        myMap.put(driverFoundID,DriverMarker);
                        //DriverMarker=null;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
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

    @Override
    public void onLocationChanged(Location location) {
        lastLocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
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
    protected void onStop()
    {
        super.onStop();
    }

    private void LogoutCustomer()
    {
        Intent WelcomeIntent=new Intent(MapsActivityCustomer.this,WelcomeActivity.class);
        WelcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(WelcomeIntent);
        finish();
    }

    private void getAssignedDriverInformation()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0)
                {
                    String name=snapshot.child("name").getValue().toString();
                    String phone=snapshot.child("phone").getValue().toString();
                    String car=snapshot.child("car").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtCarName.setText(car);


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