package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.IUrl;
import comp.imp.Response;
import comp.imp.Url;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Date;


public class Navigator implements IPlugin {

    @Override
    public float canHandle(IRequest req) {
        IUrl url = req.getUrl();
        String raw = url.getRawUrl();
        float abillity = BASELINE;
        if(raw.contains(this.toString()+"=%3E")){
            abillity *= 1 + (2.5 * (1-abillity));
        }
        if(raw.contains(this.toString())||raw.contains("Navigation")){
            abillity *= 1 + (4 * (1-abillity));
        }
        if(raw.contains("=%3E")){
            abillity *= 1 + (0.1 * (1-abillity));
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
    public IResponse handle(IRequest req)
    {
        IResponse response = new Response();
        response.setStatusCode(200);
        response.setServerHeader("Webio Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", "plain/text");
        IUrl url = req.getUrl();
        String address = String.join("+", url.getParameter().values());

        String embedded;
        if(address.equals("")){
            address = (address.equals(""))?req.getContentString():address;
            IUrl bodyUrl = new Url(address);
            if(address.equals("") || bodyUrl.getParameter().size()==0){
                embedded = "Bitte geben Sie eine Anfrage ein";
            } else {
                String street = bodyUrl.getParameter().get("street");
                street = (street==null)?"":street;
                if(street.equals("")){
                    embedded = "Bitte geben Sie eine Anfrage ein";
                } else {
                    //TODO: FIND....
                    embedded = "Orte gefunden";
                }
            }
        } else {
            embedded = "https://www.google.com/maps?q=" + address+ "&output=embed";
        }
        String result = embedded;
        byte[] jsonData;
        try {
            jsonData = result.getBytes("UTF-8");
        } catch (Exception e) {
            jsonData = result.getBytes();
        }
        response.setContent(jsonData);//<iframe src="https://www.google.com/maps?q=[ADDRESS]&output=embed"></iframe>
        return response;
    }



    @Override
    public String toString(){
        return "Navigator";
    }


    public static final String getXmlElementString(Element parent, String name)
    {
        return null;//getXmlElementString(e);
    }

    public static final String getXmlElementString(Element e)
    {
        if (e == null) return null;
        NodeList nl = e.getChildNodes();
        if (nl.getLength() > 0) { return nl.item(0).getNodeValue(); }
        return null;
    }

    public static final int getXmlElementInt(Element parent, String name, int defaultValue) {
        String value = getXmlElementString(parent, name);
        if (value == null || "".equals(value)) return defaultValue;
        try {
        return Integer.parseInt(value);
        } catch (Exception e){//(TimeFormatException tfe) {
            //Log.e("sync", "Unable to parse DateTime " + value);
            return defaultValue;
        }
    }


}
