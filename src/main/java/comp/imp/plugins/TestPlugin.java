package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;

public class TestPlugin implements IPlugin
{
    @Override
    public float canHandle(IRequest req) {
        float ability = BASELINE/2;
        String raw = req.getUrl().getRawUrl();
        ability *=
                (
                        (!req.getUrl().getParameter().containsKey("test_IPlugin"))
                                ?((raw.contains("foo.html")&&!raw.contains("test"))?0:1)
                                :1
                );
        return ability;
    }

    @Override
    public IResponse handle(IRequest req) {
        if(req.getUrl().getPath().contains("test")){
            return new FileReader().handle(req);
        }
        FileReader reader = new FileReader();
        return reader.handle(req);
    }

    @Override
    public String toString(){
        return "TestPlugin";
    }
}
