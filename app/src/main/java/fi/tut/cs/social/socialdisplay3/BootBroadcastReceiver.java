package fi.tut.cs.social.socialdisplay3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    public BootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootBroadcastReceiver", "onReceive");

        Intent startServiceIntent = new Intent(context, SocialDisplayService.class);
        startServiceIntent.putExtra("Trigger", "Boot");
        context.startService(startServiceIntent);
    }
}
