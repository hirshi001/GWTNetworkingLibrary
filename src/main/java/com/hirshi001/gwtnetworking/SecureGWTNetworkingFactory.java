package com.hirshi001.gwtnetworking;

import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.networking.network.client.Client;
import com.hirshi001.networking.networkdata.NetworkData;
import com.hirshi001.restapi.RestAPI;

public class SecureGWTNetworkingFactory extends GWTNetworkingFactory {

    @Override
    public Client createClient(NetworkData networkData, BufferFactory bufferFactory, String host, int port) {
        return new GWTClient(RestAPI.getDefaultExecutor(), networkData, bufferFactory, host, port, true);
    }

}
