package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.IUrl;
import comp.imp.Response;

import java.util.Date;


public class ToLower implements IPlugin
{
    @Override
    public float canHandle(IRequest req) {
        IUrl url = req.getUrl();
        String raw = url.getRawUrl();
        float abillity = BASELINE;
        if (raw.contains(this.toString()) || raw.contains("Lowering") || raw.contains("ToLower") || raw.contains("ToBeLower")) {
            abillity *= 1 + (6 * (1 - abillity));
        }
        if (url.getExtension() == "") {
            abillity *= 1 + (0.1 * (1 - abillity));
        }
        if (url.getFileName() == "") {
            abillity *= 1 + (0.1 * (1 - abillity));
        }
        return abillity;
    }

    @Override
    public IResponse handle(IRequest req) {
        IResponse response = new Response();
        response.setStatusCode(200);
        String content = "plain/text";
        response.setServerHeader("Webio Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", content);
        String toBeLowered = util.decodeValue(req.getContentString()).toLowerCase();
        toBeLowered = (toBeLowered.substring(0, 5).equals("text=")) ? toBeLowered.substring(5, toBeLowered.length()) : toBeLowered;
        if (toBeLowered.equals("")) toBeLowered = "Bitte geben Sie einen Text ein";
        byte[] jsonData;
        try {
            jsonData = toBeLowered.getBytes("UTF-8");
        } catch (Exception e) {
            jsonData = toBeLowered.getBytes();
        }
        response.setContent(jsonData);
        return response;
    }

    @Override
    public String toString() {
        return "ToLower";
    }


}
