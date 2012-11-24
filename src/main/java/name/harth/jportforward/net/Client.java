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
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.Set;

public class Client implements Runnable, Lifecycle, DisposableBean, InitializingBean
{
    private final Logger logger = LoggerFactory.getLogger(Client.class);
    private final Thread thread = new Thread(this, "Client");

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

    private void allocateBuffers(int bufferSize)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("allocateBuffers");
        }
        if (null == targetToRemoteBuffer)
        {
            targetToRemoteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
            remoteToTargetBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        }
    }

    @Override
    public void run()
    {
        allocateBuffers(1 * 1024 * 1024); // 1 Megabyte
        bindSocket();
        if (logger.isInfoEnabled())
        {
            logger.info("Client started");
        }
        int timeout = 250;
        int updatedKeys;
        while (true)
        {
            try
            {
                updatedKeys = selector.select();
            }
            catch (IOException ex)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("", ex);
                }
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
        if (logger.isInfoEnabled())
        {
            logger.info("Client stopped");
        }
    }

    private void finishConnect(SelectionKey key)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("finishConnect");
        }
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

        key.interestOps((key.interestOps() ^ SelectionKey.OP_CONNECT) | SelectionKey.OP_READ);
    }

    private void write(SelectionKey key)
    {
        SelectionKey oppositeKey;
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == remoteChannel)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("write from targetToRemoteBuffer");
            }
            writeFromBuffer(channel, key, targetToRemoteBuffer);
            oppositeKey = remoteSelectionKey;
        }
        else if (channel == targetChannel)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("write from remoteToTargetBuffer");
            }
            writeFromBuffer(channel, key, remoteToTargetBuffer);
            oppositeKey = targetSelectionKey;
        }
        else
        {
            if (logger.isErrorEnabled())
            {
                logger.error("This should not have happened");
            }
            return;
        }
        oppositeKey.interestOps(oppositeKey.interestOps() ^ SelectionKey.OP_WRITE);
        selector.wakeup();
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
        SelectionKey oppositeKey;
        if (channel == remoteChannel)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("read from remoteToTargetBuffer");
            }
            readIntoBuffer(channel, key, remoteToTargetBuffer);
            oppositeKey = targetSelectionKey;
        }
        else if (channel == targetChannel)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("read from targetToRemoteBuffer");
            }
            readIntoBuffer(channel, key, targetToRemoteBuffer);
            oppositeKey = remoteSelectionKey;
        }
        else
        {
            if (logger.isErrorEnabled())
            {
                logger.error("This should not have happened");
            }
            return;
        }
        oppositeKey.interestOps(oppositeKey.interestOps() | SelectionKey.OP_WRITE);
        selector.wakeup();
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

    private void debugByteBuffer (ByteBuffer buffer)
    {
        ByteBuffer dupe = buffer.duplicate();
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        dupe.flip();
        CharBuffer charBuffer = null;
        try
        {
            charBuffer = decoder.decode(dupe);
        }
        catch (CharacterCodingException ex)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("", ex);
            }
        }
        dupe.clear();
        String s = null;
        if (charBuffer != null)
        {
            s = charBuffer.toString();
        }
        if (logger.isDebugEnabled())
        {
            logger.debug(s);
        }
    }

    private void bindSocket()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("bindSocket");
        }
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

            remoteSelectionKey = remoteChannel.register(selector, SelectionKey.OP_READ);
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

            targetChannel.connect(targetInetSocketAddress);

            targetSocket = targetChannel.socket();

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
        if (logger.isDebugEnabled())
        {
            logger.debug("unbindSocket");
        }
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
            logger.info("Starting client");
        }
        stopThread = false;
        thread.start();
    }

    @Override
    public void stop()
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Stopping client");
        }
        stopThread = true;
    }

    @Override
    public boolean isRunning()
    {
        return thread.isAlive();
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
