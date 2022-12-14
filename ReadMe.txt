NIOTcpServer [Non blocking server]
==================================

Note: Compile with Java 8. Run on Java 8 or above.

Usage:
Event propagation (Channel handler chain):
    1. Connect and read events propagate from first to last channel handlers.
    2. Write and close events propagate from last to first channel handlers (reverse order).
    3. next method of Context should be called only at the end of channel handler callback methods.
        When next is called, the current context is invalidated and event is propagated to
        subsequent channel handlers.
        If current context methods are called after next is called, they are ignored (NoOp).

Design notes:
    1. Event loop mechanism.
    2. Thread per selector.
    3. Processing as of now is carried out in event loop itself.
    4. Design permits and can evolve to offload heavy processing to another thread pool.
        As of now this is not implemented.


Http codec: Minimalistic and lenient implementation is given for demonstration purpose.
                Not all status codes are implemented. Most of the use cases are covered.
                For status codes, refer: src/main/java/ramana/example/niotcpserver/codec/http/Util.java

SSL notes: If self-signed certificate expires, it has to be regenerated.
    Keystore path: src/main/resources/keyStore.jks
    For passphrase: Refer: src/main/java/ramana/example/niotcpserver/util/SslContextUtil.java

For examples and test cases:
    Refer test folder: src/test/java
    Few samples to start with:
        1. NIOTcpServer: src/test/java/ramana/example/niotcpserver/example/echo
        2. Http codec:
                src/test/java/ramana/example/niotcpserver/example/codec/http
                src/test/java/ramana/example/niotcpserver/example/codec/http/v1
        3. SSL: src/test/java/ramana/example/niotcpserver/example/codec/http/v1/ssl


