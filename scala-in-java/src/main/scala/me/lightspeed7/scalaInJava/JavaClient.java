package me.lightspeed7.scalaInJava;

import org.slf4j.LoggerFactory;

import scala.Tuple2;
import scala.collection.immutable.Map;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.Realm.RealmBuilder;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

import dispatch.Http;
import dispatch.Req;

public class JavaClient {

    public static final org.slf4j.Logger log = LoggerFactory.getLogger(JavaClient.class);
    private String baseUrl;
    private String username;
    private String password;

    public JavaClient(String baseUrl, String username, String password) {

        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;

    }

    public void sendAsyncHttpClient(MailMessage mailMessage)
            throws Exception {

        try (AsyncHttpClient asyncHttpClient = new AsyncHttpClient()) {

            // setup the query
            Realm realm = new RealmBuilder()//
                    .setPrincipal(username).setPassword(password)//
                    .setScheme(AuthScheme.DIGEST).build();

            ListenableFuture<Response> f = asyncHttpClient//
                    .preparePost(baseUrl + "/echo") //
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")//
                    .setRealm(realm)//
                    .setBody(String.format("body=%s", mailMessage.marshal())) //
                    .execute();

            // wait for the response
            Response response = f.get();
            if (200 != response.getStatusCode()) {
                throw new Exception("Server returned statusCode = " + response.getStatusCode());
            }

            // print out the response body
            String body = response.getResponseBody();
            System.out.println("AsyncHttpClient(Java) -> " + body);
        }

    }

    public void sendDispatchJava(MailMessage mailMessage)
            throws Exception {

        // setup the query
        @SuppressWarnings( "unchecked" )
        Map<String, String> map = scala.collection.immutable.Map$.MODULE$.<String, String> newBuilder()//
                .$plus$eq(new Tuple2<String, String>("body", mailMessage.marshal()))//
                .result();

        Req req = dispatch.url.apply(baseUrl + "/echo")//
                .as(username, password)//
                .setHeader("Content-Type", "application/x-www-form-urlencoded")//
                .$less$less(map)//
        ;

        // do the request
        Request request = req.toRequest();
        ListenableFuture<Response> listenableFuture = Http.apply$default$1().executeRequest(request);

        // do the future
        Response response = listenableFuture.get();
        if (200 != response.getStatusCode()) {
            throw new Exception("Server returned statusCode = " + response.getStatusCode());
        }

        // print out the response body
        String body = response.getResponseBody();
        System.out.println("Dispatch from Java    -> " + body);
    }
}
