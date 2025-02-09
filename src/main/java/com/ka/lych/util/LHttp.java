package com.ka.lych.util;

import com.ka.lych.exception.LHttpException;
import com.ka.lych.exception.LParseException;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

/**
 *
 * @author klausahrenberg
 */
public abstract class LHttp {

    public static LFuture<LMap, LHttpException> post(String url, LJson request) {
        return post(url, request, null, null);
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
                byte[] out = request.toString().getBytes(StandardCharsets.UTF_8);
                OutputStream stream = http.getOutputStream();
                stream.write(out);
                if (http.getResponseCode() == LHttpStatus.OK.value()) {
                    return (LMap) LJsonParser.of(LMap.class).inputStream(http.getInputStream()).parse();
                } else {
                    throw new LHttpException("Server returned failure response code: %s / Reason: %s", http.getResponseCode(), LHttpStatus.valueOf(http.getResponseCode()).getReasonPhrase());
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
