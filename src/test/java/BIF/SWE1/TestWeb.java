package BIF.SWE1;


import org.junit.Test;
import core.WebioServer;

public class TestWeb {

    @Test
    public void testRequest(){
        System.out.println("WORKS!!!");
        WebioServer server = new WebioServer();
        server.start();
    }



}
