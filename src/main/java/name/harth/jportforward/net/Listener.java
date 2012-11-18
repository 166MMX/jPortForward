package name.harth.jportforward.net;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import java.util.List;

public class Listener implements Runnable, Lifecycle, DisposableBean, InitializingBean {
    private String address;
    private String protocol;
    private int port;
    private int maxConnections;
    private List<AccessFilter> accessFilter;
    private List<Target> targets;
    private boolean running;

    public Listener() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void start() {

    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void run() {

    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setAccessFilter(List<AccessFilter> accessFilter) {
        this.accessFilter = accessFilter;
    }

    public List<AccessFilter> getAccessFilter() {
        return accessFilter;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public List<Target> getTargets() {
        return targets;
    }

}
