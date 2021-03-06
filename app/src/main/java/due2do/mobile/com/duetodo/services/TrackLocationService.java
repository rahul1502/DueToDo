package due2do.mobile.com.duetodo.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import due2do.mobile.com.duetodo.R;
import due2do.mobile.com.duetodo.activities.TrackStatus;
import due2do.mobile.com.duetodo.activities.LocationActivity;
import due2do.mobile.com.duetodo.model.Task;

public class TrackLocationService extends Service {

    // get uid
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uid = firebaseAuth.getCurrentUser().getUid();
    // Database
    private DatabaseReference mDatabaseReference;

    private static final String TASK = "task";
    private static final String CURRENT = "current";
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.CANADA);

    private Double currentLatitude;
    private Double currentLongitude;
    private Double taskLatitude;
    private Double taskLongitude;

    long difference;
    Task taskObj = new Task();

    private static final int LOCATION_INTERVAL = 60000;
    private static final float LOCATION_DISTANCE = 5000;

    LocationManager locationManager;
    Date taskDate;
    public TrackLocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get current location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(currentLocation != null) {
            currentLatitude = currentLocation.getLatitude();
            currentLongitude = currentLocation.getLongitude();
        }
        else {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            currentLatitude = currentLocation.getLatitude();
            currentLongitude = currentLocation.getLongitude();
        }


        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }
            @Override
            public void onProviderEnabled(String s) {

            }
            @Override
            public void onProviderDisabled(String s) {

            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference taskRef = mDatabaseReference.child(uid).child("LocationBased");
        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    taskObj = ds.getValue(Task.class);

                    // date time operation
//                    Log.i("diff", String.valueOf(taskObj.getDay()));
//                    Log.i("diff", String.valueOf(taskObj.getMonth()));
//                    Log.i("diff", String.valueOf(taskObj.getYear()));
//                    Log.i("diff", String.valueOf(taskObj.getHour()));
//                    Log.i("diff", String.valueOf(taskObj.getMinute()));
                    final Date currentTime = Calendar.getInstance().getTime();
                    final Calendar cal = Calendar.getInstance();
                    try {
                        String date = date = taskObj.getDay() + "/" + taskObj.getMonth() + "/" + taskObj.getYear() + " " + taskObj.getHour() + ":" + taskObj.getMinute();
                        taskDate = dateFormat.parse(date);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(taskDate != null){
                        difference = taskDate.getTime() - currentTime.getTime();
                        Log.i("diff",String.valueOf(difference));
                        Log.i("diff","sadasda");
                        if(difference < 0){
                            // task outdated
//                            Log.i("diff", String.valueOf(currentTime));

                            mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference taskRef = mDatabaseReference.child(uid).child("LocationBased").child(ds.getKey());

                            taskRef.child("taskStatus").setValue("OutDated");
                        }
                    }


                    // lat long notification

                    taskLatitude = taskObj.getLatitude(); // retrieved task latitude
                    taskLongitude = taskObj.getLongitude(); // retrieved task longitude

                    // calculate distance between current latitude and longitude
                    // https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
                    final int R = 6371; // Radius of the earth

                    double latDistance = Math.toRadians(currentLatitude - taskLatitude);
                    double lonDistance = Math.toRadians(currentLongitude - taskLongitude);

                    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                            + Math.cos(Math.toRadians(currentLatitude)) * Math.cos(Math.toRadians(taskLatitude))
                            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    double distance = R * c ;

                    Log.i(TASK, taskLatitude.toString());
                    Log.i(TASK, taskLongitude.toString());
                    Log.i(TASK, currentLatitude.toString());
                    Log.i(TASK, currentLongitude.toString());
                    Log.i(TASK, String.valueOf(distance));

                    if(distance < 0.1){
                        sendNotification(ds.getKey(),taskObj.getTask(), distance);
                    }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return Service.START_STICKY;
    }

    public void sendNotification(String taskId,String task, Double distance){
        // https://www.tutorialspoint.com/android/android_notifications.htm

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"LocationBasedNotification")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(task)
                .setContentText("Task " + task + " is " + distance * 1000 + " meters away.");

        Intent intent = new Intent(this, TrackStatus.class);
        intent.putExtra("TaskName",task);
        intent.putExtra("TaskId",taskId);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TrackStatus.class);

        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }
}
