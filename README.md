# rpc

## using_rpc_frameworks
Hello world examples of using RMI, CXF and Axis2.

## serialization
Hello world examples of serializers including Java provided serializer; XML serializer; JSON serializer; ProtoBuf serializer; ProtoStuff serializer; Hessian serilizer; Marshalling serilizer.

## simple-rpc
A very simple rpc server and client implemented based on Java Socket.

## spring-rpc
Integrate spring with off the shlef RPC framework such as RMI, HttpInvoker and Hessian.

## zookeeper
An example of zookeeper client.

## Netty
Echo server and client.

## rpc-framework
Service provider start it self as a Netty server and pushlish its information to ZooKeeper.
Service invoker retrieve the information of all the service providers from ZooKeeper and initiate RPC via Netty Channel.
