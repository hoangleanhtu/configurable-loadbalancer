# k8s-port-forward-starter

*Scenario*: You develop microservices and deploy all services to remote Kubernetes, and you want to run and debug one service locally, the remaining services in cloud.

## Prerequisite
* `kubectl` in $PATH
* Has Kubernetes port-forward service permission 

## Getting Started
Including library in your Spring Boot project:
```
    <dependency>
      <groupId>solutions.bkit</groupId>
      <artifactId>k8s-port-forward-starter</artifactId>
      <version>0.0.1</version>
      <optional>true</optional>
    </dependency>
```

Given following configurations in `application.yml`:
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
          order: -1000000
k8s-port-forward:
  enabled: true
```
## How It Works
1. The above configuration maps service `say-hello` to `localhost:8090`
2. With `k8s-port-forward.enabled=true`, the application will run `kubectl port-forward svc/say-hello 8090:8080`
