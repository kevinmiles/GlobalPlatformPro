package pro.javacard.gp;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Socket version of GPTool
 *
 * Created by dusanklinec on 19.07.16.
 */
public class GPToolSocket {
    public final static String DEFAULT_IP = "127.0.0.1";
    public final static int DEFAULT_PORT = 9988;
    public final static int DEFAULT_WORKERS = 1;
    public final static int DEFAULT_BACKLOG = 1024;

    public final static String OPT_IP = "ip";
    public final static String OPT_PORT = "port";
    public final static String OPT_WORKERS = "workers";

    private final static String OPT_DEBUG = "debug";
    private final static String OPT_VERBOSE = "verbose";
    private final static String OPT_VERSION = "version";

    /**
     * Server is running flag.
     */
    protected volatile boolean running = true;

    /**
     * Parsed arguments
     */
    protected OptionSet args;

    /**
     * Worker threads
     */
    protected ThreadPoolExecutor executor;

    /**
     * Concurrent queue of jobs. Will be processed by workers.
     */
    protected final ConcurrentLinkedQueue<Connection> jobs = new ConcurrentLinkedQueue<>();

    public static OptionSet parseArguments(String[] argv) throws IOException {
        OptionSet args = null;

        // Setup generic parser.
        final OptionParser parser = new OptionParser();
        parser.acceptsAll(Arrays.asList("V", OPT_VERSION), "Show information about the program");
        parser.acceptsAll(Arrays.asList("h", "help"), "Shows this help string").forHelp();
        parser.acceptsAll(Arrays.asList("d", OPT_DEBUG), "Show PC/SC and APDU trace");
        parser.acceptsAll(Arrays.asList("v", OPT_VERBOSE), "Be verbose about operations");

        // Add socket options
        parser.accepts(OPT_IP, "IP address to bind to").withOptionalArg().defaultsTo(DEFAULT_IP);
        parser.accepts(OPT_PORT, "TCP port to bind to").withOptionalArg().ofType(Integer.class).defaultsTo(DEFAULT_PORT);
        parser.accepts(OPT_WORKERS, "Number of worker threads to use").withOptionalArg().ofType(Integer.class).defaultsTo(DEFAULT_WORKERS);

        // Parse arguments
        try {
            args = parser.parse(argv);
            // Try to fetch all values so that format is checked before usage
            // for (String s: parser.recognizedOptions().keySet()) {args.valuesOf(s);} // FIXME: screws up logging hack
        } catch (OptionException e) {
            if (e.getCause() != null) {
                System.err.println(e.getMessage() + ": " + e.getCause().getMessage());
            } else {
                System.err.println(e.getMessage());
            }
            System.err.println();
            parser.printHelpOn(System.err);
            System.exit(1);
        }

        // Do the work, based on arguments
        if (args.has("help")) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }

        return args;
    }

    public int work(String[] argv) throws IOException, InterruptedException {
        args = parseArguments(argv);

        // Verbose SLF4J logging
        if (args.has(OPT_VERBOSE)) {
            // Set up slf4j simple in a way that pleases us
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
            System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
            System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
            System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
        } else {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        }

        // Version info
        if (args.has(OPT_VERSION) || args.has(OPT_VERBOSE) || args.has(OPT_DEBUG)) {
            String version = GlobalPlatform.getVersion();
            // Append host information
            version += "\nRunning on " + System.getProperty("os.name");
            version += " " + System.getProperty("os.version");
            version += " " + System.getProperty("os.arch");
            version += ", Java " + System.getProperty("java.version");
            version += " by " + System.getProperty("java.vendor");
            System.out.println("GlobalPlatformPro " + version);
        }

        // Workers spawn
        final int workers = (int) args.valueOf(OPT_WORKERS);
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(workers);

        // Start the server.
        try{
            final String ip = (String) args.valueOf(OPT_IP);
            final int port = (int) args.valueOf(OPT_PORT);

            final ServerSocket listenSocket = (ip.equals(DEFAULT_IP)) ?
                    new ServerSocket(port) :
                    new ServerSocket(port, DEFAULT_BACKLOG, InetAddress.getByName(ip));
            listenSocket.setSoTimeout(3000);

            System.out.println(String.format("Server is listening on %s:%d", ip, port));

            // Listening loop.
            while(running) {
                try {
                    final Socket clientSocket = listenSocket.accept();
                    if (args.has(OPT_DEBUG)) {
                        System.out.println(String.format("User connected: %s", clientSocket.getRemoteSocketAddress()));
                    }

                    final Connection c = new Connection(this, clientSocket);

                } catch(SocketTimeoutException timeout){
                    // Timeout is OK.
                }
            }
        }
        catch(IOException e) {
            System.out.println("Listen :" + e.getMessage());
        }

        // Shutdown executor service, wait for task completion.
        executor.shutdown();
        while (!executor.isTerminated()){
            Thread.sleep(500);
        }

        return 0;
    }

    public static void main(String[] argv) throws Exception {
        final GPToolSocket tool = new GPToolSocket();
        final int status = tool.work(argv);
        System.exit(status);
    }

    public OptionSet getArgs() {
        return args;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    protected static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Class representing one connected user.
     */
    static class Connection extends Thread {
        protected InputStream input;
        protected OutputStream output;
        protected Socket clientSocket;
        protected OptionSet args;
        protected GPToolSocket parent;

        protected PrintStream printOut;
        protected String inputData;

        public Connection (GPToolSocket parent, Socket aClientSocket) {
            try {
                clientSocket = aClientSocket;
                input = clientSocket.getInputStream();
                output = clientSocket.getOutputStream();
                this.args = parent.getArgs();
                this.parent = parent;
                this.start();
            }
            catch(IOException e) {
                System.out.println("Connection: "+e.getMessage());
            }
        }

        public void run() {
            printOut = new PrintStream(output);
            Future<?> future = null;

            try {
                inputData = convertStreamToString(input);

                // Enqueue current job to the queue.
                future = parent.getExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        final GPTool tool = new GPTool(printOut, printOut);
                        try {
                            if (args.has(OPT_DEBUG)){
                                printOut.println(String.format("Input: <<<%s>>>", inputData));
                            }

                            // If tokenizer is not powerful enough, consider using this one: org.apache.tools.ant.types.Commandline
                            final List<String> inputArgs = GPArgumentTokenizer.tokenize(inputData);
                            tool.work(inputArgs.toArray(new String[inputArgs.size()]));

                        } catch (IOException e) {
                            printOut.println("IO: " + e.getMessage());

                        } catch (NoSuchAlgorithmException e) {
                            printOut.println("Exception: " + e.getMessage());
                        }
                    }
                });

                // Wait to finish the computation
                final Object o = future.get();

            } catch (Exception e) {
                printOut.println("Exception: " + e.getMessage());

            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    /*close failed*/
                }

                if (future != null && !future.isDone() && !future.isCancelled()){
                    future.cancel(true);
                }
            }
        }

        public PrintStream getPrintOut() {
            return printOut;
        }

        public String getInputData() {
            return inputData;
        }
    }
}
