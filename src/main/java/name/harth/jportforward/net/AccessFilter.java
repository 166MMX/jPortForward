package name.harth.jportforward.net;

public class AccessFilter {
    private String onMatch;
    private String onMismatch;
    private String cidr;
    private int port;
    private String address;

    public void setOnMatch(String onMatch) {
        this.onMatch = onMatch;
    }

    public String getOnMatch() {
        return onMatch;
    }

    public void setOnMismatch(String onMismatch) {
        this.onMismatch = onMismatch;
    }

    public String getOnMismatch() {
        return onMismatch;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public String getCidr() {
        return cidr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
