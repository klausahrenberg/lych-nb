package com.ka.lych.util;

import com.dinuberinde.stomp.client.StompClient;
import com.dinuberinde.stomp.client.internal.stomp.StompMessageHelper;
import com.ka.lych.exception.LException;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 *
 * @author klausahrenberg
 */
public abstract class LHttp {
    
    public static WebSocketClient STOMP = new WebSocketClient();

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

    public static LFuture<LMap, LException> upload(String url, LJson request, File file) {
        return LFuture.<LMap, LException>execute((LTask<LMap, LException> task) -> {
            var twoHyphens = "--";
            var boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
            var lineEnd = "\r\n";
            var filefield = "test";
            try {
                //var clientId = UUID.randomUUID().toString();
                var c = new LHttpMultipart(url, "UTF-8", false);
                c.setup_writer();
                //c.addFormField("clientId", clientId);
                c.addFormField("task", null);
                c.addFilePart("excelFile", file);
                var http = c.finish();
                http.connect();
                if (http.getResponseCode() == LHttpStatus.OK.value()) {   
                    LMap map = LJsonParser.of(LMap.class).inputStream(http.getInputStream()).parse();
                    LLog.test("upload ok: %s", map.get(ILConstants.KEYWORD_ID));
                    
                    var taskId = map.get(ILConstants.KEYWORD_ID).toString();                    
                    
                    var stompClient = new StompClient("ws://localhost:8080/websocket", taskId);
                    stompClient.connect(() -> {
                        LLog.test("subscribe...");
                        stompClient.subscribe("/topic/ws/" + taskId);
                    });
                    
                    //CountDownLatch latch = new CountDownLatch(1);
                    /*WebSocket ws = HttpClient
                            .newHttpClient()
                            .newWebSocketBuilder()
                            .buildAsync(URI.create("ws://localhost:8080/websocket"), STOMP)
                            .join();*/
                    LLog.test("join");
                    //ws.sendText("Hello!", true);
                    //LLog.test("send hello");
                    /*try {
                        latch.await();
                    } catch (Exception x) {
                        x.printStackTrace();
                    }*/
                    LLog.test("latch awaited");
                    return null;
                    //return (LMap) LJsonParser.of(LMap.class).inputStream(http.getInputStream()).parse();
                } else {
                    LLog.test("upload failed");
                    throw LException.of(LJsonParser.of(LMap.class).inputStream(http.getErrorStream()).parse());
                }
            } catch (URISyntaxException use) {
                throw new LException(use);
            } catch (IOException ioe) {
                throw new LException(ioe);
            }
        });
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static class WebSocketClient implements WebSocket.Listener {
        
        //https://usoar.es/posts/consuming-websocket-with-java-http-client/

        //private final CountDownLatch latch;

        public WebSocketClient() {
            //this.latch = latch;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("onOpen using subprotocol " + webSocket.getSubprotocol());
            WebSocket.Listener.super.onOpen(webSocket);
            webSocket.sendText(StompMessageHelper.buildConnectMessage(), true);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            LLog.test("onText received: %s ", data);
            //latch.countDown();
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("Bad day! " + webSocket.toString());
            WebSocket.Listener.super.onError(webSocket, error);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket,
                int statusCode,
                String reason) {
            System.out.println("Closed " + reason);
            return null;
        }
    }

}
