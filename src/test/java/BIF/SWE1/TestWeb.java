package BIF.SWE1;

import org.junit.Test;
import core.WebioServer;



public class TestWeb {

    @Test
    public void testServerStart(){
        new WebioServer().start();
    }



}
