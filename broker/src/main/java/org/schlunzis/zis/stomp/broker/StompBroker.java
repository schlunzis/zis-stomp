package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;

import java.io.Reader;

/// A STOMP broker that uses an external framework to receive and send messages.
///
/// This broker is mainly designed to work with WebSockets, but other full-duplex communication protocols may also work.
///
/// To use the broker, you need a session adapter class that allows the broker to call methods on the session passed by
/// the framework.
/// It may look like this:
///
/// ```java
/// class SessionAdapter implements StompSessionAdapter<FrameworkSession> {
///     @Override
///     public void send(FrameworkSession session, String message) {
///         session.send(message);
///     }
///
///     @Override
///     public void close(FrameworkSession session) {
///         session.close();
///     }
/// }
/// ```
///
/// You need to make sure that different instances representing the same connection represent that in their `equals`
/// and `hashCode` implementations. If that is not the case for any given framework, you can implement the methods
/// in the adapter to override their semantics.
///
/// After that you can build and use the [StompBroker] as shown below (using a generic server implementation).
///
/// ```java
/// StompBroker<Session> broker = StompBroker.<FrameworkSession>builder()
///     .sessionAdapter(new StompSessionAdapter<FrameworkSession>{...})
///     .hosts("localhost")
///     .build();
///
/// Server server = Server.builder()
///     .configureWebsocket(ws -> ws
///         .onOpen(session -> broker.onOpen(session))
///         .onMessage(session -> broker.onMessage(session, session.reader()))
///         .onClose(session -> broker.onClose(session))
///         .onError(session -> broker.onError(session, session.error()))
///     )
///     .build();
/// broker.start();
/// server.start();
/// ```
///
/// Default implementations are provided for the following frameworks:
///
/// - [Jex](https://avaje.io/jex/) with [org.schlunzis.zis.stomp.broker.connection.jax.JaxSessionAdapter], [org.schlunzis.zis.stomp.broker.connection.jax.JaxConfigurationConsumer]
/// - [Helidon v4](https://helidon.io/docs/v4/se/websocket) with [org.schlunzis.zis.stomp.broker.connection.helidon.HelidonSessionAdapter], [org.schlunzis.zis.stomp.broker.connection.helidon.HelidonWebsocketListener]
///
/// The stomp broker is thread-safe.
///
/// @param <SESSION> the type of session handled by this broker
/// @since 1.0.0
public interface StompBroker<SESSION> extends AutoCloseable {

    /// Creates a new [StompBrokerBuilder] for building a STOMP client.
    ///
    /// Builder instances are not thread-safe and should not be shared between threads.
    ///
    /// @param <SESSION> the type of session handled by the broker
    /// @return a new instance of [StompBrokerBuilder]
    /// @since 1.0.0
    static <SESSION> StompBrokerBuilder<SESSION> builder() {
        return new StompBrokerBuilder<>();
    }

    /// Starts processing of received frames.
    ///
    /// @since 1.0.0
    void start();

    /// To be called when a new connection is opened.
    ///
    /// @param session the new session for the connection
    /// @since 1.0.0
    void onOpen(SESSION session);

    /// To be called when a new message has been received.
    ///
    /// @param session the session the message was received from
    /// @param message the message received
    /// @since 1.0.0
    void onMessage(SESSION session, Reader message);

    /// To be called when an error occurred for the session.
    ///
    /// @param session the session with the error
    /// @param t       a throwable with more information if available. May be `null`
    /// @since 1.0.0
    void onError(SESSION session, @Nullable Throwable t);

    /// To be called when the connection to the client has been closed by the client.
    ///
    /// @param session the session that has been closed
    /// @since 1.0.0
    void onClose(SESSION session);

    /// Stops processing of received frames.
    ///
    /// @since 1.0.0
    void close();

}
