package com.example.goals.DatabaseActivities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


import com.example.goals.Goal;
import com.example.goals.Database.GoalDatabase;
import com.example.goals.GoalsAdapter;
import com.example.goals.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.ActionBar;
import 	android.view.Gravity;

public class GoalListActivity extends AppCompatActivity implements GoalsAdapter.OnGoalItemClick {


    private RecyclerView recyclerView;
    private GoalDatabase goalDatabase;
    private List<Goal> goals;
    private GoalsAdapter goalsAdapter;
    private int pos;

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(GoalListActivity.this, AddGoalActivity.class), 100);
        }
    };

    private void displayList() {
        goalDatabase = GoalDatabase.getInstance(GoalListActivity.this);
        new RetrieveTask(this).execute();
    }

    private static class RetrieveTask extends AsyncTask<Void, Void, List<Goal>> {
        private WeakReference<GoalListActivity> activityReference;

        RetrieveTask(GoalListActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<Goal> doInBackground(Void... voids) {
            if (activityReference.get() != null) {
                Date temp = new Date();
                return activityReference.get().goalDatabase.getGoalDao().getGoals(temp.getTime());
            }
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Goal> goals) {
            if (goals != null && goals.size() > 0) {
                activityReference.get().goals.clear();
                activityReference.get().goals.addAll(goals);

                activityReference.get().goalsAdapter.notifyDataSetChanged();

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        initializeViews();
        BottomNavigationView nav = findViewById(R.id.navigation);
        Menu menu = nav.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_goals:
                        break;
                    case R.id.navigation_rewards:
                        Intent intent1 = new Intent(GoalListActivity.this, RewardListActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.navigation_stats:
                        Intent intent2 = new Intent(GoalListActivity.this, UserStatsActivity.class);
                        startActivity(intent2);
                        break;
                }
                return false;
            }
        });
        nav.setSelectedItemId(R.id.navigation_goals);
        displayList();

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayOptions(actionBar.DISPLAY_SHOW_CUSTOM);
            View cView = getLayoutInflater().inflate(R.layout.actionbar, null);
            actionBar.setCustomView(cView);

        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "asdf";
            String description = "desccc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("qwerty", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton add_but = findViewById(R.id.but_add);
        add_but.setOnClickListener(listener);
        recyclerView = findViewById(R.id.goalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(GoalListActivity.this));
        goals = new ArrayList<>();
        goalsAdapter = new GoalsAdapter(goals, GoalListActivity.this);
        recyclerView.setAdapter(goalsAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode > 0) {
            if (resultCode == 1) {
                goals.add((Goal) data.getSerializableExtra("goal"));

            } else if (resultCode == 2) {
                goals.set(pos, (Goal) data.getSerializableExtra("goal"));
            }
            displayList();
        }
    }

    @Override
    public void onGoalClick(final int pos) {
        new AlertDialog.Builder(GoalListActivity.this)
                .setTitle("Select Options")
                .setItems(new String[]{"Complete", "Update", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                goals.get(pos).setComplete(true);
                                Date date = new Date();
                                goals.get(pos).setCompleteDate(date.getTime());
                                goalDatabase.getGoalDao().updateGoal(goals.get(pos));
                                goals.remove(pos);
                                recyclerView.setAdapter(goalsAdapter);
                                break;
                            case 1:
                                GoalListActivity.this.pos = pos;
                                startActivityForResult(
                                        new Intent(GoalListActivity.this, AddGoalActivity.class).putExtra("goal", goals.get(pos)), 100);
                                recyclerView.setAdapter(goalsAdapter);
                                break;
                            case 2:
                                goalDatabase.getGoalDao().deleteGoal(goals.get(pos));
                                goals.remove(pos);
                                recyclerView.setAdapter(goalsAdapter);
                                break;
                        }
                    }
                }).show();
    }


    @Override
    protected void onDestroy(){
        goalDatabase.cleanUp();
        super.onDestroy();
    }
}

