package name.harth.jportforward.net;

public class AccessFilter
{
    private String onMatch;
    private String onMismatch;
    private String cidr;
    private int    port;
    private String address;

    @SuppressWarnings("UnusedDeclaration")
    public void setOnMatch(String onMatch)
    {
        this.onMatch = onMatch;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getOnMatch()
    {
        return onMatch;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setOnMismatch(String onMismatch)
    {
        this.onMismatch = onMismatch;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getOnMismatch()
    {
        return onMismatch;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setCidr(String cidr)
    {
        this.cidr = cidr;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getCidr()
    {
        return cidr;
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
}
