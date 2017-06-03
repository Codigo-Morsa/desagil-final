package pesadadobatata.songsync;

/**
 * Created by fredcurti on 03/06/17.
 */

public interface ConnectionHandlerListener {
    void onSongChanged(String songurl,String timeStamp);
}
