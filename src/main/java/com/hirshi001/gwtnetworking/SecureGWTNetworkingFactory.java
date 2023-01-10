package com.hirshi001.gwtnetworking;

import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.networking.network.client.Client;
import com.hirshi001.networking.networkdata.NetworkData;

public class SecureGWTNetworkingFactory extends GWTNetworkingFactory {

    @Override
    public Client createClient(NetworkData networkData, BufferFactory bufferFactory, String host, int port) {
        return new GWTClient(networkData, bufferFactory, host, port, true);
    }

}
