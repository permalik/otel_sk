/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.examples.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A simple client that requests a greeting from the {@link HelloWorldServer}. */
public class HelloWorldClient {

  private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());
  private static final OpenTelemetry openTelemetry =
      ExampleConfiguration.initializeOpenTelemetry("localhost", 9411);
  private final ManagedChannel channel;
  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  // OTel Tracing API
  private final Tracer tracer =
      openTelemetry.getTracer("io.grpc.examples.helloworld.HelloWorldClient");

  public HelloWorldClient() {
    GrpcTelemetry grpcTelemetry = GrpcTelemetry.create(openTelemetry);
    this.channel =
        ManagedChannelBuilder.forAddress("localhost", 50051)
            .usePlaintext()
            .intercept(grpcTelemetry.newClientInterceptor())
            .build();
    this.blockingStub = GreeterGrpc.newBlockingStub(this.channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** Say hello to server. */
  public void greet(String name) {
    logger.info("Will try to greet " + name + " ...");
    HelloRequest request = HelloRequest.newBuilder().setName(name).build();
    HelloReply response;
    try {
      response = blockingStub.sayHello(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());
  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting. The second argument is the target server.
   */
  public static void main(String[] args) throws Exception {
    HelloWorldClient client = new HelloWorldClient();
    try {
      client.greet("world");
    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
      // resources the channel should be shut down when it will no longer be used. If it may be used
      // again leave it running.
      client.shutdown();
    }
  }
}
