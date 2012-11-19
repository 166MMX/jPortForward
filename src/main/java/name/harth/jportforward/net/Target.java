package name.harth.jportforward.net;

public class Target
{
    private String address;
    private String protocol;
    private int    port;

    @SuppressWarnings("UnusedDeclaration")
    public void setAddress(String address)
    {
        this.address = address;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getAddress()
    {
        return address;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getProtocol()
    {
        return protocol;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getPort()
    {
        return port;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPort(int port)
    {
        this.port = port;
    }
}
