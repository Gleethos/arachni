package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;

public class TemperatureReader  implements IPlugin {
    @Override
    public float canHandle(IRequest req) {
        return 0;
    }

    @Override
    public IResponse handle(IRequest req) {
        return null;
    }
}
