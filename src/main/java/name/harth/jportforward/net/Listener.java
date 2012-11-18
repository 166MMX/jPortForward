package name.harth.jportforward.net;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketPermission;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Listener implements Runnable, Lifecycle, DisposableBean, InitializingBean {
    private String address;
    private String protocol;
    private int port;
    private int maxConnections;
    private List<AccessFilter> accessFilter;
    private List<Target> targets;
    private boolean localThreadRunning;
    private InetSocketAddress inetSocketAddress;
    private ServerSocketChannel localChannel;
    private ServerSocket localSocket;
    private Selector localSelector;
    private SelectionKey localSelectionKey;
    private Thread localThread;
    private boolean stopLocalThread;

    public Listener()
    {
        SecurityManager;
        localThread = new Thread(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        inetSocketAddress = new InetSocketAddress(address, port);
    }

    @Override
    public void start()
    {
        stopLocalThread = false;
        localThread.start();
    }

    @Override
    public boolean isRunning()
    {
        return localThreadRunning;
    }

    @Override
    public void stop()
    {
        stopLocalThread = true;
    }

    private void bindSocket()
    {
        try
        {
            localChannel = ServerSocketChannel.open();
            localChannel.configureBlocking(false);

            localSocket = localChannel.socket();
            localSocket.bind(inetSocketAddress);

            localSelector = Selector.open();
            localSelectionKey = localChannel.register(localSelector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException ex)
        {

        }
    }

    private void unbindSocket()
    {
        try
        {
            localSelectionKey.cancel();
            localSelector.close();
            localSocket.close();
            localChannel.close();
        }
        catch (IOException ex)
        {

        }
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void run()
    {
        localThreadRunning = true;
        bindSocket();
        while (true)
        {
            try
            {
                localSelector.select();
            }
            catch (IOException e)
            {

                break;
            }
            Set<SelectionKey> readyKeys = localSelector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
            if (stopLocalThread)
            {
                break;
            }
        }
        unbindSocket();
        localThreadRunning = false;
    }

    private void accept(SelectionKey key) {
        try {
            ServerSocketChannel remoteChannel = (ServerSocketChannel) key.channel();
            SocketChannel remoteChannel = localChannel.accept();
            ssss.configureBlocking(false);
            ssss.register(localSelector, SelectionKey.OP_READ);

            Communicator communicator = new Communicator();
            communicator.setRemoteChannel(ssss);
            communicator.start();
        } catch (IOException ex) {

        }
    }

    private void read(SelectionKey key) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void write(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer output = (ByteBuffer) key.attachment();
        if (!output.hasRemaining()) {
            output.rewind();
        }
        try {
            client.write(output);
        } catch (IOException ex) {

        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setAddress(String address) {
        this.address = address;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getAddress() {
        return address;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getProtocol() {
        return protocol;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setAccessFilter(List<AccessFilter> accessFilter) {
        this.accessFilter = accessFilter;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<AccessFilter> getAccessFilter() {
        return accessFilter;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getPort() {
        return port;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPort(int port) {
        this.port = port;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getMaxConnections() {
        return maxConnections;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<Target> getTargets() {
        return targets;
    }

}
