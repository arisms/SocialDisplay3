package fi.tut.cs.social.socialdisplay3;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.gajah.inkcaseLib.InkCase;
import com.gajah.inkcaseLib.InkCaseUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;


public class SocialDisplayService extends Service {
    private static final String TAG = "SocialDisplayService";

    private BroadcastReceiver mScreenReceiver;
    public static List<Profile> myProfiles;
    private static AlarmManager alarm = null;
    Context context;
    Drawable icon = null;
    String applicationName;
    static String packageName;
    static String currentApp = "";
    Profile currentProfile = null;

    final int START_TASK_TO_FRONT = 2;
    ActivityManager.RunningAppProcessInfo currentInfo = null;
    Field field = null;

    public SocialDisplayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context =  getApplicationContext();

        Log.d(TAG, "Service created");

        myProfiles = new ArrayList<Profile>();

        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        IntentFilter screenOnFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, screenOffFilter);
        registerReceiver(mScreenReceiver, screenOnFilter);

        readProfiles();

        //scheduleAlarm();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        if(intent != null) {
            Log.d(TAG, "intent != null");
            if (intent.getStringExtra("Trigger").equals("Alarm")) {
                //Log.d(TAG, "Service triggered by alarm.");
                readProfiles();
                getCurrentApp();
            }
        }
        else
            Log.d(TAG, "intent is null");

        if(!isAlarmUp())
            scheduleAlarm();

        return START_STICKY;
    }

    public void getCurrentApp() {
        Log.d(TAG, "getCurrentApp()");

        PackageManager pm = context.getPackageManager();

        // Get a list of the running apps
        ActivityManager am1 = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        List<ActivityManager.RunningTaskInfo> processes = am1.getRunningTasks(Integer.MAX_VALUE);

        if (processes != null) {
            icon = null;
            applicationName = null;
            // Get the activity that is running in the foreground
            packageName = processes.get(0).topActivity
                    .getPackageName();

            // If the current activity is not inkCaseCompanionApp or the launcher
            // get the activity name and icon to send it to InkCase display
            try
            {
                ApplicationInfo a = pm.getApplicationInfo(packageName,
                        PackageManager.GET_META_DATA);
                icon = context.getPackageManager().getApplicationIcon(
                        processes.get(0).topActivity.getPackageName());
                String applicationName = (String) (a != null ? pm.getApplicationLabel(a) : "(unknown)");

                filterApp(applicationName, icon);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e("ERROR", "Unable to find icon for package '"
                        + packageName + "': " + e.getMessage());
            }
        }

    }

    public boolean filterApp(String appName, Drawable icon) {
        Log.d(TAG, "filterApp()");

        if(!appName.equals(currentApp)) {

            currentApp = appName;

            if(currentProfile != null) {

                // Go through the list of selected apps of current active profile
                for(String app : currentProfile.getApps()) {

                    // If the current app has been selected in the current profile
                    if(app.equals(appName)) {
                        Log.d(TAG, appName + "App has changed, send icon to InkCase.");
                        sendToInkCase(icon, appName);
                        return true;
                    }
                }
                Log.d(TAG, appName + " App has changed but not selected, send image to InkCase.");
                Bitmap b = getProfilePicture();
                Drawable d = new BitmapDrawable(b);
                Log.d(TAG, "Bitmap: " + b + " - " + "Drawable: " + d);

                sendToInkCaseFullScreen(d);
            }


        }
        //Log.d(TAG, applicationName + "App hasn't changed, send nothing to InkCase.");
        return false;
    }

    public Bitmap getProfilePicture() {
        Bitmap profilePicture = null;

        // Read the saved image from the file
        String filename = "socialDisplayPicture" + currentProfile.getName();
        File file = new File(getApplicationContext().getFilesDir(), filename);

        if (file.exists()) {
            Log.d(TAG, "FILE EXISTS");
            try {
                FileInputStream fileInputStream = context.openFileInput(filename);
                //ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                //pictureIV.setImageBitmap(BitmapFactory.decodeStream(fileInputStream));
                profilePicture = BitmapFactory.decodeStream(fileInputStream);
                //objectInputStream.close();
                fileInputStream.close();
                return profilePicture;

            } catch (IOException e) {
                Log.e(TAG, "Unable to access file, " + e.toString());
            }
        }
        else {
            Log.d(TAG, "FILE DOES NOT EXIST: " + filename);
            return null;
        }
        return null;
    }

    public void sendToInkCase(Drawable icon, String name) {
        // Creates a bitmap file using the icon and the name of the app
        // and sends it to be displayed on the InkCase
        Log.d(TAG, "icon: " + icon);

        String[] parts = splitIntoLines(name, 9);

        Bitmap bitmap = null;
        bitmap = drawableToBitmap(icon);
        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(android.R.color.white));
        paint.setStyle(Paint.Style.FILL);
        bitmap = Bitmap.createScaledBitmap(bitmap, 150, 175, false);
        Bitmap newBitmap = Bitmap.createBitmap(300, 600, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(newBitmap, 300, 600, paint);
        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(60);

//        Typeface mono = Typeface.createFromAsset(mContext.getAssets(), "VeraMono.ttf");
//        paint.setTypeface(mono);

        int counter = 0;
        for(String part : parts) {
            canvas.drawText(part, 10, 250 + counter*65, paint);
            counter++;
        }
        canvas.drawBitmap(bitmap, 75, 10, paint);



        if (newBitmap == null)
            throw new RuntimeException("No image to send");

        File fileToSend = new File(context.getExternalCacheDir(), "helloInkCase.jpg");
        try {
            FileOutputStream fOut = new FileOutputStream(fileToSend);

            newBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Intent sharingIntent = new Intent(InkCase.ACTION_SEND_TO_INKCASE);
            sharingIntent.setType("image/jpeg");
            sharingIntent.putExtra(InkCase.EXTRA_FUNCTION_CODE,InkCase.CODE_SEND_WALLPAPER);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileToSend));
            sharingIntent.putExtra(InkCase.EXTRA_FILENAME,fileToSend.getName());
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            InkCaseUtils.startInkCaseActivity(context, sharingIntent);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void sendToInkCaseFullScreen(Drawable icon) {
        // Creates a bitmap file using the icon and the name of the app
        // and sends it to be displayed on the InkCase
        Log.d(TAG, "sendToInkCaseFullScreen " + icon);


        Bitmap bitmap = null;
        bitmap = drawableToBitmap(icon);

        if (bitmap == null)
            throw new RuntimeException("No image to send");

        File fileToSend = new File(getExternalCacheDir(), "helloInkCase.jpg");
        try {
            FileOutputStream fOut = new FileOutputStream(fileToSend);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Intent sharingIntent = new Intent(InkCase.ACTION_SEND_TO_INKCASE);
            sharingIntent.setType("image/jpeg");
            sharingIntent.putExtra(InkCase.EXTRA_FUNCTION_CODE,InkCase.CODE_SEND_WALLPAPER);
            sharingIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(fileToSend));
            sharingIntent.putExtra(InkCase.EXTRA_FILENAME,fileToSend.getName());
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            InkCaseUtils.startInkCaseActivity(this, sharingIntent);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {

                Log.v(TAG, "In Method:  ACTION_SCREEN_OFF");
            }
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            {
                Log.v(TAG, "In Method:  ACTION_SCREEN_ON");
            }
        }
    }

    public void scheduleAlarm() {
        Log.d(TAG, "scheduleAlarm");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                2000, pIntent);

    }

    public boolean isAlarmUp() {
        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiver.REQUEST_CODE,
                new Intent(getApplicationContext(), AlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            return true;
        }

        return false;
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
    }

    public void readProfiles() {
        Log.d(TAG, "readProfiles()");


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
        for( Profile profile : myProfiles) {
            if(profile.getActive()) {
                currentProfile = profile;
                break;
            }

        }
    }

    public String[] splitIntoLines(String input, int maxCharsInLine){
        // Splits a String to multiple lines with maximum length equal to maxCharsInLine

        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            while(word.length() > maxCharsInLine){
                output.append(word.substring(0, maxCharsInLine-lineLen) + "\n");
                word = word.substring(maxCharsInLine-lineLen);
                lineLen = 0;
            }

            if (lineLen + word.length() > maxCharsInLine) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word + " ");

            lineLen += word.length() + 1;
        }
        return output.toString().split("\n");
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
