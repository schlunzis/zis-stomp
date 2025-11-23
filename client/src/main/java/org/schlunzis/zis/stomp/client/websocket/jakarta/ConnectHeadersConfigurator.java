package org.schlunzis.zis.stomp.client.websocket.jakarta;

import jakarta.websocket.ClientEndpointConfig;

import java.util.List;
import java.util.Map;

/// Configurator to add custom headers to the WebSocket connection request.
///
/// This is primarily used to authenticate the WebSocket connection with the STOMP server via HTTP Basic.
///
/// @see JakartaWebsocketClient#connect(Map)
final class ConnectHeadersConfigurator extends ClientEndpointConfig.Configurator {

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
