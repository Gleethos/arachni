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
    // Client Connection via Socket Class
    private Socket _socket;
    private IPluginManager _manager;
    private IOFrame _console;

    public ClientHandler(Socket socket, IPluginManager manager, IOFrame console) {
        _socket = socket;
        _manager = manager;
        _console = console;
        _console.println("["+_socket.getInetAddress()+"]: Request handling...");
    }

    @Override
    public void run() {
        try {
            //========
            IRequest request = new Request(_socket.getInputStream());
            _console.println("["+_socket.getInetAddress()+"]: Request created!");
            float[] score = {0};
            IPlugin[] best = {null};
            _manager.getPlugins().forEach((plugin -> {
                float ability = plugin.canHandle(request);
                if(ability>score[0]){
                    best[0] = plugin;
                    score[0] = ability;
                }
            }));
            _console.println("["+_socket.getInetAddress()+"]: Plugin chosen: "+best[0].toString()+"; Response creation... ");
            IResponse response = best[0].handle(request);
            _console.println("["+_socket.getInetAddress()+"]: Response created: "+response.toString());
            try {
                response.send(_socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            _console.println("["+_socket.getInetAddress()+"]: Response sent! ");
            _console.println("["+_socket.getInetAddress()+"]: Closing client socket now ... \n");
            _socket.close();

            //========
        } catch (IOException ioe) {
            _console.println("core.WebioServer error : " + ioe);
        }
        _console.println("["+_socket.getInetAddress()+"]: Client connection closed now!\n");
    }


}
