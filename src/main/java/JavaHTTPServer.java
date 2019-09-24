import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.PluginManager;
import comp.imp.Request;

import java.io.IOException;
import java.net.Socket;

public class JavaHTTPServer implements Runnable
{
    // Client Connection via Socket Class
    private Socket _socket;
    private PluginManager _manager;

    public JavaHTTPServer(Socket socket) {
        _socket = socket;
        _manager = new PluginManager();
        _manager.add("FileReader");
    }

    @Override
    public void run() {
        try {
            //========
            IRequest request = new Request(_socket.getInputStream());
            float[] score = {0};
            IPlugin[] best = {null};
            _manager.getPlugins().forEach((plugin -> {
                float ability = plugin.canHandle(request);
                if(ability>score[0]){
                    best[0] = plugin;
                }
            }));
            IResponse response = best[0].handle(request);
            response.send(_socket.getOutputStream());
            //========
        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        }
    }


}
