package com.ka.lych.test;

import com.ka.lych.repo.LDataException;
import com.ka.lych.util.LFuture;

public class LFutureTest {

    public static void main(String[] args) {

        System.out.println("> MainThread is: " + Thread.currentThread().hashCode());
        LFuture.<Integer, Throwable>execute(task -> {
            try {
                System.out.println("> SubThread sleeps now: " + Thread.currentThread().hashCode());
                Thread.sleep(5000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("> SubThread finished now: " + Thread.currentThread().hashCode() + ". Result should follow in next line...");
            throw new LDataException(LFutureTest.class, "Holla");
            //return 5;
        })
                .await()
                .then(r -> System.out.println("> Result of SubThread: " + Thread.currentThread().hashCode()))
                .onError(ex -> System.out.println("> SubThread has an error: " + ex.getMessage()));

        /*CompletableFuture.<Integer>supplyAsync(() -> {
            try {
                System.out.println(": SubThread sleeps now: " + Thread.currentThread().hashCode());
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }   
            return 4;
        }).thenAccept(r -> {
            System.out.println(": Result in Thread: " + Thread.currentThread().hashCode() + " is " + r);
        });*/
        
        

        try {
            System.out.println("MainThread sleeps now: " + Thread.currentThread().hashCode());
            Thread.sleep(15000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Exiting main()");
    }

    void notify(String msg) {
        System.out.println("Received message: " + msg + " / " + Thread.currentThread().hashCode());
    }

}

