package core;

import comp.IPlugin;
import comp.IPluginManager;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Request;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable
{
    /**
     *  Represents the connection to the client!
     */
    private final Socket _socket;
    /**
     *  Loads and stores server plugins!
     */
    private final IPluginManager _manager;
    /**
     *  Used to log connection specific events and errors
     */
    private final Commander _console;

    /**
     * Constructor!
     * @param socket
     * @param manager
     * @param console
     */
    public ClientHandler(Socket socket, IPluginManager manager, Commander console) {
        _socket = socket;
        _manager = manager;
        _console = console;
        _console.println("[ClientHandler][CLIENT|"+_socket.getInetAddress()+"]: Request handling...");
    }

    /**
     * 1. Receives requests from the client (_socket).
     * 2. Iterates through plugins to find the best one.
     * 3. Let's the best plugin generate a response.
     * 4. Return that response.
     */
    @Override
    public void run() {
        try {
            IRequest request = new Request(_socket.getInputStream());
            _console.println("[ClientHandler][CLIENT|"+_socket.getInetAddress()+"]: Request created!");
            float[] score = {0};
            IPlugin[] best = {null};
            _manager.getPlugins().forEach((plugin -> {
                float ability = plugin.canHandle(request);
                if(ability>score[0]){
                    best[0] = plugin;
                    score[0] = ability;
                }
            }));
            _console.println("[ClientHandler][CLIENT|"+_socket.getInetAddress()+"]: Plugin chosen: "+best[0].toString()+"; Response creation... ");
            IResponse response = best[0].handle(request);
            _console.println("[ClientHandler][CLIENT|"+_socket.getInetAddress()+"]: Response created: "+response.toString());
            try {
                response.send(_socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            _console.println("[ClientHandler][CLIENT|"+_socket.getInetAddress()+"]: Response sent! ");
            _console.println("[ClientHandler][CLIENT|"+_socket.getInetAddress()+"]: Closing client socket now ... \n");
            _socket.close();
        } catch (IOException ioe) {
            _console.println("[ClientHandler][ERROR]: " + ioe);
        }
        _console.println("[ClientHandler][CLIENT|"+_socket.getInetAddress()+"]: Client connection closed now!\n");
    }


}
