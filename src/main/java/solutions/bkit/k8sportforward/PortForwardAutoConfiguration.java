package solutions.bkit.k8sportforward;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hoangleanhtu@gmail.com
 */
@Configuration
@ConfigurationProperties(prefix = "k8s-port-forward")
@ConditionalOnProperty(prefix = "k8s-port-forward", name = "enabled", havingValue = "true")
@Slf4j
public class PortForwardAutoConfiguration {

  private Map<String, PortForwardItem> services;

  @Bean
  CommandLineRunner run() {
    return args -> {
      final ExecutorService executorService = Executors.newSingleThreadExecutor();
      log.info("==================================");
      log.info("Will run kubectl port-forward");
      services.forEach((serviceId, hostPort) -> {
        try {
          final String command = String
              .format("kubectl port-forward svc/%s %d:%d", serviceId, hostPort.getLocalPort(), hostPort.getSvcPort());
          log.info("{}", command);
          final Process process = Runtime.getRuntime().exec(command);
          final StreamGobbler streamGobbler =
              new StreamGobbler(process.getInputStream(), System.out::println);
          executorService.submit(streamGobbler);
        } catch (IOException exception) {
          log.error("port-forward error", exception);
        }
      });
      log.info("Finished running kubectl port-forward");
      log.info("==================================");
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

  public Map<String, PortForwardItem> getServices() {
    return services;
  }

  public void setServices(
      Map<String, PortForwardItem> services) {
    this.services = services;
  }

}