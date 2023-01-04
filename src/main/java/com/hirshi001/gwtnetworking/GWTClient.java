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

import java.util.HashMap;
import java.util.Map;

public class GWTClient extends BaseClient {

    GWTChannel channel;

    Scheduler scheduler;

    Map<ClientOption, Object> options;

    CheckTCPCommand checkTCPCommand;


    public GWTClient(NetworkData networkData, BufferFactory bufferFactory, String host, int port) {
        super(networkData, bufferFactory, host, port);
        scheduler = Scheduler.get();
        options = new HashMap<>();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public <T> void setClientOption(ClientOption<T> option, T value) {
        options.put(option, value);
        activateOption(option, value);
    }

    protected void activateOption(ClientOption option, Object value){
        if(option==ClientOption.TCP_PACKET_CHECK_INTERVAL){
            if(checkTCPCommand!=null) {
                checkTCPCommand.stop();
                checkTCPCommand = null;
            }
            Integer interval = (Integer) value;
            if(interval==null || interval<0) return;
            checkTCPCommand = new CheckTCPCommand(interval);
            scheduler.scheduleFixedDelay(checkTCPCommand, interval);
        }
    }



    @Override
    public <T> T getClientOption(ClientOption<T> option) {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean tcpOpen() {
        return getChannel().isTCPOpen();
    }

    @Override
    public boolean udpOpen() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return getChannel().isOpen();
    }

    @Override
    public void close() {
        getChannel().close().perform();
    }

    @Override
    public RestFuture<?, Client> startTCP() {
        return RestAPI.create(() -> {
            getChannel().startTCP();
            return this;
        });
    }

    @Override
    public RestFuture<?, Client> startUDP() {
        return Unsupported.createUDP();
    }

    @Override
    public RestFuture<?, Client> stopTCP() {
        return null;
    }

    @Override
    public RestFuture<?, Client> stopUDP() {
        return RestAPI.create(() -> {
            throw new UnsupportedOperationException("UDP is not supported in GWT");
        });
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
