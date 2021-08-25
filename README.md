# configurable-loadbalancer
A small library to override Spring Cloud Load Balancer, creates service mapper from configuration.

*Scenario*: You develop microservices and deploy all services to remote Kubernetes, and you want to run and debug one service locally, the remaining services in cloud.

It's also run `kubectl port-forward` to forward port from remote Kubernetes, so you need permission to run port-forward.

Including library in your Spring Boot project:
```
    <dependency>
      <groupId>solutions.bkit</groupId>
      <artifactId>configurable-load-balanced</artifactId>
      <version>0.0.1</version>
    </dependency>
```

Given following configurations in `application.yml`:
```
property-source-lb:
  enabled: true
  services:
    "say-hello": :9999:80
    "goodbye": host.docker.internal:10000:8080
```

Then the library will:
* Map service id `say-hello` to `localhost:9999` and run command line `kubectl port-forward svc/say-hello 9999:80`
* Map service id `goodbye` to `host.docker.internal:10000` and run command line `kubectl port-forward svc/goodbye 10000:8080` (obviously it doesn't work in Docker)
