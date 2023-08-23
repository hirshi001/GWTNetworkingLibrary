package com.hirshi001.gwtnetworking;

import com.google.gwt.core.client.Scheduler;
import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.network.client.BaseClient;
import com.hirshi001.networking.network.client.Client;
import com.hirshi001.networking.network.client.ClientOption;
import com.hirshi001.networking.networkdata.NetworkData;
import com.hirshi001.restapi.RestAPI;
import com.hirshi001.restapi.RestFuture;
import com.hirshi001.restapi.ScheduledExec;

public class GWTClient extends BaseClient {

    GWTChannel channel;

    Scheduler scheduler;

    boolean secure = false;

    public GWTClient(ScheduledExec exec, NetworkData networkData, BufferFactory bufferFactory, String host, int port) {
        super(exec, networkData, bufferFactory, host, port);
        scheduler = Scheduler.get();
    }

    public GWTClient(ScheduledExec exec, NetworkData networkData, BufferFactory bufferFactory, String host, int port, boolean secure) {
        super(exec, networkData, bufferFactory, host, port);
        scheduler = Scheduler.get();
        this.secure = secure;
    }

    public boolean isSecure() {
        return secure;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    protected void setReceiveBufferSize(int size) {
        // do nothing as GWT does not support udp
    }

    @Override
    public RestFuture<?, Client> startTCP() {
        return RestAPI.create((future, nullInput) -> {
            if (channel == null) {
                channel = new GWTChannel(this, getExecutor(), getHost(), getPort());
                if (channelInitializer != null) channelInitializer.initChannel(channel);
            }
            getChannel().startTCP().onFailure(future::setCause).then((c) -> future.taskFinished(this)).perform();
        });
    }

    @Override
    public RestFuture<?, Client> startUDP() {
        return Unsupported.createUDP();
    }

    @Override
    public RestFuture<?, Client> stopTCP() {
        return RestAPI.create(() -> {
            if (channel != null) channel.stopTCP().perform();
            return GWTClient.this;
        });
    }

    @Override
    public RestFuture<?, Client> stopUDP() {
        return Unsupported.createUDP();
    }


    @Override
    public boolean supportsTCP() {
        return true;
    }

    @Override
    public boolean supportsUDP() {
        return false;
    }

}
