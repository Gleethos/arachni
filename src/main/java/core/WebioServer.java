package core;

import comp.IPlugin;
import comp.imp.PluginManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class WebioServer
{
    /**  Main port used to connect to WEBIO! */
    private static final int PORT = 8080;// port to listen connection

    /** Settings - Index Meaning:  **/
    private static final int IS_ALIVE = 0;

    /**  Plugins are loaded and stored here:  **/
    private PluginManager _manager;

    /**==============================================================================================================**/
    public WebioServer(){
        _manager = new PluginManager();
        _manager.add("FileReader");
        _manager.add("TestPlugin");
    }


    public void start()
    {
        /** core.WebioServer Thread:  **/
        Thread serverRunner = null;

        /** Shared State:  (core.WebioServer Thread <-> User Thread communication!) **/
        boolean[] settings = {
            false, //alive / dead
        };

        /** User Interface: **/
        IOFrame user = new IOFrame("Webio - core.WebioServer - commandline", 1000, true);
        boolean alive = true;
        while(alive)
        {
            String command = user.read();
            user.println(command);
            switch(command)
            {
                case "run":
                    user.println("[INFO]: starting server...");
                    if(!settings[IS_ALIVE]) {
                        serverRunner = new Thread(() -> {
                            _run(settings);// <= The Server Thread listening for clients!
                        });
                        serverRunner.start();
                        settings[IS_ALIVE] = true;
                    } else {
                        user.println("[Warning]: core.WebioServer already running!");
                    }
                    break;

                case "stop":
                    user.println("[INFO]: stopping server...");
                    if(serverRunner!=null){
                        settings[IS_ALIVE] = false;
                        serverRunner = null;
                    } else {
                        user.println("[Warning]: core.WebioServer not running!");
                    }
                    break;
                case "plugins":
                    user.println("[INFO](plugins): ");
                    for (IPlugin plugin : _manager.getPlugins()) {
                        user.println(plugin.toString());
                    }
                    user.println("");
                    break;
                case "quit":
                    alive = false;
                    break;
                case "help":
                    user.println("[INFO](help): Commands:");
                    user.println("'start' => Starts your server. It can now handle requests from clients!");
                    user.println("'stop' => Stops your server. It not handle requests from clients!");
                    user.println("'plugins' => List of plugins installed!");
                    user.println("'quit' => Shutdown Webio.\n");
                    break;
            }
        }
    }

    /**
     * This method is run by the server thread.
     * It listens for clients and creates ClientHandler for them.
     * @param settings
     */
    private void _run(boolean[]  settings)
    {
        IOFrame log = new IOFrame("Webio - core.WebioServer", 1000, false);
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            log.println("[SERVER]: started! ");
            log.println("[SERVER]: Listening for connections on port : " + PORT + " ...\n");
            // we listen until user halts server execution
            while (settings[IS_ALIVE]) {
                Socket client = serverConnect.accept();
                log.println("[SERVER]: Connection opened with: "+client.toString()+" (" + new Date() + ")");
                ClientHandler handler = new ClientHandler(client, _manager, log);
                // create dedicated thread to manage the client connection
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER]: Connection error : " + e.getMessage());
        }
    }


}
