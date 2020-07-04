package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CRUD extends AbstractDatabaseConnection implements IPlugin
{
    private interface FrontendConsumer {
        FrontendConsumer $(Object s);
    }


    public CRUD() {
        super("jdbc:sqlite:C:/sqlite/db/TailDB", "", "");
    }

    public CRUD(String url, String name, String password) {
        super(url, name, password);
        try { _createAndOrConnectToDatabase(); } catch (Exception e) { e.printStackTrace(); }
        _executeFile("tailworld_bootstrap.sql");
        _executeFile("tailworld_setup.sql");
        _close();
    }

    @Override
    public float canHandle(IRequest req) {
        float abillity = BASELINE;
        if(req.getUrl().getRawUrl().contains("Tail"))  abillity *= 1 + (0.7 * (1-abillity));
        if(req.getUrl().getRawUrl().toLowerCase().contains("crud")) abillity *= 1 + (3.0 * (1-abillity));
        if(req.getUrl().getRawUrl().contains("TailWorld")) abillity *= 1 + (0.17 * (1-abillity));
        if(req.getUrl().getExtension().equals("")) abillity *= 1 + (0.15 * (1-abillity));
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
            if(frag[i].toLowerCase().contains("crud") && i<frag.length-1) methodName = "_"+frag[i+1].split("\\?")[0];
        }
        if(methodName==null){
            try {
                File file = new File(
                        getClass().getClassLoader().getResource("CRUD/default.html").getFile()
                );
                int fileLength = (int) file.length();
                byte[] fileData = util.readFileData(file, fileLength);// send HTTP Headers
                response.setContent(fileData);
            } catch (FileNotFoundException fnfe) {
                try {
                    util.fileNotFound(response, "CRUD/default.html");
                } catch (IOException ioe) {
                    System.err.println("Error with file not found exception : " + ioe.getMessage());
                    response.setContent("Error with file not found exception : " + ioe.getMessage());
                }
            } catch (IOException e) {
                response.setContent("IO-Error : " + e.getMessage());
                e.printStackTrace();
            }
            return response;
        }
        Method method = null;
        try {
            method = this.getClass().getDeclaredMethod(methodName, IRequest.class, IResponse.class);
        } catch (SecurityException e) {
            response.setContent("Security-Error : " + e.getMessage());
        } catch (NoSuchMethodException e) {
            response.setContent("Reflection-Error : " + e.getMessage());
        }
        if(method==null) return response;
        method.setAccessible(true);
        try {
            try {
                _createAndOrConnectToDatabase();
            } catch (SQLException e) {
                response.setContent(e.getMessage());
                e.printStackTrace();
            }
            method.invoke(this, req, response);
            try {
                if(_connection!=null) _connection.commit();
                else return response;
            } catch (SQLException e) {
                response.setContent(e.getMessage());
                e.printStackTrace();
                return response;
            }
            _close();
        } catch (IllegalArgumentException e) { e.printStackTrace(); }
        catch (IllegalAccessException e) { e.printStackTrace(); }
        catch (InvocationTargetException e) { e.printStackTrace(); }
        return response;
    }

    private void _setJDBC(IRequest req,  IResponse response)
    {
        response.setContent("text/html");
        Map<String, String> params = req.getUrl().getParameter();
        String result = "";
        if(params.containsKey("url")) {
            _setUrl(params.get("url").trim());
            result = "JDBC url set to : '"+params.get("url")+"'";
        } else {
            result = "GET parameter key 'url' not found in request!";
        }
        response.setContent(result);

    }

    private void _find(IRequest req,  IResponse response)
    {
        response.setContent("text/html");//"application/json"
        ArrayList<String> cols = new ArrayList<>();
        String tableName = req.getUrl().getFileName();
        Map<String, String> paramTable = req.getUrl().getParameter();
        Map<String, String> decodedParams = new HashMap<>();
        paramTable.forEach( (k,v) -> decodedParams.put(util.decodeValue(k), v) );

        Map<String, List<String>> tables = _tablesSpace();
        List<String> columns = tables.get(tableName).stream().map(Object::toString).collect(Collectors.toList());
        for(String column : columns) {
            if(decodedParams.containsKey(column) && !decodedParams.get(column).equals("")) cols.add(column);
        }
        List<Object> values = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM " + req.getUrl().getFileName());
        if ( !cols.isEmpty() ) sql.append(" WHERE ");
        for ( int i=0; i<cols.size(); i++ ) {
            String[] split = cols.get(i).split(" ");
            if(split[1].toLowerCase().contains("text") || split[1].toLowerCase().contains("char")){
                sql.append(split[0]).append(" LIKE ? ");
                values.add(decodedParams.get("%"+cols.get(i)+"%"));
            } else {
                sql.append(split[0]).append(" = ? ");
                values.add(decodedParams.get(cols.get(i)));
            }
            if ( i < cols.size()-1 ) sql.append("OR ");
        }
        Map<String, List<Object>> map = _query(sql.toString(), values);

        String result = __entitiesToForm( tableName, map, tables );
        try {
            response.setContent(result.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            response.setContent(result.getBytes());
        }
    }

    private String __entitiesToForm(
            String tableName,
            Map<String, List<Object>> map,
            Map<String, List<String>> tables
    ){
        if(map.isEmpty()) return "<div>Nothing found...</div>";

        StringBuilder result = new StringBuilder();
        FrontendConsumer f = new FrontendConsumer() {
            public FrontendConsumer $(Object o) {
                result.append(o.toString());
                return this;
            }
        };
        f.$("<div>");
        int rowCount = map.values().stream().findFirst().get().size();
        String indexAttribute = map.keySet().stream().filter(k->k.contains("id")).findFirst().get();
        for(int i=0; i<rowCount; i++) {
            int inner = i;
            String entityID = map.get(indexAttribute).get(i).toString();
            String rowID = tableName+"_"+entityID;
            f.$("<div id=\""+rowID+"\">");
            map.forEach( (k,v) -> {
                f.$("<div class=\"EntityWrapper\">");
                String htmlID = k.toLowerCase().replace(" ","_")+"_"+entityID;

                f.$(
                        "<span              " +
                        "   value=\"0\"     " +
                        "   id=\""+htmlID+"\"      " +
                        ">                  "
                ).$(
                        k
                ).$(
                        "</span>" +
                        "<input                                 " +
                        "      value=\""+v.get(inner)+"\"       " +
                        "      onchange=\"                                             " +
                        "                 $('#"+htmlID+"').val($('#"+htmlID+"').val()+1);     " +
                        "                 if( $('#"+htmlID+"').val()>10 ) {" +//TODO: Persist
                        "                          $('#"+htmlID+"').val(0);                     " +
                        "                 }                                         " +
                        "      \"                                           " +
                        ">"
                ).$(
                        v.get(inner)
                ).$(
                        "</input>"
                );
                f.$("</div>");
            });
            f.$("</div>");
        }
        f.$("</div>");

        List<String> tableNames = _listOfAllTables();
        String relationTable = tables
                .keySet()
                .stream()
                .filter(k->!k.equals(tableName)&&k.contains("relation"))
                .findFirst().get();
        if(relationTable!=null && !relationTable.isEmpty()) {
            String foreignKey = null;
            List<String> foreignAttributes = tables.get(relationTable);
            for(String attribute : foreignAttributes) {
                if(attribute.contains(tableName)&&attribute.contains("parent")&&attribute.contains("id")) {
                    foreignKey = attribute;
                }
            }
        }
        //TODO: also load children...
        //TODO: parse search result into forms!
        //TODO: add JS logic for sending save and update calls!

        //ResultSet rs = stmt.executeQuery(sql.toString());
        //String result = _toCRUD(rs,req.getUrl().getFileName(), tableNames).toString();
        return result.toString();
    }


    private void _search(IRequest req, IResponse response) throws SQLException {
        response.setContentType("text/html");
        Map<String, List<String>> space = _tablesSpace();
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
                "}" +
                "   function persistRow( selector ) {     " +
                "                                       " +
                "                                       " +
                "                                       " +
                "                                       " +
                "   }                                   "+
                "</script>";
        response.setContent(result);
    }




}
