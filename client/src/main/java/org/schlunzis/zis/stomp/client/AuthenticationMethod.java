package org.schlunzis.zis.stomp.client;

/// Enumeration of supported authentication methods.
///
/// @since 1.0.0
public enum AuthenticationMethod {

    /// Login and passcode authentication as defined in the STOMP protocol using the CONNECT frame.
    ///
    /// @since 1.0.0
    STOMP,

    /// HTTP Basic Authentication using the `Authorization` header with Base64-encoded credentials.
    ///
    /// @since 1.0.0
    HTTP_BASIC

}
