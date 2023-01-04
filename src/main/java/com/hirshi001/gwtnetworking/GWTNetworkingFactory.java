package com.hirshi001.gwtnetworking;

import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.networking.network.NetworkFactory;
import com.hirshi001.networking.network.client.Client;
import com.hirshi001.networking.network.server.Server;
import com.hirshi001.networking.networkdata.NetworkData;

import java.io.IOException;

public class GWTNetworkingFactory implements NetworkFactory {

    @Override
    public Server createServer(NetworkData networkData, BufferFactory bufferFactory, int port) throws IOException {
        throw new UnsupportedOperationException("GWT Networking does not support servers");
    }

    @Override
    public Client createClient(NetworkData networkData, BufferFactory bufferFactory, String host, int port) throws IOException {
        return new GWTClient(networkData, bufferFactory, host, port);
    }

}
