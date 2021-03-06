package name.harth.jportforward.cli;

import com.beust.jcommander.IStringConverter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

class InetSocketAddressConverter implements IStringConverter<InetSocketAddress>
{

    @Override
    public InetSocketAddress convert(String address)
    {
        int i = address.indexOf(':');

        if (i == -1)
        {
            throw new IllegalArgumentException("No port number in address " + address);
        }

        String portStr = address.substring(i + 1);
        int port;

        try
        {
            port = Integer.parseInt(portStr);
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException("Port not a number " + portStr, ex);
        }

        if (port < 1 || port > 65535)
        {
            throw new IllegalArgumentException("Port number out of range " + port);
        }

        String host = address.substring(0, i);
        InetAddress inetAddress;

        try
        {
            inetAddress = InetAddress.getByName(host);
        }
        catch (UnknownHostException ex)
        {
            throw new IllegalArgumentException("Unknown host " + host, ex);
        }

        return new InetSocketAddress(inetAddress, port);
    }

}
