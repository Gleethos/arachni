
import comp.imp.PluginManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server
{
    private static final int PORT = 8080;// port to listen connection

    /** Settings - Index Meaning:  **/
    private static final int IS_ALIVE = 0;

    /**  Plugins:  **/
    private PluginManager _manager;

    /**==============================================================================================================**/
    Server(){
        _manager = new PluginManager();
        _manager.add("FileReader");
    }

    public void start()
    {
        /** Server Thread:  **/
        Thread serverRunner = null;

        /** Shared State:  (Server Thread <-> User Thread communication!) **/
        boolean[] settings = {
            false, //alive / dead
        };

        /** User Interface: **/
        IOFrame user = new IOFrame("Webio - Server - commandline", 1000, true);
        while(true){
            String command = user.read();
            user.println(command);
            switch(command)
            {
                case "run":
                    user.println("[INFO]: starting server...");
                    if(!settings[IS_ALIVE]) {
                        serverRunner = new Thread(() -> {
                            _run(settings);
                        });
                        serverRunner.start();
                        settings[IS_ALIVE] = true;
                    } else {
                        user.println("[Warning]: Server already running!");
                    }
                    break;

                case "stop":
                    user.println("[INFO]: stopping server...");
                    if(serverRunner!=null){
                        settings[IS_ALIVE] = false;
                        serverRunner = null;
                    } else {
                        user.println("[Warning]: Server not running!");
                    }
                    break;

            }
            System.out.println("HERE!");
        }
    }

    private void _run(boolean[]  settings){
        IOFrame log = new IOFrame("Webio - Server", 1000, false);
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            log.println("[SERVER]: started! ");
            log.println("[SERVER]: Listening for connections on port : " + PORT + " ...\n");
            // we listen until user halts server execution
            while (settings[0]) {
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
