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
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Random;

public class CRUD extends AbstractDatabaseConnection implements IPlugin
{

    public CRUD() {
        super("jdbc:sqlite:C:/sqlite/db/TailDB", "", "");
        Connection conn = _createAndOrConnectToDatabase();
        String check = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='tails'";
        int entryCount = 0;
        try{
            Statement stmt= conn.createStatement();
            ResultSet rs = stmt.executeQuery(check);
            String result = _toJSON(rs).toString();
            System.out.println("Temp table exists? : "+result);
            if(result.contains(":0")){
                _executeFile("tailworld_bootstrap.sql", conn);
            } else {
                check = "SELECT count(*) FROM tails";
                rs = stmt.executeQuery(check);
                result = _toJSON(rs).toString();
                result = result.substring(13, result.length()-2);
                entryCount = Integer.parseInt(result);
            }
        } catch (Exception e){

        }
        _executeFile("tailworld_setup.sql", conn);
        _listOfTables(conn);
        _close(conn);

    }

    @Override
    public float canHandle(IRequest req) {
        float abillity = BASELINE;
        if(req.getUrl().getRawUrl().contains("Tail")){
            abillity *= 1 + (0.7 * (1-abillity));
        }
        if(req.getUrl().getRawUrl().contains("TailWorld")){
            abillity *= 1 + (0.17 * (1-abillity));
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
            String sql = (req.getContentLength()>0)?util.decodeValue(req.getContentString()):"SELECT * FROM tails";
            sql = (sql.substring(0, 6).equals("query=")) ? sql.substring(6) : sql;
            //sql = (sql.equals(""))?"SELECT * FROM temperatures":sql;
            String result = "";
            try {
                Statement stmt= conn.createStatement();
                System.out.println("sql: "+sql);

                if(req.getUrl().getParameterCount()==0){
                    ResultSet rs = stmt.executeQuery(sql);
                    result = _toJSON(rs).toString();
                } else {
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
                jsonData = result.getBytes(StandardCharsets.UTF_8);
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
        _close(conn);
        return response;
    }





}
