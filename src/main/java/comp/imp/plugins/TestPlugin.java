package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;

public class TestPlugin implements IPlugin
{
    @Override
    public float canHandle(IRequest req) {
        return 0.0f;
    }

    @Override
    public IResponse handle(IRequest req) {
        return null;
    }

    @Override
    public String toString(){
        return "TestPlugin";
    }
}
