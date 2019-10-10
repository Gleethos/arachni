package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;

public class TestPlugin implements IPlugin
{
    @Override
    public float canHandle(IRequest req) {
        return BASELINE/2;
    }

    @Override
    public IResponse handle(IRequest req) {
        IResponse r = new Response();
        r.setStatusCode(200);
        r.setContent(req.getContentBytes());
        return r;
    }

    @Override
    public String toString(){
        return "TestPlugin";
    }
}
