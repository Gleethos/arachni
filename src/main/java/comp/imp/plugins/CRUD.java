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
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Random;

public class CRUD extends AbstractDatabaseConnection implements IPlugin
{

    public CRUD() {
        super("jdbc:sqlite:C:/sqlite/db/TailDB", "", "");
        _createAndOrConnectToDatabase();
        Connection conn = _connection;
                String check = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='tails'";
        int entryCount = 0;
        try{
            Statement stmt= conn.createStatement();
            ResultSet rs = stmt.executeQuery(check);
            String result = _toJSON(rs).toString();
            System.out.println("Temp table exists? : "+result);
            if(result.contains(":0")){
                _executeFile("tailworld_bootstrap.sql");
            } else {
                check = "SELECT count(*) FROM tails";
                rs = stmt.executeQuery(check);
                result = _toJSON(rs).toString();
                result = result.substring(13, result.length()-2);
                entryCount = Integer.parseInt(result);
            }
        } catch (Exception e){

        }
        _executeFile("tailworld_setup.sql");
        //_listOfTables();
        _close();

    }

    @Override
    public float canHandle(IRequest req) {
        float abillity = BASELINE;
        if(req.getUrl().getRawUrl().contains("Tail")){
            abillity *= 1 + (0.7 * (1-abillity));
        }
        if(req.getUrl().getRawUrl().contains("CRUD")){
            abillity *= 1 + (0.17 * (1-abillity));
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
        _createAndOrConnectToDatabase();
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
            StringBuilder sql = new StringBuilder((req.getContentLength() > 0) ? util.decodeValue(req.getContentString()) : "SELECT * FROM tails");
            sql = new StringBuilder((sql.substring(0, 6).equals("query=")) ? sql.substring(6) : sql.toString());

            String result = "";
            try {
                Statement stmt = _connection.createStatement();
                System.out.println("sql: "+sql);

                if(req.getUrl().getParameterCount()==0){
                    ResultSet rs = stmt.executeQuery(sql.toString());
                    result = _toCRUD(rs, "").toString();
                } else if (req.getUrl().toString().toLowerCase().contains("search")){
                    Map<String, String[]> space = _tablesSpace();
                    String[] searchform = {""};
                    space.forEach((table, columns)->{

                        String form = "<div id=\""+table+"_search\">";
                        form += "<label>"+table+" - search :</label>";
                        form += "<button onclick=\"loadSearch('"+table+"')\">find!</button>";
                        for(String column : columns){
                            form += ("<input name=\""+column+"\" placeholder=\""+column+"\"></input>");
                        }
                        form += "<div id=\""+table+"_result\"></div>";
                        form += "</div>";
                        searchform[0] += form;
                    });
                    result = searchform[0] +
                            "<script>" +
                            "function loadSearch(selector){\n" +
                            "    var param = []\n" +
                            "    $('#'+selector+'_search').children('input').each(function () {\n" +
                            "       param.push(encodeURIComponent(this.name) + '=' + encodeURIComponent(this.value))\n" +
                            "    });\n" +
                            "    $('#'+selector+'_result').html(\"\").load('CRUD/find/in/'+selector+'?'+param.join('&'));\n" +
                            "}" +
                            "</script>";
                } else if(req.getUrl().toString().toLowerCase().contains("find")){

                    ArrayList<String> cols = new ArrayList<>();
                    for(String column : _tablesSpace().get(req.getUrl().getFileName())){
                        if(req.getUrl().getParameter().containsKey(column.split(" ")[0])){
                            cols.add(column);
                        }
                    }

                    sql = new StringBuilder("SELECT * FROM " + req.getUrl().getFileName() + " WHERE ");
                    for(int i=0; i<cols.size(); i++){
                        String[] split = cols.get(i).split(" ");
                        if(split[1].toLowerCase().contains("text")||split[1].toLowerCase().contains("char")){
                            sql.append(split[0]).append(" LIKE \"%").append(req.getUrl().getParameter().get(split[0])).append("%\" ");
                        } else {
                            sql.append(split[0]).append(" = \"").append(req.getUrl().getParameter().get(split[0])).append("\" ");
                        }
                        if(i<cols.size()-1) sql.append("AND ");
                    }

                    ResultSet rs = stmt.executeQuery(sql.toString());
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
            _connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        _close();
        return response;
    }





}
