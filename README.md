# k8s-port-forward-starter

*Scenario*: You develop microservices using Spring Cloud and deploy all services to remote Kubernetes, and you want to run and debug one service locally, the remaining services in cloud.

## Prerequisite
* `kubectl` in $PATH
* Has Kubernetes port-forward service permission 
* Authenticated your Kubernetes cluster

## Getting Started
Including library in your Spring Boot project:
```
    <dependency>
      <groupId>solutions.bkit</groupId>
      <artifactId>k8s-port-forward-starter</artifactId>
      <version>0.0.3</version>
      <optional>true</optional>
    </dependency>
```

Given following configurations in `application-local.yml` and you started application with `--spring.profiles.active=local`:
```
spring:
  cloud:
    discovery:
      client:
        simple:
          instances:
            say-hello:
              - host: localhost
                port: 8090
                svc-port: 8080
            goodbye:
              - uri: http://localhost:9091
          order: -1000000
k8s-port-forward:
  enabled: true
```
**Troubleshoot**: If your application throws error `Reason: No setter found for property: port`, please use property `uri: http://localhost:${port}` in configurations:
```
spring:
  cloud:
    discovery:
      client:
        simple:
          instances:
            say-hello:
              - uri: http://localhost:8090
                svc-port: 8080
          order: -1000000
```
## How It Works
1. The above configuration maps services:
   - `say-hello` to `localhost:8090`
   - `goodbye` to `localhost:9091`
2. With `k8s-port-forward.enabled=true`, the application will:
   - run `kubectl port-forward svc/say-hello 8090:8080`
   - service `goodbye` will not run port-ward, since its omitted `goodbye.svc-port`
   
3. Verify: run `ps ax | grep 'kubectl port-forward'` to make sure all services was port-forward