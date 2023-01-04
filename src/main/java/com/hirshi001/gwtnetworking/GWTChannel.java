package com.hirshi001.gwtnetworking;

import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketException;
import com.github.czyzby.websocket.serialization.Serializer;
import com.google.gwt.core.client.Scheduler;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.networking.network.channel.BaseChannel;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.network.client.Client;
import com.hirshi001.networking.network.networkside.NetworkSide;
import com.hirshi001.restapi.RestAPI;
import com.hirshi001.restapi.RestFuture;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

public class GWTChannel extends BaseChannel {

    private WebSocket webSocket;
    private WebSocketListener listener;
    private String url;
    private String ip;
    private int port;

    private final ByteBuffer receiveTCPBuffer;

    private static final byte[] noAddress = new byte[0];
    private final byte[] sendBuffer;


    public GWTChannel(NetworkSide networkSide, ScheduledExecutorService executor, String ip, int port) {
        super(networkSide, executor);
        url = WebSockets.toWebSocketUrl(ip, port);
        sendBuffer = new byte[256];
        receiveTCPBuffer = getSide().getBufferFactory().circularBuffer(256);
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public byte[] getAddress() {
        return noAddress;
    }

    @Override
    public RestFuture<?, Channel> startTCP() {
        return RestAPI.create( (future, inputNull)->{
            webSocket = WebSockets.newSocket(url);
            listener = new WebSocketAdapter(){
                @Override
                public boolean onOpen(WebSocket webSocket) {
                    future.taskFinished(GWTChannel.this);
                    getListenerHandler().onTCPConnect(GWTChannel.this);
                    return super.onOpen(webSocket);
                }

                @Override
                public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
                    getListenerHandler().onTCPDisconnect(GWTChannel.this);
                    return super.onClose(webSocket, closeCode, reason);
                }

                @Override
                public boolean onMessage(WebSocket webSocket, String packet) {
                    return super.onMessage(webSocket, packet);
                }

                @Override
                public boolean onMessage(WebSocket webSocket, byte[] packet) {
                    receiveTCPBuffer.writeBytes(packet);
                    return super.onMessage(webSocket, packet);
                }

                @Override
                public boolean onError(WebSocket webSocket, Throwable error) {
                    return super.onError(webSocket, error);
                }
            };
            webSocket.addListener(listener);
            try {
                webSocket.connect();
            }catch (Exception e){
                future.setCause(e);
            }
        });
    }

    @Override
    public RestFuture<?, Channel> stopTCP() {
        return RestAPI.create( ()->{
            if(webSocket!=null) webSocket.close();
            return this;
        });
    }

    @Override
    public RestFuture<?, Channel> startUDP() {
        return Unsupported.createUDP();
    }

    @Override
    public RestFuture<?, Channel> stopUDP() {
        return Unsupported.createUDP();
    }

    @Override
    public boolean isTCPOpen() {
        return webSocket != null && webSocket.isOpen();
    }

    @Override
    public boolean isUDPOpen() {
        return false;
    }

    @Override
    public void checkTCPPackets() {
        if(isTCPClosed()) return;
        onTCPBytesReceived(receiveTCPBuffer);
    }

    @Override
    public void checkUDPPackets() {
        // does nothing
    }

    @Override
    protected void writeAndFlushTCP(ByteBuffer buffer) {
        if(isTCPOpen()) {
            while(buffer.readableBytes()>=sendBuffer.length){
                buffer.readBytes(sendBuffer);
                webSocket.send(sendBuffer);
            }
            if(buffer.readableBytes() > 0){
                byte[] bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
                webSocket.send(bytes);
            }
        }
    }

    @Override
    protected void writeAndFlushUDP(ByteBuffer buffer) {
        // do nothing
    }


}
