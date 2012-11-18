package name.harth.jportforward.net;

import java.util.List;

public class Server {
    private int maxConnections;
    private List<Listener> listeners;
    private List<AccessFilter> accessFilter;

    public void setListeners(List<Listener> listeners) {
        this.listeners = listeners;
    }

    public List<Listener> getListeners() {
        return listeners;
    }

    public void setAccessFilter(List<AccessFilter> accessFilter) {
        this.accessFilter = accessFilter;
    }

    public List<AccessFilter> getAccessFilter() {
        return accessFilter;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
}
