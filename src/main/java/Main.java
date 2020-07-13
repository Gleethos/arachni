import core.WebioServer;

public class Main {

    public static void main(String[] args) {
        initialize();
    }

    public static void initialize(){
        new WebioServer().start();
    }


}
