package name.harth.jportforward.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Client implements Runnable, Lifecycle, DisposableBean, InitializingBean
{
    private final Logger logger = LoggerFactory.getLogger(Client.class);

    private Target target;

    private Selector selector;

    private SocketChannel     remoteChannel;
    private SelectionKey      remoteSelectionKey;
    private Socket            remoteSocket;
    private ByteBuffer        remoteToTargetBuffer;

    private SocketChannel     targetChannel;
    private SelectionKey      targetSelectionKey;
    private Socket            targetSocket;
    private ByteBuffer        targetToRemoteBuffer;

    private Thread  thread;
    private boolean stopThread;
    private boolean running;

    public Client()
    {
        thread = new Thread(this);
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

        int bufferSize = 4 * 1024 * 1024; // 4 Megabyte
        targetToRemoteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        remoteToTargetBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
    }

    private void write(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == remoteChannel)
        {
            try
            {
                channel.write(targetToRemoteBuffer);
            }
            catch (IOException ex)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex);
                }
            }
            if (targetToRemoteBuffer.remaining() > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Remote channel buffer full");
                }
            }
        }
        else if (channel == targetChannel)
        {
            try
            {
                channel.write(remoteToTargetBuffer);
            }
            catch (IOException ex)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex);
                }
            }
            if (remoteToTargetBuffer.remaining() > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Target channel buffer full");
                }
            }
        }
        else
        {
            if (logger.isErrorEnabled())
            {
                logger.error("This should not have happened");
            }
        }
    }

    private void read(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel) key.channel();
        int amount;
        if (channel == remoteChannel)
        {
            try
            {
                amount = channel.read(remoteToTargetBuffer);
            }
            catch (IOException ex)
            {
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
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex);
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
                return;
            }
        }
        else if (channel == targetChannel)
        {
            try
            {
                amount = channel.read(targetToRemoteBuffer);
            }
            catch (IOException ex)
            {
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
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex);
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
                return;
            }
        }
        if (logger.isErrorEnabled())
        {
            logger.error("This should not have happened");
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

            remoteSocket = remoteChannel.socket();

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