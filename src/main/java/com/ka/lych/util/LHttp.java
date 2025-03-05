package com.ka.lych.util;

import com.ka.lych.exception.LException;
import com.ka.lych.exception.LHttpException;
import com.ka.lych.exception.LParseException;
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

    public static LFuture<LMap, LHttpException> post(String url, Object request) {
        return post(url, LJson.of(request));
    }
    
    public static LFuture<LMap, LHttpException> post(String url, LJson request) {
        return post(url, request, null, null);
    }
    
    public static LFuture<LMap, LHttpException> post(String url, Object request, String user, String password) {
        return post(url, LJson.of(request), user, password);
    }
    
    public static LFuture<LMap, LHttpException> post(String url, LJson request, String user, String password) {
        return LFuture.<LMap, LHttpException>execute((LTask<LMap, LHttpException> task) -> {
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
                LLog.test("http request: %s ", r);
                byte[] out = r.getBytes(StandardCharsets.UTF_8);
                OutputStream stream = http.getOutputStream();
                LLog.test("http request - b");
                stream.write(out);
                LLog.test("http request - c");
                if (http.getResponseCode() == LHttpStatus.OK.value()) {
                    LLog.test("http request - d");
                    return (LMap) LJsonParser.of(LMap.class).inputStream(http.getInputStream()).parse();
                } else {
                    LLog.test("http request - e");
                    var e = LJsonParser.of(LMap.class).inputStream(http.getErrorStream()).parse();
                    LLog.test("http request - f: %s", e);
                    throw new LHttpException("Server returned failure response code: %s / Reason: %s / %s", http.getResponseCode(), LHttpStatus.valueOf(http.getResponseCode()).getReasonPhrase(), http.getInputStream());
                }
            } catch (URISyntaxException use) {
                throw new LHttpException(use);
            } catch (IOException ioe) {
                throw new LHttpException(ioe);
            } catch (LParseException lpe) {
                throw new LHttpException(lpe);
            }
        });
    }
}
