package name.harth.jportforward.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.List;
import java.util.Set;

public class Server implements Runnable, Lifecycle, InitializingBean, DisposableBean
{
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private final Thread thread = new Thread(this);

    private int                maxConnections;
    private List<Listener>     listeners;
    private List<AccessFilter> accessFilter;

    private Selector selector;

    private boolean stopThread;
    private boolean running;

    public Server()
    {
    }

    @Override
    public void destroy() throws Exception
    {
        stop();
        while (running)
        {
            Thread.sleep(100);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
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

    private void bindSockets()
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
        for (Listener listener : listeners)
        {
            listener.bindSocket(selector);
        }
    }

    private void unbindSockets()
    {
        for (Listener listener : listeners)
        {
            listener.unbindSocket();
        }
        try
        {
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
    public void run()
    {
        running = true;
        bindSockets();
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
            for (SelectionKey key : readyKeys)
            {
                if (!key.isValid())
                {
                    continue;
                }
                if (key.isAcceptable())
                {
                    Listener listener = (Listener) key.attachment();
                    listener.accept(key);
                }
            }
            if (stopThread)
            {
                break;
            }
        }
        unbindSockets();
        running = false;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setListeners(List<Listener> listeners)
    {
        this.listeners = listeners;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<Listener> getListeners()
    {
        return listeners;
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
    public int getMaxConnections()
    {
        return maxConnections;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setMaxConnections(int maxConnections)
    {
        this.maxConnections = maxConnections;
    }

}
