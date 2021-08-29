package solutions.bkit.k8sportforward;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortForwardItem {

  private int localPort;
  private int svcPort;
}
