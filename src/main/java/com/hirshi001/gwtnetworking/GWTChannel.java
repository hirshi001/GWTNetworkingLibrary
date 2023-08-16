package com.hirshi001.gwtnetworking;

import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.networking.network.channel.BaseChannel;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.network.networkside.NetworkSide;
import com.hirshi001.restapi.RestAPI;
import com.hirshi001.restapi.RestFuture;
import com.hirshi001.restapi.ScheduledExec;

public class GWTChannel extends BaseChannel {

    private WebSocket webSocket;
    private WebSocketListener listener;
    private final String ip, url;
    private final int port;
    private final ByteBuffer receiveBuffer;


    private static final byte[] noAddress = new byte[0];
    private final byte[] sendBuffer;

    public GWTChannel(NetworkSide networkSide, ScheduledExec executor, String ip, int port) {
        super(networkSide, executor);
        sendBuffer = new byte[256];

        receiveBuffer = getSide().getBufferFactory().circularBuffer(1024);

        this.ip = ip;
        this.port = port;
        if (((GWTClient) getSide()).isSecure()) url = WebSockets.toSecureWebSocketUrl(getIp(), getPort());
        else url = WebSockets.toWebSocketUrl(getIp(), getPort());

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
        return RestAPI.create((future, inputNull) -> {

            webSocket = WebSockets.newSocket(url);
            listener = new WebSocketAdapter() {
                @Override
                public boolean onOpen(WebSocket webSocket) {
                    future.taskFinished(GWTChannel.this);
                    onTCPConnected();
                    return true;
                }

                @Override
                public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
                    onTCPDisconnected();
                    return true;
                }

                @Override
                public boolean onMessage(WebSocket webSocket, byte[] packet) {
                    receiveBuffer.writeBytes(packet);
                    return true;
                }

                @Override
                public boolean onError(WebSocket webSocket, Throwable error) {
                    return super.onError(webSocket, error);
                }
            };
            webSocket.addListener(listener);
            try {
                webSocket.connect();
            } catch (Exception e) {
                future.setCause(e);
            }
        });
    }

    @Override
    public RestFuture<?, Channel> stopTCP() {
        return RestAPI.create(() -> {
            if (webSocket != null) webSocket.close();
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
        if (isTCPClosed()) return;
        onTCPBytesReceived(receiveBuffer);
        super.checkTCPPackets();
    }

    @Override
    protected void writeAndFlushTCP(ByteBuffer buffer) {
        while (buffer.readableBytes() >= sendBuffer.length) {
            buffer.readBytes(sendBuffer);
            webSocket.send(sendBuffer);
        }

        if (buffer.readableBytes() > 0) {
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            webSocket.send(bytes);
        }
    }

    @Override
    protected void writeAndFlushUDP(ByteBuffer buffer) {
        // do nothing
    }


}
