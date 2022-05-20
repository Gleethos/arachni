import core.Commander;
import core.IOFrame;
import core.Logger;
import core.WebioServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    private static final String GUI = "gui";
    private static final String NO_GUI = "no-"+GUI;

    public static void main(String[] args) {
        initialize(args);
    }

    public static void initialize(String[] args)
    {
        if ( args == null || args.length == 0 )
            args = new String[]{NO_GUI, "run"};

        WebioServer server = null;

        if ( Arrays.stream(args).anyMatch( a -> a.equals(NO_GUI) ) ) // Simple startup!
            server = new WebioServer(
                    WebioServer.DEFAULT_PORT,
                    new OneTimeCommander(args),
                    false,
                    name -> new Logger() {
                        @Override public void println(String text) { System.out.println("["+name+"]> "+text); }
                        @Override public void print(String text) { System.out.print("["+name+"]> "+text); }
                    }
            );
        else
            server = new WebioServer(
                    new IOFrame("Webio - core.WebioServer - commandline", 1000, true, args)
            );

        System.out.println("Staring server on main thread now!");
        server.run();
    }

    private static class OneTimeCommander implements Commander
    {
        List<String> commands = new ArrayList<>();

        private OneTimeCommander(String... args) {
            commands.addAll(Stream.of(args).filter(a -> !a.equals(NO_GUI) ).toList());
        }

        @Override
        public String read() {
            if ( !commands.isEmpty() ) return commands.remove(0);
            return "";
        }

        @Override
        public void println(String text) { System.out.println(text); }

        @Override
        public void print(String text) { System.out.print(text); }
    }

}
