package pesadadobatata.songsync;

/**
 * Created by fredcurti on 31/05/17.
 */
public interface RequestHandlerListener{
    void onEvent();
    void onRequestAccepted();
    void onConnectionHandlerCreated();
}
