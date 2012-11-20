package name.harth.jportforward.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Listener implements Runnable, Lifecycle, InitializingBean, DisposableBean
{
    private final Logger logger = LoggerFactory.getLogger(Listener.class);

    private String             address;
    private String             protocol;
    private int                port;
    private int                maxConnections;
    private List<AccessFilter> accessFilter;
    private Target             target;

    private InetSocketAddress   inetSocketAddress;
    private ServerSocketChannel channel;
    private ServerSocket        socket;
    private Selector            selector;
    private SelectionKey        selectionKey;

    private List<Client> clients;

    private Thread  thread;
    private boolean stopThread;
    private boolean running;

    public Listener()
    {
        thread = new Thread(this);
        clients = new ArrayList<Client>();
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        inetSocketAddress = new InetSocketAddress(address, port);
    }

    @Override
    public void start()
    {
        stopThread = false;
        thread.start();
    }

    @Override
    public boolean isRunning()
    {
        return running;
    }

    @Override
    public void stop()
    {
        stopThread = true;
    }

    private void bindSocket()
    {
        try
        {
            selector = Selector.open();
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
        }
        try
        {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);

            socket = channel.socket();
            socket.bind(inetSocketAddress);

            selectionKey = channel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
        }
    }

    private void unbindSocket()
    {
        try
        {
            selectionKey.cancel();
            socket.close();
            channel.close();

            selector.close();
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
        }
    }

    @Override
    public void destroy() throws Exception
    {

    }

    @Override
    public void run()
    {
        running = true;
        bindSocket();
        while (true)
        {
            try
            {
                long timeout = 1000;
                selector.select(timeout);
            }
            catch (IOException ex)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex);
                }
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext())
            {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid())
                {
                    continue;
                }

                if (key.isAcceptable())
                {
                    accept(key);
                }
            }
            if (stopThread)
            {
                break;
            }
        }
        unbindSocket();
        running = false;
    }

    private void accept(SelectionKey key)
    {
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
    public void setAccessFilter(List<AccessFilter> accessFilter)
    {
        this.accessFilter = accessFilter;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<AccessFilter> getAccessFilter()
    {
        return accessFilter;
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
