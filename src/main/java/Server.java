import comp.imp.PluginManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server {

    // port to listen connection
    private static final int PORT = 8080;
    private ConsoleFrame _console;
    private PluginManager _manager;

    Server(){
        _console = new ConsoleFrame("Webio - Server", 1000);
        _manager = new PluginManager();
        _manager.add("FileReader");
    }

    public void start(){
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            _console.println("[SERVER]: started! ");
            _console.println("[SERVER]: Listening for connections on port : " + PORT + " ...\n");
            // we listen until user halts server execution
            while (true) {
                Socket client = serverConnect.accept();
                _console.println("[SERVER]: Connection opened with: "+client.toString()+" (" + new Date() + ")");
                ClientHandler handler = new ClientHandler(client, _manager, _console);

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("[SERVER]: Connection error : " + e.getMessage());
        }
    }


}
