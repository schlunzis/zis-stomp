package org.schlunzis.zis.stomp.client;

import java.util.EnumSet;
import java.util.Set;

/// Policy for requesting receipts from the STOMP server.
/// If the policy is enabled for a specific command, the client will request a receipt
/// from the server for that command.
///
/// You can use the [#all()] method to create a policy that requests receipts for all supported commands,
/// or the [#none()] method to create a policy that does not request any receipts.
/// You can also use the [#add(Policy)] method to add specific policies to an existing receipt policy.
///
/// A ReceiptPolicy can be set on the [StompClientBuilder] to define the default receipt policy for the client.
///
/// @since 1.0.0
public final class ReceiptPolicy {

    private final Set<Policy> policies = EnumSet.noneOf(Policy.class);

    private ReceiptPolicy() {
    }

    /// Creates a receipt policy that requests receipts for all supported commands.
    ///
    /// @return a receipt policy that requests receipts for all supported commands
    /// @since 1.0.0
    public static ReceiptPolicy all() {
        ReceiptPolicy policy = new ReceiptPolicy();
        policy.policies.addAll(EnumSet.allOf(Policy.class));
        return policy;
    }

    /// Creates a receipt policy that does not request any receipts.
    ///
    /// @return a receipt policy that does not request any receipts
    /// @since 1.0.0
    public static ReceiptPolicy none() {
        return new ReceiptPolicy();
    }

    /// Adds a policy to request receipts for the specified command.
    ///
    /// @param policy the policy to add
    /// @return the receipt policy instance
    /// @since 1.0.0
    public ReceiptPolicy add(Policy policy) {
        this.policies.add(policy);
        return this;
    }

    /// Checks if the receipt policy is enabled for the specified command.
    ///
    /// @param policy the policy to check
    /// @return true if the receipt policy is enabled for the specified command, false otherwise
    /// @since 1.0.0
    public boolean isEnabled(Policy policy) {
        return this.policies.contains(policy);
    }

    /// Policies for requesting receipts from the STOMP server.
    ///
    /// @since 1.0.0
    public enum Policy {
        /// Request receipt for SEND commands.
        FOR_SEND,
        /// Request receipt for SUBSCRIBE commands.
        FOR_SUBSCRIBE,
        /// Request receipt for UNSUBSCRIBE commands.
        FOR_UNSUBSCRIBE,
        /// Request receipt for DISCONNECT commands.
        FOR_DISCONNECT,
    }

}
