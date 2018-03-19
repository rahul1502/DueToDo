package due2do.mobile.com.duetodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class to_do extends AppCompatActivity {

    FloatingActionButton fab_main, fab_photo, fab_map, fab_simple,fab_event;
    Animation fabopen, fabclose, fabrotate, fabantirotate;
    TextView taskName,taskDate;
    boolean isopen = false;
    CameraReminder cameraReminder = new CameraReminder();
    RecyclerView recyclerView;
    ReminderAdapter adapter;

    List<CameraReminder> reminderList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference readRef = database.getReference().child("CameraTask");


        fab_main = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab_photo = (FloatingActionButton) findViewById(R.id.floatingActionButton4);
        fab_map = (FloatingActionButton) findViewById(R.id.floatingActionButton5);
        fab_simple = (FloatingActionButton) findViewById(R.id.floatingActionButton6);
        fab_event = (FloatingActionButton) findViewById(R.id.floatingActionButton7);
        fabopen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.animation);
        fabclose= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.animation_close);
        fabrotate = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        fabantirotate = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);
        taskName = findViewById(R.id.TaskTitle);
        taskDate = findViewById(R.id.date);

        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isopen){
                    fab_photo.startAnimation(fabclose);
                    fab_map.startAnimation(fabclose);
                    fab_simple.startAnimation(fabclose);
                    fab_event.startAnimation(fabclose);
                    fab_main.startAnimation(fabantirotate);
                    fab_photo.setClickable(false);
                    fab_map.setClickable(false);
                    fab_simple.setClickable(false);
                    fab_event.setClickable(false);
                    isopen=false;

                }else {
                    fab_photo.startAnimation(fabopen);
                    fab_map.startAnimation(fabopen);
                    fab_simple.startAnimation(fabopen);
                    fab_event.startAnimation(fabopen);
                    fab_main.startAnimation(fabrotate);
                    fab_photo.setClickable(true);
                    fab_map.setClickable(true);
                    fab_simple.setClickable(true);
                    fab_event.setClickable(true);
                    isopen=true;
                }
            }
        });

        fab_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(to_do.this, CameraActivity.class));
            }
        });

        fab_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(to_do.this, create2.class));
            }
        });

        fab_simple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(to_do.this,MainActivity1.class));
            }
        });

        fab_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(to_do.this,Event.class));
            }
        });



        recyclerView = (RecyclerView) findViewById(R.id.recylerView);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Read From Firebase Database
        readRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    cameraReminder = ds.getValue(CameraReminder.class);
                    reminderList.add(cameraReminder);
                }
                adapter = new ReminderAdapter(to_do.this, reminderList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


}
