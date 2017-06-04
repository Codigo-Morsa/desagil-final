package pesadadobatata.songsync;

public class Request{
    private String requestKey;
    private String requester;
    private String destination;

        Request(String rk, String rq, String dt){
            requestKey = rk;
            requester = rq;
            destination = dt;
        }

    public String getDestination() {
        return destination;
    }

    public String getRequester() {
        return requester;
    }

    public String getRequestKey() {
        return requestKey;
    }
}
