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
import java.util.Map;
import java.util.Random;

public class TemperatureReader extends AbstractDatabaseConnection implements IPlugin {

    public TemperatureReader() {

        super("jdbc:sqlite:C:/sqlite/db/TempDB", "", "");

        Connection conn = _createAndOrConnectToDatabase();
        //---
        String[] commands = new String[0];
        File file = new File("db/", "setup.sql");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = util.readFileData(file, fileLength);
            String query = new String(fileData);
            commands = query.split("--<#SPLIT#>--");
            for(String command : commands){
                _execute(command, conn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        _listOfTables(conn);
        _close(conn);
        int startIndex = commands.length-1;
        Thread iot = new Thread(()->{
            long time = 100000000L;
            for(int ti=0; ti<time; ti++){
                try {
                    Thread.sleep(ti);
                } catch (Exception e){

                }
                double temp = Math.cos(1.2324453*ti)*60-15;
                //YYYY-MM-DD HH:MM:SS

                String YYYY = "201"+(Math.abs(new Random().nextInt())%10);
                String MM = ""+(1+Math.abs(new Random().nextInt())%12);
                String DD = ""+(1+Math.abs(new Random().nextInt())%27);
                String hh = ""+(Math.abs(new Random().nextInt())%24);
                String mm = ""+""+(Math.abs(new Random().nextInt())%60);
                String ss = ""+(Math.abs(new Random().nextInt())%60);
                String date = YYYY+"-"+MM+"-"+DD+" "+hh+":"+mm+":"+ss;
                String command =
                        "INSERT INTO temperatures (id, value, created)\n" +
                        "VALUES ("+(startIndex+ti)+", "+temp+", datetime('"+date+"'));";
                Connection iotConn = _createAndOrConnectToDatabase();
                //_listOfTables(iotConn);
                try {
                    _execute(command, iotConn);
                } catch (Exception e){
                    System.out.println("[ERROR]: Could not store new temperature because: "+e.getMessage());
                }

                _close(iotConn);
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
        if(req.getUrl().getExtension().equals("")){
            abillity *= 1 + (0.15 * (1-abillity));
        }
        return abillity;
    }

    @Override
    public IResponse handle(IRequest req) {
        Connection conn = _createAndOrConnectToDatabase();
        IResponse response = new Response();
        response.setStatusCode(200);
        int contentLength = 0;

        String content = (req.getUrl().getParameterCount()>0)?"text/xml":"plain/text";
        if(req.getUrl().getParameter().containsKey("asHtml")&&req.getUrl().getParameter().get("asHtml").equals("true")){
            content = "text/html";
        }

        response.setServerHeader("Webio Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", content);
        response.getHeaders().put("content-length", String.valueOf(contentLength));

        if(req.getContentLength()>0 || req.getUrl().getParameterCount()>0){
            String sql = (req.getContentLength()>0)?util.decodeValue(req.getContentString()):"SELECT * FROM temperatures";
            sql = (sql.substring(0, 6).equals("query=")) ? sql.substring(6, sql.length()) : sql;
            //sql = (sql.equals(""))?"SELECT * FROM temperatures":sql;
            String result = "";
            try {
                Statement stmt= conn.createStatement();
                System.out.println("sql: "+sql);
                ResultSet rs = stmt.executeQuery(sql);

                if(req.getUrl().getParameterCount()==0){
                    result = convert(rs).toString();
                } else {
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
                jsonData = result.getBytes();//result.getBytes("UTF-8");
            } catch (Exception e) {
                jsonData = result.getBytes();
            }
            response.setContent(jsonData);
        }
        _close(conn);
        return response;
    }



}
