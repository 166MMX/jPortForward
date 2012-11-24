package name.harth.jportforward;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import name.harth.jportforward.cli.CommandLineArgs;
import name.harth.jportforward.cli.ConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
//import sun.misc.Signal;
//import sun.misc.SignalHandler;

public class JPortForward
{
    private final Logger logger = LoggerFactory.getLogger(JPortForward.class);
    private static JPortForward instance;

    public static void main(String arguments[])
    {
        CommandLineArgs cla = new CommandLineArgs();
        JCommander commander = new JCommander(cla);
        commander.addConverterFactory(new ConverterFactory());
        commander.setProgramName("JPortForward");
        try
        {
            commander.parse(arguments);
        }
        catch (ParameterException ex)
        {
            commander.usage();
            System.exit(-1);
        }

        instance = new JPortForward();
    }

    private JPortForward()
    {
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.registerShutdownHook();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));
        ctx.refresh();
        ctx.start();
    }

}
