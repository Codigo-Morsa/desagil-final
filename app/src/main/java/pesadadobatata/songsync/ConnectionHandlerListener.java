package pesadadobatata.songsync;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by fredcurti on 03/06/17.
 */

public interface ConnectionHandlerListener {
    void onSongChanged(Track track, String timeStamp);
    void onBothClientsReady();
    void onStatusChanged(String status);
}
