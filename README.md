# k8s-port-forward-starter
A small library to run ``kubectl port-forward svc`` from configurations.

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
k8s-port-forward:
  enabled: true
  services:
    "say-hello":
      local-port: 10000
      svc-port: 80
```

Then the library will run command line `kubectl port-forward svc/say-hello 10000:80`
