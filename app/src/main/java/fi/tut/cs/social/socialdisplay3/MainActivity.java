package fi.tut.cs.social.socialdisplay3;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProfilesFragment.OnListFragmentInteractionListener,
        NewProfileFragment.OnNewProfileFragmentInteractionListener
{
    private static final String TAG = "MainActivity";

    Fragment currentFragment;
    ProfilesFragment profilesFragment;
    NewProfileFragment newProfileFragment;
    FloatingActionButton fab;
    public static List<Profile> myProfiles;
    public static Profile currentProfile;
    public static Profile activeProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myProfiles = new ArrayList<Profile>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = (FloatingActionButton) findViewById(R.id.addFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newProfile();
            }
        });

        // Create the Fragments
        profilesFragment = new ProfilesFragment();
        newProfileFragment = new NewProfileFragment();

        // Load profiles fragment
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, profilesFragment)
                .addToBackStack(null).commit();
        currentFragment = profilesFragment;

        // Trigger the service
        Intent startServiceIntent = new Intent(this, SocialDisplayService.class);
        startServiceIntent.putExtra("Trigger", "Activity");
        startService(startServiceIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.new_profile) {
            newProfile();
            return true;
        }
        else if(id == R.id.home_page) {
            // Load profiles fragment
            loadProfilesFragment(null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(Profile profile, Boolean longPress) {
        Log.d(TAG, "onListFragmentInteraction - " + longPress);

        // Delete profile on long press
        if(longPress) {

            currentProfile = profile;

            deleteProfileDialogFragment dialogFragment = new deleteProfileDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "dialogFragmentTag");
        }
    }

    public void newProfile() {

        // Go to NewProfileFragment
        Log.d(TAG, "newProfile()");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newProfileFragment)
                .addToBackStack(null).commit();
        currentFragment = newProfileFragment;

        // Hide the FAB
        hideFab();
    }

    public void writeProfiles(){

        String filename = "socialDisplayProfiles";
        try {
            File file = new File(getApplicationContext().getFilesDir(), filename);

            // Create the file if it doesn't already exist
            if (!file.exists()) {
                file.createNewFile();
            }
            // Write to the file
            FileOutputStream outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(myProfiles);
            objectOutputStream.close();
            outputStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Unable to access file, " + e.toString());
        }
        profilesFragment.addProfilesToList();
    }

    public void readProfiles() {

        String filename = "socialDisplayProfiles";
        File file = new File(this.getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = this.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                myProfiles = (List<Profile>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

//        if(currentFragment == profilesFragment) {
            profilesFragment.addProfilesToList();
//        }
    }

    public void loadProfilesFragment(String toast) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profilesFragment)
                .addToBackStack(null).commit();
        currentFragment = profilesFragment;

        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveNewFragment() {

    }

    public void hideFab() {
        fab.hide();
    }

    public void showFab() {
        fab.show();
    }

    @SuppressLint("ValidFragment")
    public class deleteProfileDialogFragment extends DialogFragment {

        public deleteProfileDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you want to delete profile " + currentProfile.getName() + "?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            myProfiles.remove(currentProfile);
                            writeProfiles();
                            loadProfilesFragment("Profile " + currentProfile.getName() + " was deleted.");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public void activateProfile(Profile profile) {

        for(Profile tempProfile : myProfiles) {
            if(tempProfile.getName().equals(profile.getName())) {
                activeProfile = profile;
            }
            else {
                tempProfile.setActive(false);
            }
        }
        writeProfiles();
        Toast.makeText(this, "Profile " + profile.getName() + " has been activated.", Toast.LENGTH_SHORT).show();
        for(String appName : profile.getApps()) {
            Log.d(TAG, appName);
        }
    }

}
