package org.schlunzis.zis.stomp.client.websocket.jakarta;

import jakarta.websocket.ClientEndpointConfig;

import java.util.List;
import java.util.Map;

class ConnectHeadersConfigurator extends ClientEndpointConfig.Configurator {

    private final Map<String, List<String>> additionalHeaders;

    ConnectHeadersConfigurator(Map<String, List<String>> additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> entry : additionalHeaders.entrySet()) {
            headers.merge(
                    entry.getKey(),
                    entry.getValue(),
                    (oldValues, newValues) -> {
                        oldValues.addAll(newValues);
                        return oldValues;
                    }
            );
        }
    }

}
