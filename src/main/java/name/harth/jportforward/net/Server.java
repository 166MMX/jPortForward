package name.harth.jportforward.net;

import java.util.List;

public class Server
{
    private int                maxConnections;
    private List<Listener>     listeners;
    private List<AccessFilter> accessFilter;

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
