package com.hirshi001.gwtnetworking;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.network.client.BaseClient;
import com.hirshi001.networking.network.client.Client;
import com.hirshi001.networking.network.client.ClientOption;
import com.hirshi001.networking.networkdata.NetworkData;
import com.hirshi001.restapi.RestAPI;
import com.hirshi001.restapi.RestFuture;
import com.hirshi001.restapi.ScheduledExec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class GWTClient extends BaseClient {

    GWTChannel channel;

    Scheduler scheduler;

    Map<ClientOption, Object> options;

    CheckTCPCommand checkTCPCommand;

    boolean secure = false;

    public GWTClient(ScheduledExec exec, NetworkData networkData, BufferFactory bufferFactory, String host, int port) {
        super(exec, networkData, bufferFactory, host, port);
        scheduler = Scheduler.get();
        options = new HashMap<>();
    }

    public GWTClient(ScheduledExec exec, NetworkData networkData, BufferFactory bufferFactory, String host, int port, boolean secure) {
        super(exec, networkData, bufferFactory, host, port);
        scheduler = Scheduler.get();
        options = new HashMap<>();
        this.secure = secure;
    }

    public boolean isSecure(){
        return secure;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    protected void activateOption(ClientOption option, Object value){
        if(option==ClientOption.TCP_PACKET_CHECK_INTERVAL){
            if(checkTCPCommand!=null) {
                checkTCPCommand.stop();
                checkTCPCommand = null;
            }
            Integer interval = (Integer) value;
            if(interval==null) interval=0;
            if(interval<0) {
                channel.autoHandlePackets(false);
                return;
            }
            if(interval==0) {
                // automatically handle packets once bytes are received
                channel.autoHandlePackets(true);
            }else {
                channel.autoHandlePackets(false);
                checkTCPCommand = new CheckTCPCommand(interval);
                scheduler.scheduleFixedDelay(checkTCPCommand, interval);
            }
        }
    }

    @Override
    protected void setReceiveBufferSize(int size) {
        // do nothing as GWT does not support udp
    }

    @Override
    public RestFuture<?, Client> startTCP() {
        return RestAPI.create((future, nullInput) -> {
            if(channel==null){
                channel = new GWTChannel(this, getExecutor(), getHost(), getPort());
                if(channelInitializer!=null) channelInitializer.initChannel(channel);
            }
            getChannel().startTCP().onFailure(future::setCause).then((c)->future.taskFinished(this)).perform();
        });
    }

    @Override
    public RestFuture<?, Client> startUDP() {
        return Unsupported.createUDP();
    }

    @Override
    public RestFuture<?, Client> stopTCP() {
        return RestAPI.create( ()->{
            if(channel!=null) channel.stopTCP().perform();
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

    class CheckTCPCommand implements Scheduler.RepeatingCommand {

        private final int interval;
        private boolean shouldStop = false;

        public CheckTCPCommand(int interval) {
            this.interval = interval;
        }

        @Override
        public boolean execute() {
            if(shouldStop) return false;

            // check tcp packets
            checkTCPPackets();

            return true;
        }

        public void stop(){
            shouldStop = true;
        }
    }
}
