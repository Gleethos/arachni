package BIF.SWE1;


import org.junit.Test;
import core.WebioServer;

public class TestWeb {

    @Test
    public void testRequest(){
        WebioServer server = new WebioServer();
        server.start();
    }



}
