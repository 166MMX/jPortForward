package name.harth.jportforward.cli;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ConverterFactory implements IStringConverterFactory
{

    private static final Map<Class<?>, Class<? extends IStringConverter<?>>> CLASS_CONVERTERS;

    static
    {
        CLASS_CONVERTERS = new HashMap<Class<?>, Class<? extends IStringConverter<?>>>();
        CLASS_CONVERTERS.put(InetSocketAddress.class, InetSocketAddressConverter.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends IStringConverter<?>> getConverter(Class forType)
    {
        return CLASS_CONVERTERS.get(forType);
    }

}