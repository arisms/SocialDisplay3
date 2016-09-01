package fi.tut.cs.social.socialdisplay3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NewProfileFragment extends Fragment {
    private static final String TAG = "NewProfileFragment";


    public static final String IMAGE_SELECTED = "fi.tut.cs.social.socialdisplay3.IMAGE_SELECTED";
    private OnNewProfileFragmentInteractionListener mListener;
    MainActivity mainActivity;
    Spinner typeSP;
    EditText nameET;
    Button saveBN;
    Button appsBN;
    Button pictureBN;
    ImageView pictureIV;
    SeekBar rangeSB;
    TextView minRangeTV, maxRangeTV, currentRangeTV, rangeTitleTV;
    List<App> selectedApps;
    List<String> selectedAppsNames;
    Bitmap profilePicture = null;

    static final int SELECT_APPS_REQUEST = 10;

    public NewProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_profile, container, false);

        mainActivity = (MainActivity) getActivity();

        selectedApps = new ArrayList<App>();
        selectedAppsNames = new ArrayList<String>();

        rangeSB = (SeekBar) view.findViewById(R.id.rangeSB);
        minRangeTV = (TextView) view.findViewById(R.id.minRangeTV);
        maxRangeTV = (TextView) view.findViewById(R.id.maxRangeTV);
        currentRangeTV = (TextView) view.findViewById(R.id.currentRangeTV);
        rangeTitleTV = (TextView) view.findViewById(R.id.rangeTitleTV);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IMAGE_SELECTED);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        // Create the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mainActivity,
                R.array.type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSP = (Spinner) view.findViewById(R.id.typeSP);
        typeSP.setAdapter(adapter);

        typeSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (typeSP.getSelectedItem().toString().equals("Location")) {
                    rangeSB.setVisibility(View.VISIBLE);
                    minRangeTV.setVisibility(View.VISIBLE);
                    maxRangeTV.setVisibility(View.VISIBLE);
                    currentRangeTV.setVisibility(View.VISIBLE);
                    rangeTitleTV.setVisibility(View.VISIBLE);
                } else {
                    rangeSB.setVisibility(View.GONE);
                    minRangeTV.setVisibility(View.GONE);
                    maxRangeTV.setVisibility(View.GONE);
                    currentRangeTV.setVisibility(View.GONE);
                    rangeTitleTV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rangeSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == rangeSB.getMax()) {
                    currentRangeTV.setText("1km");
                } else {
                    currentRangeTV.setText(Integer.toString(i * 50) + "m");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        nameET = (EditText) view.findViewById(R.id.nameET);
        // Hide the soft keyboard when the user is done typing
        nameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!nameET.isFocused()) {
                    InputMethodManager imm = (InputMethodManager) mainActivity
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        pictureIV = (ImageView) view.findViewById(R.id.pictureIV);

        pictureBN = (Button) view.findViewById(R.id.pictureBN);
        pictureBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ImageCropActivity.class);
                startActivity(intent);
            }
        });

        saveBN = (Button) view.findViewById(R.id.saveBN);
        saveBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Boolean nameExists = false;
                String name = nameET.getText().toString();
                for(Profile tempProfile : mainActivity.myProfiles) {
                    if(name.equals(tempProfile.getName())) {
                        nameExists = true;
                        break;
                    }
                }

                if(nameExists) {
                    Toast.makeText(getContext(), "Name of profile already exists!", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Check if name is empty
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Name of profile cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                    else if(profilePicture == null) {
                        Toast.makeText(getContext(), "You must choose a picture!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Profile newProfile = new Profile();
                        newProfile.setName(name);
                        newProfile.setType(typeSP.getSelectedItem().toString());
                        newProfile.setActive(false);
                        Log.d(TAG, "SIZE: " + selectedAppsNames.size());
                        for(String appName : selectedAppsNames) {
                            newProfile.addApp(appName);
                        }
                        mainActivity.myProfiles.add(newProfile);
                        mainActivity.writeProfiles();

                        // Write the picture to the profile-specific file
                        String filename = "socialDisplayPicture" + name;
                        try {
                            File file = new File(getContext().getFilesDir(), filename);

                            // Create the file if it doesn't already exist
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            // Write to the file
                            FileOutputStream outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                            profilePicture.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.close();

                        } catch (IOException e) {
                            Log.e(TAG, "Unable to access file, " + e.toString());
                        }

                        mainActivity.loadProfilesFragment("Profile " + newProfile.getName() + " has been created.");
                    }
                }
            }
        });

        appsBN = (Button) view.findViewById(R.id.appsBN);
        appsBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Start the AppsListActivity
                Intent selectActivitiesIntent = new Intent().setClass(getContext(), AppsListActivity.class);
                startActivityForResult(selectActivitiesIntent, SELECT_APPS_REQUEST);

            }
        });

        mainActivity.hideFab();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewProfileFragmentInteractionListener) {
            mListener = (OnNewProfileFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult - Fragment, requestCode: " + requestCode + " resultCode: " + resultCode);

        //String[] selectedAppNames = data.getStringArrayExtra("selectedAppNames");

        if(requestCode == SELECT_APPS_REQUEST) {

            // Read the list of selected apps from the shared file
            String filename = "socialDisplayApps";
            File file = new File(mainActivity.getFilesDir(), filename);

            if (file.exists()) {
                try {
                    FileInputStream fileInputStream = mainActivity.openFileInput(filename);
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                    selectedAppsNames = (List<String>) objectInputStream.readObject();
                    objectInputStream.close();
                    fileInputStream.close();

                } catch (IOException e) {
                    Log.e(TAG, "Unable to access file, " + e.toString());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            for (String app : selectedAppsNames) {
                Log.d(TAG, "Selected names: " + app);
                //selectedAppsNames.add(app);
            }
        }

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "broadcastReceiver - onReceive()");

            if(intent.getAction().equals(IMAGE_SELECTED)) {
                Log.d(TAG, "intent action = IMAGE_SELECTED");

                // Read the saved image from the file
                String filename = "SocialDisplayPicturesBuffer";
                File file = new File(getContext().getFilesDir(), filename);

                if (file.exists()) {
                    try {
                        FileInputStream fileInputStream = context.openFileInput(filename);
                        //ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                        //pictureIV.setImageBitmap(BitmapFactory.decodeStream(fileInputStream));
                        profilePicture = BitmapFactory.decodeStream(fileInputStream);
                        pictureIV.setImageBitmap(profilePicture);
                        //objectInputStream.close();
                        fileInputStream.close();

                    } catch (IOException e) {
                        Log.e(TAG, "Unable to access file, " + e.toString());
                    }
                }

            }
        }
    };

    public interface OnNewProfileFragmentInteractionListener {

        void onSaveNewFragment();
    }
}
