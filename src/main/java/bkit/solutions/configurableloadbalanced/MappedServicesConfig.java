package bkit.solutions.configurableloadbalanced;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConditionalOnProperty(prefix = "property-source-lb", name = "enabled", havingValue = "true")
@PropertySource("classpath:/services-map.properties")
@ConfigurationProperties(prefix = "property-source-lb")
public class MappedServicesConfig {
  private Map<String, String> services;

  public MappedServicesConfig(Map<String, String> services) {
    this.services = services;
  }

  public Map<String, String> getServices() {
    return services;
  }
}
