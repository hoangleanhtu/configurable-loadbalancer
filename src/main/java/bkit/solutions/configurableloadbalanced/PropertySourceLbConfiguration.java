package bkit.solutions.configurableloadbalanced;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

/**
 * @author hoangleanhtu@gmail.com
 */
@Configuration
@ConditionalOnProperty(prefix = "property-source-lb", name = "enabled", havingValue = "true")
@Import({MappedServicesConfig.class})
public class PropertySourceLbConfiguration {

  private static final Logger log = LoggerFactory.getLogger(PropertySourceLbConfiguration.class);

  @Bean
  @Primary
  ServiceInstanceListSupplier serviceInstanceListSupplier(
      MappedServicesConfig mappedServicesConfig) {
    final Map<String, HostPortItem> servicesMap = mapToHostPort(mappedServicesConfig);

    return new MappedServiceInstanceListSuppler("local-port-forward",
        servicesMap.entrySet().stream().collect(
            Collectors.toMap(Entry::getKey, it -> Flux.just(Arrays.asList(new DefaultServiceInstance(
                it.getKey(), it.getKey(), it.getValue().getHost(),
                it.getValue().getPort(), false))))));
  }

  @Bean
  @ConditionalOnProperty(prefix = "property-source-lb", name = "auto-port-forward", havingValue = "true", matchIfMissing = true)
  CommandLineRunner run(MappedServicesConfig mappedServicesConfig) {
    return args -> {
      final Map<String, HostPortItem> servicesMap = mapToHostPort(mappedServicesConfig);
      final ExecutorService executorService = Executors.newSingleThreadExecutor();
      servicesMap.forEach((serviceId, hostPort) -> {
        try {
          final String command = String
              .format("kubectl port-forward svc/%s %d:%d", serviceId, hostPort.getPort(), hostPort.getSvcPort());
          log.info("Will execute port-forward [{}]", command);
          final Process process = Runtime.getRuntime().exec(command);
          final StreamGobbler streamGobbler =
              new StreamGobbler(process.getInputStream(), System.out::println);
          executorService.submit(streamGobbler);
        } catch (IOException exception) {
          log.error("port-forward error", exception);
        }
      });
    };
  }

  private static class StreamGobbler implements Runnable {

    private InputStream inputStream;
    private Consumer<String> consumer;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines()
          .forEach(consumer);
    }
  }

  private Map<String, HostPortItem> mapToHostPort(
      MappedServicesConfig mappedServicesConfig) {
    return mappedServicesConfig
        .getServices().entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, it -> {
          final String value = it.getValue();
          final String[] split = value.split(":");
          final String host = split[0].length() == 0 ? "localhost" : split[0];
          final int port = Integer.parseInt(split[1]);
          final int svcPort = split.length == 3 ? Integer.parseInt(split[2]) : 8080;
          return new HostPortItem(host, port, svcPort);
        }));
  }

}

class MappedServiceInstanceListSuppler implements ServiceInstanceListSupplier {

  private final String serviceId;
  private final Map<String, Flux<List<ServiceInstance>>> serviceMap;

  public MappedServiceInstanceListSuppler(String serviceId,
      Map<String, Flux<List<ServiceInstance>>> serviceMap) {
    this.serviceId = serviceId;
    this.serviceMap = serviceMap;
  }

  @Override
  public String getServiceId() {
    return serviceId;
  }

  @Override
  public Flux<List<ServiceInstance>> get(Request request) {
    final DefaultRequestContext context = (DefaultRequestContext) request.getContext();
    final RequestData clientRequest = (RequestData) context.getClientRequest();
    return this.serviceMap.get(clientRequest.getUrl().getHost());
  }

  @Override
  public Flux<List<ServiceInstance>> get() {
    return Flux.just(Arrays
        .asList(new DefaultServiceInstance(serviceId + "1", serviceId, "localhost", 8090, false),
            new DefaultServiceInstance(serviceId + "2", serviceId, "localhost", 9092, false),
            new DefaultServiceInstance(serviceId + "3", serviceId, "localhost", 9999, false)));
  }
}