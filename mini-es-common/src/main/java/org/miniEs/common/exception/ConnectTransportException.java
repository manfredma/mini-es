package org.miniEs.common.exception;

public class ConnectTransportException extends TransportException {

    public ConnectTransportException(String nodeAddress, String reason) {
        super("[][" + nodeAddress + "] connect_exception; " + reason);
    }

    public ConnectTransportException(String nodeAddress, Throwable cause) {
        super("[][" + nodeAddress + "] connect_exception", cause);
    }
}
