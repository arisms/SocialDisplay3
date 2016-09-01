package fi.tut.cs.social.socialdisplay3;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AppsListActivity extends AppCompatActivity {
    private static final String TAG = "AppsListActivity";

    Button saveBN;
    List<String> selectedAppsNames;
    List<App> installedApps;
    AppsRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apps_list);

        final PackageManager pm = getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities( mainIntent, 0);

        // Create the list of installed applications that will be shown in the dialog list
        selectedAppsNames = new ArrayList<String>();
        installedApps = new ArrayList<App>();

        for(int i=0; i<pkgAppsList.size(); i++) {
            try {
                App tempApp = new App(pm.getApplicationLabel((pm.getApplicationInfo(pkgAppsList.get(i)
                        .activityInfo.applicationInfo.packageName, PackageManager.GET_META_DATA))).toString(),
                        drawableToBitmap(pkgAppsList.get(i).loadIcon(getPackageManager())),
                        true
                );
                installedApps.add(tempApp);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        //pkgAppsList.get(i).loadIcon(getPackageManager())


//        for(App app : selectedApps) {
//            Log.d(TAG, app.getName() + " - " + app.getIcon() + " - " + app.getSelected());
//        }

        adapter = new AppsRecyclerViewAdapter(installedApps, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.apps_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        saveBN = (Button) findViewById(R.id.saveBN);
        saveBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(App app : installedApps) {
                    //Log.d(TAG, app.getName() + " - " + app.getSelected());
                    if(app.getSelected()) {
                        selectedAppsNames.add(app.getName());
                    }
                }

                // Write the list of the selected apps to the shared file
                String filename = "socialDisplayApps";
                try {
                    File file = new File(getApplicationContext().getFilesDir(), filename);

                    // Create the file if it doesn't already exist
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    // Write to the file
                    FileOutputStream outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(selectedAppsNames);
                    objectOutputStream.close();
                    outputStream.close();

                } catch (IOException e) {
                    Log.e(TAG, "Unable to access file, " + e.toString());
                }

                Intent selectedAppsIntent = new Intent();
                setResult(RESULT_OK, selectedAppsIntent);
                finish();

            }
        });

    }


    public static Bitmap drawableToBitmap (Drawable drawable) {
        // Converts a drawable object to bitmap
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
