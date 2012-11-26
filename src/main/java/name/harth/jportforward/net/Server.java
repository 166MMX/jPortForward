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
    private final Thread thread = new Thread(this, "Server");

    private int                maxConnections;
    private List<Listener>     listeners;
    private List<AccessFilter> accessFilter;

    private Selector selector;

    private boolean stopThread;

    public Server()
    {
    }

    @Override
    public void destroy() throws Exception
    {
        stop();
        while (thread.isAlive())
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
        if (logger.isInfoEnabled())
        {
            logger.info("Starting server");
        }
        stopThread = false;
        thread.start();
    }

    @Override
    public boolean isRunning()
    {
        return thread.isAlive();
    }

    @Override
    public void stop()
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Stopping server");
        }
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
        bindSockets();
        if (logger.isInfoEnabled())
        {
            logger.info("Server started");
        }
        int timeout = 1000;
        int updatedKeys;
        while (true)
        {
            try
            {
                updatedKeys = selector.select(timeout);
            }
            catch (IOException ex)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex);
                }
                break;
            }
            if (stopThread)
            {
                break;
            }
            if (0 == updatedKeys)
            {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys)
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
        }
        unbindSockets();
        if (logger.isInfoEnabled())
        {
            logger.info("Server stopped");
        }
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
