package name.harth.jportforward.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Client implements Runnable, Lifecycle, DisposableBean, InitializingBean
{
    private final Logger logger = LoggerFactory.getLogger(Client.class);
    private final Thread thread = new Thread(this);

    private Target target;

    private Selector selector;

    private SocketChannel remoteChannel;
    private SelectionKey  remoteSelectionKey;
    private Socket        remoteSocket;
    private ByteBuffer    remoteToTargetBuffer;

    private SocketChannel targetChannel;
    private SelectionKey  targetSelectionKey;
    private Socket        targetSocket;
    private ByteBuffer    targetToRemoteBuffer;

    private boolean stopThread;
    private boolean running;

    public Client()
    {
    }

    public boolean matchesAccessFilters(List<AccessFilter> accessFilters)
    {
        boolean matches = false;
        SocketAddress remoteAddress = remoteSocket.getRemoteSocketAddress();
        for (AccessFilter accessFilter : accessFilters)
        {
            if (accessFilter.match(remoteAddress))
            {
                matches = true;
            }
        }
        return matches;
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

                if (key.isConnectable())
                {
                    this.finishConnect(key);
                }
                else if (key.isReadable())
                {
                    this.read(key);
                }
                else if (key.isWritable())
                {
                    this.write(key);
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

    private void finishConnect(SelectionKey key)
    {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try
        {
            socketChannel.finishConnect();
        }
        catch (IOException ex)
        {
            key.cancel();
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
            return;
        }

        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        int bufferSize = 1 * 1024 * 1024; // 1 Megabyte
        targetToRemoteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        remoteToTargetBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
    }

    private void write(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == remoteChannel)
        {
            writeFromBuffer(channel, key, targetToRemoteBuffer);
        }
        else if (channel == targetChannel)
        {
            writeFromBuffer(channel, key, remoteToTargetBuffer);
        }
        if (logger.isErrorEnabled())
        {
            logger.error("This should not have happened");
        }
    }

    private void writeFromBuffer(SocketChannel channel, SelectionKey key, ByteBuffer buffer)
    {
        buffer.flip();
        try
        {
            channel.write(buffer);
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            try
            {
                channel.close();
            }
            catch (IOException ex1)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex1);
                }
            }
            return;
        }
        if (buffer.remaining() > 0)
        {
            buffer.compact();
        }
        else
        {
            buffer.clear();
        }
    }

    private void read(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == remoteChannel)
        {
            readIntoBuffer(channel, key, remoteToTargetBuffer);
        }
        else if (channel == targetChannel)
        {
            readIntoBuffer(channel, key, targetToRemoteBuffer);
        }
        if (logger.isErrorEnabled())
        {
            logger.error("This should not have happened");
        }
    }

    private void readIntoBuffer(SocketChannel channel, SelectionKey key, ByteBuffer buffer)
    {
        int amount;
        try
        {
            amount = channel.read(buffer);
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            try
            {
                channel.close();
            }
            catch (IOException ex1)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex1);
                }
            }
            return;
        }

        if (amount == -1)
        {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.cancel();
            try
            {
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
            remoteChannel.configureBlocking(false);

            remoteSelectionKey = remoteChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
        }

        InetSocketAddress targetInetSocketAddress = target.getInetSocketAddress();

        try
        {
            targetChannel = SocketChannel.open();
            targetChannel.configureBlocking(false);

            targetSocket = targetChannel.socket();
            targetSocket.connect(targetInetSocketAddress);

            targetSelectionKey = targetChannel.register(selector, SelectionKey.OP_CONNECT);
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
            targetSelectionKey.cancel();
            targetSocket.close();
            targetChannel.close();

            remoteSelectionKey.cancel();
            remoteSocket.close();
            remoteChannel.close();

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
    public void stop()
    {
        stopThread = true;
    }

    @Override
    public boolean isRunning()
    {
        return running;
    }

    @SuppressWarnings("UnusedDeclaration")
    public SocketChannel getRemoteChannel()
    {
        return remoteChannel;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRemoteChannel(SocketChannel remoteChannel)
    {
        remoteSocket = remoteChannel.socket();
        this.remoteChannel = remoteChannel;
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
}
