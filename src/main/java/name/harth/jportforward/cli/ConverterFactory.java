package name.harth.jportforward.cli;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class ConverterFactory implements IStringConverterFactory
{

    private static HashMap<Class<?>, Class<? extends IStringConverter<?>>> classConverters;

    static
    {
        classConverters = new HashMap<>();
        classConverters.put(InetSocketAddress.class, InetSocketAddressConverter.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends IStringConverter<?>> getConverter(Class forType)
    {
        return classConverters.get(forType);
    }

}