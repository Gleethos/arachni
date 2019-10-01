package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.IUrl;

public class Navigator implements IPlugin {

    @Override
    public float canHandle(IRequest req) {
        IUrl url = req.getUrl();
        String raw = url.getRawUrl();
        float abillity = BASELINE;
        if(raw.contains(this.toString()+"=>")){
            abillity *= 1 + (0.3 * (1-abillity));
        }
        if(url.getExtension()==""){
            abillity *= 1 + (0.1 * (1-abillity));
        }
        if(url.getFileName()==""){
            abillity *= 1 + (0.1 * (1-abillity));
        }
        return abillity;
    }

    @Override
    public IResponse handle(IRequest req) {
        IUrl url = req.getUrl();
        String raw = url.getRawUrl();
        String route = raw.split(this.toString()+"=>")[1];
        
        return null;
    }
}
