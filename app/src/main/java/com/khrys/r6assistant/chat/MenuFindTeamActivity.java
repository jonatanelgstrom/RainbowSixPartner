package com.khrys.r6assistant.chat;
/*
 * Created by Khrys.
 *
 * App : RainbowSixAssistant
 * Info : 7/10/2017 [11:18 AM]
*/
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khrys.r6assistant.R;

public class MenuFindTeamActivity extends AppCompatActivity
{
    AlertDialog dialog;
    String pseudoDb, ownerId;
    boolean existDb;
    ProgressBar progressLoad;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_teams);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ListView listTeams = (ListView) findViewById(R.id.listTeam);
        Button buttonCreateTeam = (Button) findViewById(R.id.buttonCreateTeam);
        progressLoad = (ProgressBar) findViewById(R.id.progressBarLoadTeam);

        initPseudoDb();

        buttonCreateTeam.setOnClickListener(new View.OnClickListener()
        {
            EditText editName;
            Spinner spinnerLanguage, spinnerMpMode, spinnerPlatform, spinnerRank, spinnerPlayerNeed;
            String[] modeMp = {"Casual","Ranked","Practice"}, platformsList = {"PC","Xbox ONE","PS4"}, ranksList = {"Copper","Bronze","Silver","Gold","Platinium","Diamond"};
            String[] languagesList = {"English","French","German","Italian","Japanese","Korean","Polish","Portuguese","Russian","Spanish"};
            String[] languagesIdList = {"gb","fr","de","it","jp","kr","pl","pt","ru","es"};
            String[] numberOfPlayers = {"1","2","3"};

            @Override
            public void onClick(View view)
            {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());

                LayoutInflater layoutDialog = getLayoutInflater();

                dialogBuilder.setView(layoutDialog.inflate(R.layout.dialog_create_team, null))
                        .setPositiveButton("Create", null)
                        .setNegativeButton("Cancel", null);

                dialog = dialogBuilder.create();
                dialog.setTitle("Create your team");

                dialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface dialogInterface)
                    {
                        editName = (EditText) dialog.findViewById(R.id.editName);
                        spinnerLanguage = (Spinner) dialog.findViewById(R.id.spinnerLanguage);
                        spinnerMpMode = (Spinner) dialog.findViewById(R.id.spinnerLooking);
                        spinnerPlatform = (Spinner) dialog.findViewById(R.id.spinnerPlatform);
                        spinnerRank = (Spinner) dialog.findViewById(R.id.spinnerRank);
                        spinnerPlayerNeed = (Spinner) dialog.findViewById(R.id.spinnerPlayerNeed);

                        spinnerLanguage.setAdapter(new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, languagesList));
                        spinnerPlatform.setAdapter(new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, platformsList));
                        spinnerMpMode.setAdapter(new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, modeMp));
                        spinnerRank.setAdapter(new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, ranksList));
                        spinnerPlayerNeed.setAdapter(new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, numberOfPlayers));

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                if(!dbExists())
                                {
                                    if(editName.getText().toString().length() > 4)
                                    {
                                        Task taskSend = FirebaseDatabase.getInstance().getReference().child("teams").child("team_"+getUid())
                                                .setValue(new Team(getUid(),editName.getText().toString(),languagesIdList[spinnerLanguage.getSelectedItemPosition()],spinnerPlatform.getSelectedItemPosition()+1,spinnerMpMode.getSelectedItemPosition()+1,spinnerRank.getSelectedItemPosition()+1,Integer.parseInt(numberOfPlayers[spinnerPlayerNeed.getSelectedItemPosition()])));
                                        taskSend.addOnSuccessListener(new OnSuccessListener()
                                        {
                                            @Override
                                            public void onSuccess(Object o)
                                            {
                                                switchToTeamView();
                                                dialog.cancel();
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(), "Name of the team is way too short !", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                });

                dialog.show();
            }
        });

        FirebaseListAdapter<Team> adapter = new FirebaseListAdapter<Team>(this, Team.class, R.layout.team, FirebaseDatabase.getInstance().getReference("teams"))
        {
            String nameChann;

            @Override
            public void onDataChanged()
            {
                super.onDataChanged();
                progressLoad.setVisibility(View.GONE);
            }

            @Override
            protected void populateView(View v, Team model, final int position)
            {
                TextView txtNom = v.findViewById(R.id.nomTeam);
                ImageView imageLanguage = v.findViewById(R.id.imageLanguage);
                TextView txtLooking = v.findViewById(R.id.textLooking);
                ImageView imagePlatform = v.findViewById(R.id.imagePlatform);
                ImageView imageRank = v.findViewById(R.id.imageRank);
                TextView textPlayerNeeded = v.findViewById(R.id.textPlayerNeeded);

                nameChann = model.getName();
                txtNom.setText(nameChann);

                imageLanguage.setImageResource(getResources().getIdentifier("flag_"+model.getLanguage(),"drawable",getPackageName()));

                String textLooking = "";

                switch (model.getLooking())
                {
                    case 1:
                        textLooking = "for Casual";
                        break;

                    case 2:
                        textLooking = "for Ranked";
                        break;

                    case 3:
                        textLooking = "for Practice";
                        break;
                }

                txtLooking.setText(textLooking);

                imagePlatform.setImageResource(getResources().getIdentifier("platform_"+model.getPlatform(), "drawable", getPackageName()));

                imageRank.setImageResource(getResources().getIdentifier("rankg_"+model.getRank(), "drawable", getPackageName()));

                String textPlayerNeed = String.valueOf(5-model.getPlayerneeded())+"/5";
                textPlayerNeeded.setText(textPlayerNeed);

                ownerId = model.getIdplayer();

                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        switchToTeamView();
                    }
                });
            }
        };

        listTeams.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void switchToTeamView()
    {
        Intent teamAct = new Intent(MenuFindTeamActivity.this, TeamActivity.class).putExtra("ownerId", ownerId).putExtra("pseudo",pseudoDb);
        startActivity(teamAct);
    }

    String getUid()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    void initPseudoDb()
    {
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                pseudoDb = dataSnapshot.child("users").child(getUid()).getValue(User.class).getPseudo();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    boolean dbExists()
    {
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                existDb = dataSnapshot.child("teams").hasChild("team_"+getUid());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return existDb;
    }
}