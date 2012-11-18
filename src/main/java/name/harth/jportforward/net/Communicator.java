package name.harth.jportforward.net;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Communicator implements Runnable, Lifecycle, DisposableBean, InitializingBean {

    private SocketChannel remoteChannel;
    private Thread remoteThread;
    private boolean stopRemoteThread;
    private boolean remoteThreadRunning;

    public Communicator()
    {
        remoteThread = new Thread(this);
    }

    @Override
    public void run()
    {
        remoteThreadRunning = true;
        bindSocket();
        while (true)
        {
            if (stopRemoteThread)
            {
                break;
            }
        }
        unbindSocket();
        remoteThreadRunning = false;
    }

    private void bindSocket()
    {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ);


        SocketChannel remoteChannel = localChannel.accept();
        remoteChannel.configureBlocking(false);
        remoteChannel.register(localSelector, SelectionKey.OP_READ);
    }

    private void unbindSocket() {

    }

    @Override
    public void destroy() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void start() {
        stopRemoteThread = false;
        remoteThread.start();
    }

    @Override
    public void stop() {
        stopRemoteThread = true;
    }

    @Override
    public boolean isRunning() {
        return remoteThreadRunning;
    }

    @SuppressWarnings("UnusedDeclaration")
    public SocketChannel getRemoteChannel() {
        return remoteChannel;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRemoteChannel(SocketChannel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }
}
