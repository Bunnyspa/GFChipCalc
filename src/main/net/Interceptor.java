package main.net;

public interface Interceptor {

    boolean interceptRequest(RequestHeader requestHeader);

    boolean interceptResponse(RequestHeader requestHeader, ResponseHeader responseHeader);

    void onRequestIntercept(RequestHeader requestHeader, byte[] request);

    void onResponseIntercept(RequestHeader requestHeader, ResponseHeader responseHeader, byte[] response);

}
