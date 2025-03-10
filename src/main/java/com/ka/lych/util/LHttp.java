package com.ka.lych.util;

import com.ka.lych.exception.LException;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 *
 * @author klausahrenberg
 */
public abstract class LHttp {

    public static LFuture<LMap, LException> post(String url, Object request) {
        return post(url, LJson.of(request));
    }
    
    public static LFuture<LMap, LException> post(String url, LJson request) {
        return post(url, request, null, null);
    }
    
    public static LFuture<LMap, LException> post(String url, Object request, String user, String password) {
        return post(url, LJson.of(request), user, password);
    }
    
    public static LFuture<LMap, LException> post(String url, LJson request, String user, String password) {
        return LFuture.<LMap, LException>execute((LTask<LMap, LException> task) -> {
            try {
                var uri = new URI(url);
                //var ur = Path.of(url).toUri().toURL();
                var con = uri.toURL().openConnection();
                LLog.test("con %s", con);
                HttpURLConnection http = (HttpURLConnection) con;
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Content-Type", "application/json");
                if ((!LString.isEmpty(user)) && (!LString.isEmpty(password))) {
                    String auth = user + ":" + password;
                    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                    String authHeaderValue = "Basic " + new String(encodedAuth);
                    http.setRequestProperty("Authorization", authHeaderValue);
                }
                var r = request.toString();
                byte[] out = r.getBytes(StandardCharsets.UTF_8);
                OutputStream stream = http.getOutputStream();
                stream.write(out);
                if (http.getResponseCode() == LHttpStatus.OK.value()) {
                    return (LMap) LJsonParser.of(LMap.class).inputStream(http.getInputStream()).parse();
                } else {
                    throw LException.of(LJsonParser.of(LMap.class).inputStream(http.getErrorStream()).parse());
                }
            } catch (URISyntaxException use) {
                throw new LException(use);
            } catch (IOException ioe) {
                throw new LException(ioe);
            }
        });
    }
}
