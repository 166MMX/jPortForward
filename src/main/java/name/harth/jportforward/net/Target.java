package name.harth.jportforward.net;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Target implements InitializingBean, DisposableBean
{
    private String address;
    private String protocol;
    private int    port;

    private InetAddress inetAddress;
    private InetSocketAddress inetSocketAddress;

    @Override
    public void destroy() throws Exception
    {
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        inetAddress = InetAddress.getByName(address);
        inetSocketAddress = new InetSocketAddress(inetAddress, port);
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

    @SuppressWarnings("UnusedDeclaration")
    public InetAddress getInetAddress()
    {
        return inetAddress;
    }

    @SuppressWarnings("UnusedDeclaration")
    private void setInetAddress(InetAddress inetAddress)
    {
        this.inetAddress = inetAddress;
    }

    @SuppressWarnings("UnusedDeclaration")
    public InetSocketAddress getInetSocketAddress()
    {
        return inetSocketAddress;
    }

    @SuppressWarnings("UnusedDeclaration")
    private void setInetSocketAddress(InetSocketAddress inetSocketAddress)
    {
        this.inetSocketAddress = inetSocketAddress;
    }
}
