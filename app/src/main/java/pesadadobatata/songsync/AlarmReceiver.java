package pesadadobatata.songsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by fredcurti on 04/06/17.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private AlarmListener al;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARM_RECIEVER","ALARM RECIEVED");
        String message = "Alarm Triggered";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public static AlarmListener setAlarmListener (AlarmListener al){
        return al;
    }

}
