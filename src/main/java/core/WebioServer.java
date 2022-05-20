package core;

import comp.IPlugin;
import comp.imp.PluginManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.function.Supplier;

public class WebioServer implements Runnable
{
    private final ThreadPoolExecutor _pool =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
            );

    /**  Main port used to connect to WEBIO! */
    public static final int DEFAULT_PORT = 8080;// port to listen connection

    /** Settings - Index Meaning:  **/
    private static final int IS_ALIVE = 0;

    /**  Plugins are loaded and stored here:  **/
    private final PluginManager _manager;
    private final int _port;

    /** Shared State:  (core.WebioServer Thread <-> User Thread communication!) **/
    private final boolean[] settings = {
            false, //alive / dead
    };

    private final Runnable _runnable;
    private final Function<String,Logger> _workerOutput;


    public WebioServer(Commander user){
        this(
                DEFAULT_PORT,
                user,
                false,
                name -> new IOFrame("Webio - "+name, 1000, false)
        );
    }

    public WebioServer(int port, Commander user, boolean autoRun, Function<String,Logger> workerOutput){
        _port = port;
        _manager = new PluginManager();
        _manager.add("FileReader");
        _manager.add("TestPlugin");
        _runnable = () -> _run(user, autoRun);
        _workerOutput = workerOutput;
    }

    @Override
    public void run() {
        _runnable.run();
    }

    private void _run(
            Commander user,
            boolean autoRun
    ) {
        /** core.WebioServer Thread:  **/
        Thread serverRunner = null;

        /** User Interface: **/
        boolean alive = true;
        while(alive)
        {
            String command;
            if ( autoRun ) {
                command = "run";
                autoRun = false;
            } else {
                command = user.read();
            }
            if ( !command.isBlank() ) user.println(command);
            switch (command) {
                case "run" -> {
                    user.println("[INFO]: starting server...");
                    if (!settings[IS_ALIVE]) {
                        serverRunner = new Thread(() -> {
                            _run(settings);// <= The Server Thread listening for clients!
                        });
                        serverRunner.start();
                        settings[IS_ALIVE] = true;
                    } else {
                        user.println("[Warning]: core.WebioServer already running!");
                    }
                }
                case "stop" -> {
                    user.println("[INFO]: stopping server...");
                    if (serverRunner != null) {
                        settings[IS_ALIVE] = false;
                        serverRunner = null;
                    } else {
                        user.println("[Warning]: core.WebioServer not running!");
                    }
                }
                case "plugins" -> {
                    user.println("[INFO](plugins): ");
                    for (IPlugin plugin : _manager.getPlugins()) {
                        user.println(plugin.toString());
                    }
                    user.println("");
                }
                case "quit" -> alive = false;
                case "help" -> {
                    user.println("[INFO](help): Commands:");
                    user.println("'start' => Starts your server. It can now handle requests from clients!");
                    user.println("'stop' => Stops your server. It will not handle requests from clients!");
                    user.println("'plugins' => List of plugins installed!");
                    user.println("'quit' => Shutdown Webio.\n");
                }
            }
        }
    }

    public boolean isRunning() {
        return settings[IS_ALIVE];
    }

    public void shutdown() {
        this._pool.shutdown();
        this._manager.clear();
    }

    /**
     * This method is run by the server thread.
     * It listens for clients and creates ClientHandler for them.
     * @param settings
     */
    private void _run(boolean[]  settings)
    {
        Logger log = _workerOutput.apply("CLIENT HANDLING");
        try {
            ServerSocket serverConnect = new ServerSocket(_port);
            log.println("started! ");
            log.println("Listening for connections on port : " + _port + " ...\n");
            // we listen until user halts server execution
            while (settings[IS_ALIVE]) {
                Socket client = serverConnect.accept();
                log.println("Connection opened with: "+client.toString()+" (" + new Date() + ")");
                ClientHandler handler = new ClientHandler(client, _manager, log);
                // Submit as task for the thread pool!
                _pool.submit(handler);
            }
            log.println("Stopped listening for connections on port : " + _port + " !\n");
        } catch (IOException e) {
            System.err.println("Connection error : " + e.getMessage());
        }
    }

}
