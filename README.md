# rpc
使用Spring Boot、Nacos和Netty实现的轻量级RPC框架。

## 核心功能
+ 服务透明：框架完整封装了底层通信细节，开发时调用远程服务就像调用本地服务一样，将项目打包成Spring-Boot-Starter，使用时可以通过依赖和注解快速接入。
+ 通讯方式：采用Netty进行网络通信，支持连接空闲检测和心跳机制。
+ 多调用方案：支持Sync、Async、Oneway等多种调用方式。
+ 多序列化算法：支持Kryo、Hessian与Google Protobuf等序列化算法。
+ 负载均衡：支持加权随机、加权轮询、最小活跃数等负载均衡算法。
+ 启动预热：当服务运行时长小于服务预热时间时，对服务进行降权，避免服务在启动之初就处于高负载状态。
+ 异常重试：支持客户端调用超时和异常重试机制，增强了重试的逻辑，实现合理重试。
+ 泛化调用：客户端不需要依赖服务端提供的API接口也可以发起远程调用。