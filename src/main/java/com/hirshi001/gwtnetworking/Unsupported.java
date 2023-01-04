package com.hirshi001.gwtnetworking;

import com.hirshi001.restapi.RestAPI;
import com.hirshi001.restapi.RestFuture;

public class Unsupported {

    public static <A> RestFuture<A, A> createUDP(){
        return RestAPI.create(()->{
            throw new UnsupportedOperationException("UDP is not supported in GWT");
        });
    }

    public static UnsupportedOperationException throwUDP(){
        return new UnsupportedOperationException("UDP is not supported in GWT");
    }

}
