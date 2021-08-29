package solutions.bkit.k8sportforward;

import java.net.URI;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortForwardItem {

  private int port;
  private Integer svcPort;
  private URI uri;

  public void setUri(URI uri) {
    this.uri = uri;
    this.port = this.uri.getPort();
  }
}
