package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.*;
import java.util.Date;
import java.util.Random;

public class TemperatureReader extends AbstractDatabaseConnection implements IPlugin {

    int _temp_count = 0;

    public int tempCount(){
        return _temp_count;
    }

    public TemperatureReader() {
        super("jdbc:sqlite:C:/sqlite/db/TempDB", "", "");
        Connection conn = null;
        try {
            _createAndOrConnectToDatabase();
            conn = _connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        String check = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='temperatures'";
        int entryCount = 0;
        try{
            Statement stmt= conn.createStatement();
            ResultSet rs = stmt.executeQuery(check);
            String result = _toJSON(rs).toString();
            //System.out.println("Temp table exists? : "+result);
            if(result.contains(":0")){
                _executeFile("bootstrap.sql");
            } else {
                check = "SELECT count(*) FROM temperatures";
                rs = stmt.executeQuery(check);
                result = _toJSON(rs).toString();
                result = result.substring(13, result.length()-2);
                entryCount = Integer.parseInt(result);
            }
        } catch (Exception e){

        }
        _temp_count = entryCount;
        _executeFile("setup.sql");
        _close();
        int startSupply = entryCount;
        Thread iot = new Thread( () -> {
            long time = 100000000L;
            for(int ti=startSupply; ti<time; ti++){
                int tired = ti;
                tired = (tired<10_000)?1:ti-10_000;
                try {
                    Thread.sleep(tired);
                } catch (Exception e){

                }
                double temp = ((1+Math.cos(1.2324453*ti))/2)*(0.5+(Math.cos(new Random().nextInt()*ti)))*60;
                String YYYY = "201"+(Math.abs(new Random().nextInt())%10);
                String MM = ""+(1+Math.abs(new Random().nextInt())%12);
                MM = (MM.length()==1)?"0"+MM:MM;
                String DD = ""+(1+Math.abs(new Random().nextInt())%27);
                DD = (DD.length()==1)?"0"+DD:DD;
                String hh = ""+(Math.abs(new Random().nextInt())%24);
                hh = (hh.length()==1)?"0"+hh:hh;
                String mm = ""+""+(Math.abs(new Random().nextInt())%60);
                mm = (mm.length()==1)?"0"+mm:mm;
                String ss = ""+(Math.abs(new Random().nextInt())%60);
                ss = (ss.length()==1)?"0"+ss:ss;
                String date = YYYY+"-"+MM+"-"+DD+" "+hh+":"+mm+":"+ss;
                String command =
                        "INSERT INTO temperatures (value, created)\n" +
                        "VALUES ("+temp+", datetime('"+date+"'));";
                try {
                    _createAndOrConnectToDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Connection iotConn = _connection;
                try {
                    _execute(command);
                    try {
                        iotConn.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e){
                    System.out.println("[ERROR]: Could not store new temperature because: "+e.getMessage());
                }
                _close();
            }
        });
        iot.start();

    }

    @Override
    public float canHandle(IRequest req) {
        float abillity = BASELINE;
        if(req.getUrl().getRawUrl().contains("Temp")){
            abillity *= 1 + (0.7 * (1-abillity));
        }
        if(req.getUrl().getRawUrl().contains("Temperature")){
            abillity *= 1 + (0.7 * (1-abillity));
        }
        if(req.getUrl().getExtension().equals("")){
            abillity *= 1 + (0.15 * (1-abillity));
        }
        return abillity;
    }

    @Override
    public IResponse handle(IRequest req) {

        IResponse response = new Response();
        response.setStatusCode(200);
        String content = (req.getUrl().getParameterCount()>0)?"text/xml":"plain/text";
        if(req.getUrl().getParameter().containsKey("asHtml")&&req.getUrl().getParameter().get("asHtml").equals("true")){
            content = "text/html";
        }
        response.setServerHeader("Webio Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", content);
        Connection conn = null;
        try {
            _createAndOrConnectToDatabase();
            conn = _connection;
        } catch (Exception e) {
            e.printStackTrace();
            response.setContent(e.getMessage());
        }
        if(req.getContentLength()>0 || req.getUrl().getParameterCount()>0){
            String sql = (req.getContentLength()>0)?util.decodeValue(req.getContentString()):"SELECT * FROM temperatures";
            sql = (sql.substring(0, 6).equals("query=")) ? sql.substring(6, sql.length()) : sql;
            //sql = (sql.equals(""))?"SELECT * FROM temperatures":sql;
            String result = "";
            try {
                Statement stmt= conn.createStatement();
                //System.out.println("sql: "+sql);

                if(req.getUrl().getParameterCount()==0){
                    ResultSet rs = stmt.executeQuery(sql);
                    result = _toJSON(rs).toString();
                } else {
                    if(req.getUrl().getParameter().containsKey("from")||req.getUrl().getParameter().containsKey("date")){
                        String date = req.getUrl().getParameter().get("date");
                        String from = req.getUrl().getParameter().get("from");
                        String to = req.getUrl().getParameter().get("to");
                        from = (from==null)?date:from;
                        to = (to==null)?date:to;
                        from += (from.length()>10)?"":" 00:00:00";
                        to += (to.length()>10)?"":" 23:59:59";
                        sql += " WHERE created > date('"+from+"') AND created < date('"+to+"')";
                    }
                    ResultSet rs = stmt.executeQuery(sql);
                    Document doc = null;
                    try {
                        doc = toDocument(rs);
                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer transformer = null;
                        try {
                            transformer = tf.newTransformer();
                        } catch (TransformerConfigurationException e) {
                            e.printStackTrace();
                        }
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        StringWriter writer = new StringWriter();
                        try {
                            transformer.transform(new DOMSource(doc), new StreamResult(writer));
                        } catch (TransformerException e) {
                            e.printStackTrace();
                        }
                        result = writer.getBuffer().toString().replaceAll("\n|\r", "");
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                result+=e.toString();
            }
            byte[] jsonData;
            try {
                jsonData = result.getBytes("UTF-8");
            } catch (Exception e) {
                jsonData = result.getBytes();
            }
            response.setContent(jsonData);
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        _close();
        return response;
    }



}
