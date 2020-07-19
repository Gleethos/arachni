package testsuite.provider;

import comp.IPluginManager;
import comp.IRequest;
import comp.IResponse;
import comp.imp.PluginManager;
import comp.imp.Request;
import comp.imp.Response;

import java.io.InputStream;

public class Test_4_Provider {
    public void helloWorld() {

    }

    public IRequest getRequest(InputStream inputStream) {
        return new Request(inputStream);
    }

    public IResponse getResponse() {
        return new Response();
    }

    public IPluginManager getPluginManager() {
        return new PluginManager();
    }
}
