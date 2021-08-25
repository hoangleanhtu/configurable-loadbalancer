package bkit.solutions.configurableloadbalanced;

public class HostPortItem {

  private String host;
  private int port;
  private int svcPort;

  public HostPortItem(String host, int port, int svcPort) {
    this.host = host;
    this.port = port;
    this.svcPort = svcPort;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public int getSvcPort() {
    return svcPort;
  }
}
