package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;
import org.json.JSONArray;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

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
            System.out.println("Tail table exists? : "+result);
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
        if(req.getUrl().getRawUrl().toLowerCase().contains("crud")){
            abillity *= 1 + (3.0 * (1-abillity));
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
    public IResponse handle(IRequest req)
    {
        IResponse response = new Response();
        response.setStatusCode(200);
        int contentLength = 0;

        response.setServerHeader("Arachni Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", "text/html");
        response.getHeaders().put("content-length", String.valueOf(contentLength));

        String[] frag = req.getUrl().getSegments();
        String methodName = null;
        for(int i=0; i<frag.length; i++){
            if(frag[i].toLowerCase().contains("crud") && i<frag.length-1) methodName = "_"+frag[i+1];
        }
        if(methodName==null){
            response.setContent(
                    "<div>Hello! This is the CRUD plugin! </div></br>" +
                    "<button onclick=\"redirect()\">Redirect to TailWorld!</button>" +
                    "<script>" +
                            "function redirect(){" +
                                "window.location = window.location.origin+'/tailworld.html';\n" +
                            "}\n" +
                    "</script>");
            return response;
        }
        Method method = null;
        try {
            method = this.getClass().getDeclaredMethod(methodName, IRequest.class, IResponse.class);
        } catch (SecurityException e) { } catch (NoSuchMethodException e) { }
        if(method==null) return response;
        method.setAccessible(true);
        try {
            _createAndOrConnectToDatabase();
            method.invoke(this, req, response);
            try {
                _connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            _close();
        } catch (IllegalArgumentException e) { e.printStackTrace(); }
        catch (IllegalAccessException e) { e.printStackTrace(); }
        catch (InvocationTargetException e) { e.printStackTrace(); }
        return response;
    }

    private void _find(IRequest req,  IResponse response) throws SQLException {
        response.setContent("application/json");
        Statement stmt = _connection.createStatement();
        ArrayList<String> cols = new ArrayList<>();
        String tableName = req.getUrl().getFileName();
        Map<String, String> paramTable = req.getUrl().getParameter();
        Map<String, String> decodedParams = new HashMap<>();
        paramTable.forEach((k,v) -> decodedParams.put(util.decodeValue(k), v));

        Map<String, String[]> tables = _tablesSpace();
        String[] columns = tables.get(tableName);
        for(String column : columns) {
            if(decodedParams.containsKey(column) && !decodedParams.get(column).equals("")) cols.add(column);
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM " + req.getUrl().getFileName() + " WHERE ");
        for(int i=0; i<cols.size(); i++){
            String[] split = cols.get(i).split(" ");
            if(split[1].toLowerCase().contains("text") || split[1].toLowerCase().contains("char")){
                sql.append(split[0]).append(" LIKE \"%").append(decodedParams.get(cols.get(i))).append("%\" ");
            } else {
                sql.append(split[0]).append(" = \"").append(decodedParams.get(cols.get(i))).append("\" ");
            }
            if(i<cols.size()-1) sql.append("AND ");
        }
        String[] tableNames = _listOfAllTables();
        ResultSet rs = stmt.executeQuery(sql.toString());
        String result = _toCRUD(rs,req.getUrl().getFileName(), tableNames).toString();
        byte[] jsonData;
        try {
            jsonData = result.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            jsonData = result.getBytes();
        }
        response.setContent(jsonData);
    }

    private void _search(IRequest req, IResponse response) throws SQLException {
        response.setContentType("text/html");
        Map<String, String[]> space = _tablesSpace();
        String[] searchform = {""};
        space.forEach((table, columns)->{
            StringBuilder form = new StringBuilder("<div id=\"" + table + "_search\" style=\"border: 0.01em solid black; border-radius: 0.1em; padding:0.5em;\">");
            form.append("<div style=\"border: 0.01em solid black; border-radius: 0.1em; padding:0.25em;\"><label>").append(table).append(" - search :</label>");
            form.append("<button onclick=\"loadSearch('").append(table).append("')\">find!</button></div></br>");
            for(String column : columns){
                form.append("<input name=\"").append(column).append("\" placeholder=\"").append(column).append("\"></input>");
            }
            form.append("<div id=\"").append(table).append("_result\"></div>");
            form.append("</div>");
            searchform[0] += form;
        });
        String result = searchform[0] +
                "<script>" +
                "function loadSearch(selector){\n" +
                "    var param = []\n" +
                "    $('#'+selector+'_search').children('input').each(function () {\n" +
                "       param.push(encodeURIComponent(this.name) + '=' + encodeURIComponent(this.value))\n" +
                "    });\n" +
                "    $('#'+selector+'_result').html(\"\").load('CRUD/find/in/'+selector+'?'+param.join('&'));\n" +
                "    $.getJSON( 'CRUD/find/in/'+selector+'?'+param.join('&'), function( data ) {\n" +
                "       var items = [];\n" +
                "       $.each( data, function( key, val ) {\n" +
                "          items.push( \"<li id='\" + key + \"'>\" + val + \"</li>\" );\n" +
                "       });\n" +
                "       \n" +
                "       $( \"<ul/>\", {\n" +
                "          \"class\": \"my-new-list\",\n" +
                "          html: items.join( \"\" )\n" +
                "       }).appendTo('#'+selector+'_result');\n" +
                "   });" +
                "}" +
                "</script>";
        response.setContent(result);
    }




}
