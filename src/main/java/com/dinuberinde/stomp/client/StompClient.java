package com.dinuberinde.stomp.client;

import com.dinuberinde.stomp.client.exceptions.InternalFailureException;
import com.dinuberinde.stomp.client.exceptions.NetworkExceptionResponse;
import com.dinuberinde.stomp.client.internal.*;
import com.dinuberinde.stomp.client.internal.stomp.StompCommand;
import com.dinuberinde.stomp.client.internal.stomp.StompMessageHelper;
import com.ka.lych.util.LLog;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public final class StompClient implements AutoCloseable {

    final String _url;
    final String _clientId;

    /**
     * The websockets subscriptions open so far with this client, per topic.
     */
    private final ConcurrentHashMap<String, Subscription> internalSubscriptions;

    /**
     * The websockets queues where the results are published and consumed, per
     * topic.
     */
    private final ConcurrentHashMap<String, BlockingQueue<Object>> queues;

    /**
     * Lock to synchronize the initial connection of the websocket client.
     */
    private final Object CONNECTION_LOCK = new Object();

    /**
     * Boolean to track whether the client is connected.
     */
    private boolean isClientConnected = false;

    /**
     * The websocket instance.
     */
    private WebSocket webSocket;

    /**
     * It construct an instance of the StompClient.
     *
     * @param url the url of the webSocket endpoint, e.g ws://localhost:8080
     */
    public StompClient(String url, String clientId) {
        _url = url;
        _clientId = clientId;
        this.internalSubscriptions = new ConcurrentHashMap<>();
        this.queues = new ConcurrentHashMap<>();
    }

    /**
     * It opens a webSocket connection and connects to the STOMP endpoint.
     */
    public void connect() {
        connect(null, null, null);
    }

    /**
     * It opens a webSocket connection and connects to the STOMP endpoint.
     *
     * @param onStompConnectionOpened handler for a successful STOMP endpoint
     * connection
     */
    public void connect(Callback onStompConnectionOpened) {
        connect(onStompConnectionOpened, null, null);
    }

    /**
     * It opens a webSocket connection and connects to the STOMP endpoint.
     *
     * @param onStompConnectionOpened handler for a successful STOMP endpoint
     * connection
     * @param onWebSocketFailure handler for the webSocket connection failure
     * due to an error reading from or writing to the network
     */
    public void connect(Callback onStompConnectionOpened, Callback onWebSocketFailure) {
        connect(onStompConnectionOpened, onWebSocketFailure, null);
    }

    /**
     * It opens a webSocket connection and connects to the STOMP endpoint.
     *
     * @param onStompConnectionOpened handler for a successful STOMP endpoint
     * connection
     * @param onWebSocketFailure handler for the webSocket connection failure
     * due to an error reading from or writing to the network
     * @param onWebSocketClosed handler for the webSocket connection when both
     * peers have indicated that no more messages will be transmitted and the
     * connection has been successfully released.
     */
    public void connect(Callback onStompConnectionOpened, Callback onWebSocketFailure, Callback onWebSocketClosed) {
        LLog.notification("[Stomp client] Connecting to %s", _url);

        try {

            webSocket = HttpClient
                    .newHttpClient()
                    .newWebSocketBuilder()
                    .header("uuid", _clientId)
                    .buildAsync(URI.create(_url), new WebSocket.Listener() {

                        @Override
                        public void onOpen(WebSocket webSocket) {
                            LLog.notification("[Stomp client] Connected to server");
                            WebSocket.Listener.super.onOpen(webSocket);
                            webSocket.sendText(StompMessageHelper.buildConnectMessage(), true);
                            LLog.notification("[Stomp client] Connect message sent to server");
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            LLog.notification("[Stomp client] Received message %s / %s", data, last);

                            try {
                                Message message = StompMessageHelper.parseStompMessage(data.toString());
                                String payload = message.getPayload();
                                StompCommand command = message.getCommand();

                                switch (command) {

                                    case CONNECTED:
                                        LLog.notification("[Stomp client] Connected to stomp session");
                                        if (onStompConnectionOpened != null) {
                                            onStompConnectionOpened.invoke();
                                        }
                                        emitClientConnected();
                                        break;

                                    case RECEIPT:
                                        String destination = message.getStompHeaders().getDestination();
                                        LLog.notification("[Stomp client] Subscribed to topic " + destination);

                                        Subscription subscription = internalSubscriptions.get(destination);
                                        if (subscription == null) {
                                            throw new NoSuchElementException("Topic not found");
                                        }

                                        subscription.emitSubscription();
                                        break;

                                    case ERROR:
                                        LLog.notification("[Stomp client] STOMP Session Error: " + payload);

                                        // clean-up client resources because the server closed the connection
                                        close();
                                        if (onWebSocketFailure != null) {
                                            onWebSocketFailure.invoke();
                                        }
                                        break;

                                    case MESSAGE:
                                        destination = message.getStompHeaders().getDestination();
                                        LLog.notification("[Stomp client] Received message from topic " + destination);
                                        handleStompDestinationResult(payload, destination);
                                        break;
                                    default:
                                        LLog.notification("unexpected stomp message " + command);
                                        break;
                                }
                            } catch (Exception e) {
                                LLog.error("[Stomp client] Got an exception while handling message", e);
                                e.printStackTrace();
                            }
                            //return new CompletableFuture().newIncompleteFuture().thenAccept(System.out::println);
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }

                        @Override
                        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                            return WebSocket.Listener.super.onBinary(webSocket, data, last);
                        }

                        @Override
                        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
                            return WebSocket.Listener.super.onPing(webSocket, message);
                        }

                        @Override
                        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
                            return WebSocket.Listener.super.onPong(webSocket, message);
                        }

                        @Override
                        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                            LLog.notification("[Stomp client] WebSocket session closed");
                            if (onWebSocketClosed != null) {
                                onWebSocketClosed.invoke();
                            }
                            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            LLog.error("[Stomp client] WebSocket Session error", error);
                            error.printStackTrace();

                            close();
                            if (onWebSocketFailure != null) {
                                onStompConnectionOpened.invoke();
                            }
                        }
                    })
                    .join();
            /*webSocket = new WebSocketFactory()
                    .setConnectionTimeout(30 * 1000)
                    .createSocket(url)
                    .addHeader("uuid", clientKey)
                    .addListener(new WebSocketAdapter() {
                        
                        
                        
                        @Override
                        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                            LOGGER.info("[Stomp client] Connected to server");

                            // we open the stomp session
                            websocket.sendText(StompMessageHelper.buildConnectMessage());
                        }

                        @Override
                        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                            LOGGER.info("[Stomp client] WebSocket session closed");
                            if (onWebSocketClosed != null) {
                                onWebSocketClosed.invoke();
                            }
                        }

                        public void onTextMessage(WebSocket websocket, String messageTxt) {
                            LOGGER.info("[Stomp client] Received message");

                            try {
                                Message message = StompMessageHelper.parseStompMessage(messageTxt);
                                String payload = message.getPayload();
                                StompCommand command = message.getCommand();

                                switch (command) {

                                    case CONNECTED:
                                        LOGGER.info("[Stomp client] Connected to stomp session");
                                        if (onStompConnectionOpened != null) {
                                            onStompConnectionOpened.invoke();
                                        }
                                        emitClientConnected();
                                        break;

                                    case RECEIPT:
                                        String destination = message.getStompHeaders().getDestination();
                                        LOGGER.info("[Stomp client] Subscribed to topic " + destination);

                                        Subscription subscription = internalSubscriptions.get(destination);
                                        if (subscription == null) {
                                            throw new NoSuchElementException("Topic not found");
                                        }

                                        subscription.emitSubscription();
                                        break;

                                    case ERROR:
                                        LOGGER.info("[Stomp client] STOMP Session Error: " + payload);

                                        // clean-up client resources because the server closed the connection
                                        close();
                                        if (onWebSocketFailure != null) {
                                            onWebSocketFailure.invoke();
                                        }
                                        break;

                                    case MESSAGE:
                                        destination = message.getStompHeaders().getDestination();
                                        LOGGER.info("[Stomp client] Received message from topic " + destination);
                                        handleStompDestinationResult(payload, destination);
                                        break;

                                    default:
                                        LOGGER.info("unexpected stomp message " + command);
                                        break;
                                }
                            }
                            catch (Exception e) {
                                LOGGER.info("[Stomp client] Got an exception while handling message");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                            LOGGER.info("[Stomp client] WebSocket Session error");
                            cause.printStackTrace();

                            close();
                            if (onWebSocketFailure != null) {
                                onStompConnectionOpened.invoke();
                            }
                        }
                    })
                    .connect();*/
        } catch (Exception e) {
            LLog.error(e.getMessage(), e);
        }
        //awaitClientConnection();
    }

    /**
     * Emits that the client is connected.
     */
    private void emitClientConnected() {
        synchronized (CONNECTION_LOCK) {
            CONNECTION_LOCK.notify();
        }
    }

    /**
     * Awaits if necessary until the websocket client is connected.
     */
    private void awaitClientConnection() {
        synchronized (CONNECTION_LOCK) {
            if (!isClientConnected)
                try {
                CONNECTION_LOCK.wait();
                isClientConnected = true;
            } catch (InterruptedException e) {
                LLog.error(e.getMessage(), e);
                throw new InternalFailureException("unexpected exception " + e.getMessage());
            }
        }
    }

    /**
     * It handles a STOMP result message of a destination.
     *
     * @param result the result
     * @param destination the destination
     */
    private void handleStompDestinationResult(String result, String destination) {
        Subscription subscription = internalSubscriptions.get(destination);
        if (subscription != null) {
            ResultHandler<?> resultHandler = subscription.getResultHandler();

            if (resultHandler.getResultTypeClass() == Void.class || result == null || result.equals("null")) {
                resultHandler.deliverNothing();
            } else {
                resultHandler.deliverResult(result);
            }
        }
    }

    /**
     * It sends a payload to the standard "user" topic destination by performing
     * an initial subscription and then it waits for the result. The
     * subscription is recycled.
     *
     * @param <T> the type of the expected result
     * @param topicDestination the topic destination
     * @param resultType the result type
     */
    public <T> T send(String topicDestination, Class<T> resultType) {
        return send(topicDestination, resultType);
    }

    /**
     * It sends a message payload to the standard "user" topic destination by
     * performing an initial subscription and then it waits for the result. The
     * subscription is recycled.
     *
     * @param <T> the type of the expected result
     * @param <P> the type of the payload
     * @param topicDestination the topic destination
     * @param resultType the result type
     * @param payload the payload
     */
    @SuppressWarnings("unchecked")
    public <T, P> T send(String topicDestination, Class<T> resultType, P payload) throws InterruptedException, NetworkExceptionResponse {
        LLog.notification("[Stomp client] Subscribing to %s", topicDestination);

        String resultTopic = "/user/" + _clientId + topicDestination;
        Object result;

        BlockingQueue<Object> queue = queues.computeIfAbsent(topicDestination, _key -> new LinkedBlockingQueue<>(1));
        synchronized (queue) {
            subscribe(resultTopic, resultType, queue);

            LLog.notification("[Stomp client] Sending payload to topic destination %s", topicDestination);
            webSocket.sendText(StompMessageHelper.buildSendMessage(topicDestination, payload), true);
            result = queue.take();
        }

        if (result instanceof Nothing) {
            return null;
        } else if (result instanceof ErrorModel) {
            throw new NetworkExceptionResponse((ErrorModel) result);
        } else {
            return (T) result;
        }
    }

    /**
     * Subscribes to a topic providing a {@link BiConsumer} handler to handle
     * the result published by the topic. The subscription is recycled and the
     * method awaits for the subscription to complete.
     *
     * @param topic the topic destination
     * @param resultType the result type
     * @param handler handler of the result
     * @param <T> the result type
     */
    public <T> void subscribeToTopic(String topic, Class<T> resultType, BiConsumer<T, ErrorModel> handler) {
        Subscription subscription = internalSubscriptions.computeIfAbsent(topic, _topic -> {

            ResultHandler<T> resultHandler = new ResultHandler<>(resultType) {
                @Override
                public void deliverResult(String payload) {
                    try {
                        handler.accept(this.toModel(payload), null);
                    } catch (InternalFailureException e) {
                        deliverError(new ErrorModel(e.getMessage() != null ? e.getMessage() : "Got a deserialization error", InternalFailureException.class.getName()));
                    }
                }

                @Override
                public void deliverError(ErrorModel errorModel) {
                    handler.accept(null, errorModel);
                }

                @Override
                public void deliverNothing() {
                    handler.accept(null, null);
                }
            };

            return subscribeInternal(topic, resultHandler);
        });
        subscription.awaitSubscription();
    }

    /**
     * It sends a payload to a previous subscribed topic. The method
     * {@link #subscribeToTopic(String, Class, BiConsumer)}} is used to
     * subscribe to a topic.
     *
     * @param topic the topic
     * @param payload the payload
     */
    public <T> void sendToTopic(String topic, T payload) {
        LLog.notification("[Stomp client] Sending to topic %s", topic);
        webSocket.sendText(StompMessageHelper.buildSendMessage(topic, payload), true);
    }

    /**
     * Subscribes to a topic and register its queue where to deliver the result.
     *
     * @param topic the topic
     * @param resultType the result type
     * @param queue the queue
     * @param <T> the result type
     */
    private <T> void subscribe(String topic, Class<T> resultType, BlockingQueue<Object> queue) {
        Subscription subscription = internalSubscriptions.computeIfAbsent(topic, _topic -> subscribeInternal(topic, new ResultHandler<>(resultType) {
            @Override
            public void deliverResult(String payload) {
                try {
                    deliverInternal(this.toModel(payload));
                } catch (Exception e) {
                    deliverError(new ErrorModel(e.getMessage() != null ? e.getMessage() : "Got a deserialization error", InternalFailureException.class.getName()));
                }
            }

            @Override
            public void deliverError(ErrorModel errorModel) {
                deliverInternal(errorModel);
            }

            @Override
            public void deliverNothing() {
                deliverInternal(Nothing.INSTANCE);
            }

            private void deliverInternal(Object result) {
                try {
                    queue.put(result);
                } catch (Exception e) {
                    LLog.error(e.getMessage(), e);
                }
            }
        }));
        subscription.awaitSubscription();
    }
    
    public void subscribe(String topic) {
        webSocket.sendText(StompMessageHelper.buildSubscribeMessage(topic, "100"), true);
    }

    /**
     * Internal method to subscribe to a topic. The subscription is recycled.
     *
     * @param topic the topic
     * @param handler the result handler of the topic
     * @return the subscription
     */
    private Subscription subscribeInternal(String topic, ResultHandler<?> handler) {
        String subscriptionId = "" + (internalSubscriptions.size() + 1);
        Subscription subscription = new Subscription(topic, subscriptionId, handler);
        webSocket.sendText(StompMessageHelper.buildSubscribeMessage(subscription.getTopic(), subscription.getSubscriptionId()), true);

        return subscription;
    }

    /**
     * It unsubscribes from a topic.
     *
     * @param subscription the subscription
     */
    private void unsubscribeFrom(Subscription subscription) {
        LLog.notification("[Stomp client] Unsubscribing from '%s'", subscription.getTopic());
        webSocket.sendText(StompMessageHelper.buildUnsubscribeMessage(subscription.getSubscriptionId()), true);
    }

    @Override
    public void close() {
        LLog.notification("[Stomp client] Closing webSocket session");
        internalSubscriptions.values().forEach(this::unsubscribeFrom);
        internalSubscriptions.clear();

        // indicates a normal closure
        webSocket.sendClose(1000, null);
        //webSocket.disconnect(1000);
    }

    /**
     * Special object to wrap a NOP.
     */
    private static class Nothing {

        private final static Nothing INSTANCE = new Nothing();

        private Nothing() {
        }
    }

}
