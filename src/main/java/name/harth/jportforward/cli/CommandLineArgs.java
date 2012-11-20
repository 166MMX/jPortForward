package name.harth.jportforward.cli;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.net.InetSocketAddress;

public class CommandLineArgs {

    //final static Logger logger = LoggerFactory.getLogger(CommandLineArgs.class);

    private static final String USER_NAME = System.getProperty("user.name");
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String APP_HOME = USER_HOME + "/.jPortForward";
    private static final String USER_DIR = System.getProperty("user.dir");

//    @Parameter(names = { "-p", "--protocol" }, description = "protocol to be used")
//    public String protocol;

    @Parameter(names = { "-r", "--remote" }, description = "remote ip and port to accept connections from")
    public InetSocketAddress remote;

//    = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 56882)
    @Parameter(names = { "-l", "--local" }, description = "local ip and port to bind to")
    public InetSocketAddress local;

    @Parameter(names = { "-d", "--destination" }, description = "destination ip and port to forward to")
    public InetSocketAddress destination;

    @Parameter(names = { "--daemon" }, description = "start as daemon")
    public Boolean daemon = false;

    @Parameter(names = { "-u", "--user" }, description = "user to be used for daemon")
    public String user = USER_NAME;

    @Parameter(names = { "-g", "--group" }, description = "group to be used for daemon")
    public String group;

    @Parameter(names = { "--argumentsFile" }, description = "command line arguments properties file")
    public File argumentsFile = new File(APP_HOME + "/arguments.properties");

    @Parameter(names = { "--logConfigFile" }, description = "Groovy Logback configuration file")
    public File logConfigFile = new File(APP_HOME + "/logback.groovy");

    @Parameter(names = { "--appConfigFile" }, description = "XML application context configuration file")
    public File appConfigFile = new File(APP_HOME + "/applicationContext.xml");

    @Parameter(names = { "--pidFile" }, description = "Location of file containing the pid")
    public File pidFile;

//    private static InetAddress getLocalInetAddress() {
//        try
//        {
//            ArrayList<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
//            for (NetworkInterface networkInterface : networkInterfaces)
//            {
//                if (!networkInterface.isLoopback() && !networkInterface.isVirtual() && networkInterface.isUp())
//                {
//                    networkInterface.getInetAddresses();
//                }
//            }
//        }
//        catch (SocketException ex)
//        {
//            if (logger.isErrorEnabled())
//            {
//                logger.error("Unable to retrieve Inet Address", ex);
//            }
//        }
//    }

}
