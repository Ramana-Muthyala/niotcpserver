NIOTcpServer
============

Event propagation (Channel handler chain):
    1. Connect and read events propagate from first to last channel handlers.
    2. Write and close events propagate from last to first channel handlers (reverse order).
    3. next method of Context should be called only at the end of channel handler callback methods.
        When next is called, the current context is invalidated and event is propagated to
        subsequent channel handlers.
        If current context methods are called after next is called, they are ignored (NoOp).


For examples and test cases:
    Refer test folder: src/test/java
    Few samples to start with:
        1. NIOTcpServer: src/test/java/ramana/example/niotcpserver/example/echo
        2. Http codec: src/test/java/ramana/example/niotcpserver/example/codec/http



