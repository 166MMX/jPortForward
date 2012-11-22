package name.harth.jportforward.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Listener implements InitializingBean, DisposableBean
{
    private final Logger logger = LoggerFactory.getLogger(Listener.class);

    private String             address;
    private String             protocol;
    private int                port;
    private int                maxConnections;
    private List<AccessFilter> accessFilters;
    private Target             target;

    private InetSocketAddress   inetSocketAddress;
    private ServerSocketChannel channel;
    private ServerSocket        socket;
    private SelectionKey        selectionKey;

    private List<Client> clients;

    public Listener()
    {
        clients = new ArrayList<Client>();
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        inetSocketAddress = new InetSocketAddress(address, port);
    }

    @Override
    public void destroy() throws Exception
    {

    }

    public void bindSocket(Selector selector)
    {
        try
        {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);

            socket = channel.socket();
            socket.bind(inetSocketAddress);

            selectionKey = channel.register(selector, SelectionKey.OP_ACCEPT, this);
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
        }
    }

    public void unbindSocket()
    {
        try
        {
            selectionKey.cancel();
            socket.close();
            channel.close();
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
        }
    }

    public void accept(SelectionKey key)
    {
        if (clients.size() >= maxConnections)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Reached maximum connections for this listener");
            }
            return;
        }

        ServerSocketChannel listenerChannel = (ServerSocketChannel) key.channel();
        SocketChannel remoteChannel;

        try
        {
            remoteChannel = listenerChannel.accept();
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
            key.cancel();
            return;
        }

        Client client = new Client();
        client.setTarget(target);
        client.setRemoteChannel(remoteChannel);
        if (!client.matchesAccessFilters(accessFilters))
        {
            return;
        }
        client.start();

        clients.add(client);
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
    public void setAccessFilters(List<AccessFilter> accessFilters)
    {
        this.accessFilters = accessFilters;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<AccessFilter> getAccessFilters()
    {
        return accessFilters;
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
    public int getMaxConnections()
    {
        return maxConnections;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setMaxConnections(int maxConnections)
    {
        this.maxConnections = maxConnections;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setTarget(Target target)
    {
        this.target = target;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Target getTarget()
    {
        return target;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<Client> getClients()
    {
        return clients;
    }

    @SuppressWarnings("UnusedDeclaration")
    private void setClients(List<Client> clients)
    {
        this.clients = clients;
    }

}
