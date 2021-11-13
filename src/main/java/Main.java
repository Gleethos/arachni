import core.IOFrame;
import core.WebioServer;

public class Main {

    public static void main(String[] args) {
        initialize(args);
    }

    public static void initialize(String[] args){
        var server = new WebioServer(
                                new IOFrame("Webio - core.WebioServer - commandline", 1000, true)
                        );
        System.out.println("Staring server on main thread now!");
        server.run();
    }


}
