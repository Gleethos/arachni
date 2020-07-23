package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;
import comp.imp.Url;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CRUD extends AbstractDatabaseConnection implements IPlugin
{

    public CRUD() {
        super("jdbc:sqlite:"+new File("storage/dbs").getAbsolutePath()+"/CRUD_WORLD_DB", "", "");
    }

    public CRUD(String url, String name, String password)
    {
        super(url, name, password);
        try { _createAndOrConnectToDatabase(); } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Test constructor!
     * @param url
     */
    public CRUD(String url, String worldfolder)
    {
        super(url, "", "");
        _initializeWorldSource(worldfolder);
    }

    private void _initializeWorldSource( String worldfolder ) {
        try { _createAndOrConnectToDatabase(); } catch (Exception e) { e.printStackTrace(); }
        List<String> filesFound = new ArrayList<>();
        worldfolder = (worldfolder.contains(":"))?worldfolder:"storage/sql/"+worldfolder;
        File folder = new File(worldfolder);
        for ( final File f : folder.listFiles() ) {
            if (f.isFile()) {
                if (f.getName().endsWith(".sql")) filesFound.add(f.getAbsolutePath());
            }
        }
        String bootstrap = filesFound.stream().filter(f->f.endsWith("bootstrap.sql")).findFirst().get();
        String setup = filesFound.stream().filter(f->f.endsWith("setup.sql")).findFirst().get();
        if( bootstrap == null ) throw new IllegalStateException("CRUD could not read bootstrap file in folder '"+worldfolder+"'!");
        if( setup == null ) throw new IllegalStateException("CRUD could not read setup file in folder '"+worldfolder+"'!");
        _executeFile(bootstrap);
        _executeFile(setup);
        _close();
    }

    @Override
    public float canHandle(IRequest req)
    {
        float abillity = BASELINE;
        if(req.getUrl().getRawUrl().contains("world"))  abillity *= 1 + (0.7 * (1-abillity));
        if(req.getUrl().getRawUrl().toLowerCase().contains("crud")) abillity *= 1 + (3.0 * (1-abillity));
        if(req.getUrl().getRawUrl().toLowerCase().contains("crudworld")) abillity *= 1 + (0.17 * (1-abillity));
        if(req.getUrl().getExtension().equals("")) abillity *= 1 + (0.15 * (1-abillity));
        return abillity;
    }

    @Override
    public IResponse handle(IRequest req)
    {
        IResponse response = new Response();
        response.setStatusCode(200);

        response.setServerHeader("Arachni Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", "text/html");

        String[] frag = req.getUrl().getSegments();
        String methodName = null;
        for(int i=0; i<frag.length; i++){
            if(frag[i].toLowerCase().contains("crud") && i<frag.length-1) methodName = "_"+frag[i+1].split("\\?")[0];
        }
        Method method = null;
        try {
            if(methodName==null) method = this.getClass().getDeclaredMethod("_view", IRequest.class, IResponse.class);
            else method = this.getClass().getDeclaredMethod(methodName, IRequest.class, IResponse.class);
        } catch (SecurityException e) {
            response.setContent("Security-Error : " + e.getMessage());
        } catch (NoSuchMethodException e) {
            response.setContent("Reflection-Error : " + e.getMessage());
        }
        if(method==null) return response;
        method.setAccessible(true);
        try {
            _createAndOrConnectToDatabase(response);
            method.invoke(this, req, response);
            _commit(response);
            _close();
        } catch (IllegalArgumentException e) {
            if(e.getMessage()!=null)response.setContent(e.getMessage());
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            if(e.getMessage()!=null)response.setContent(e.getMessage());
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            if(e.getMessage()!=null) response.setContent(e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    private void _setJDBC(IRequest req,  IResponse response)
    {
        response.setContentType("text/html");
        Map<String, String> params = req.getUrl().getParameter();
        String result = "";
        if( params.containsKey("db_url") ) {
            String url = params.get("db_url").trim();
            if ( !url.startsWith("jdbc:sqlite:") ) {
                if (url.contains(":")) url = "jdbc:sqlite:" + url;
                else url = "jdbc:sqlite:" + new File("storage/dbs/" + url).getAbsolutePath();
            }
            _setUrl(url);
            result += "JDBC url set to : '"+url+"'.\n";
        } else {
            result += "GET parameter key 'db_url' not found in request!\n";
        }
        if( params.containsKey("sql_source") ) {
            _initializeWorldSource(params.get("sql_source"));
            result += "SQL source set to : '"+params.get("sql_source")+"'.\n";
        } else {
            result += "GET parameter key 'sql_source' not found in request!\n";
        }
        response.setContent(result);

    }

    private Map<String, String> _defaultEntitySetting(IRequest req, Map<String, String> paramTable){
        Map<String, String> settingsTable = new TreeMap<>(Map.of(
                "appendRelations",  "true",
                "noRow",            "false",
                "appendButtons",    "true",
                "searchQuickly",    "false"
        ));
        paramTable.putAll(req.getUrl().getParameter());
        paramTable.forEach((k,v)->{ if(settingsTable.containsKey(k)) settingsTable.put(k,v); });
        if( req.getMethod().equals("POST") ) {
            paramTable.putAll(new Url(req.getContentString()).getParameter());
        }
        settingsTable.forEach((k,v)->paramTable.remove(k));
        return settingsTable;
    }

    private void _save(IRequest req,  IResponse response)
    {
        response.setContentType("text/html");
        String tableName = req.getUrl().getFileName();
        Map<String, String> paramTable = req.getUrl().getParameter();
        Map<String, String> settingTable = _defaultEntitySetting(req, paramTable);

        Map<String, List<String>> tables = _tablesSpace();
        if(!tables.containsKey(tableName)){
            response.setStatusCode(500);
            response.setContent("Cannot save entity '"+tableName+"'! : Table not found in database!");
            _close();
            return;
        }
        Map<String, List<String>> attributes = __attributesPropertiesTableOf(tables.get(tableName));

        List<String> columns = attributes.keySet().stream().collect(Collectors.toList());
        for(String column : columns) {
            if (!paramTable.containsKey(column) && column.equals("created")) {
                paramTable.put("created", new java.sql.Date(System.currentTimeMillis()).toString());
            }
        }
        if(!_execute(__generateSaveSQLFor(paramTable, tableName), response)) return;
        int lastID = _lastInsertID();
        _commit(response);
        _close();
        _createAndOrConnectToDatabase(response);
        String foundParamID = paramTable.get("id");
        req.getUrl().getParameter().clear();
        req.getUrl().getParameter().put("id", Objects.requireNonNullElseGet(foundParamID, () -> String.valueOf(lastID)));
        req.getUrl().getParameter().putAll(settingTable);
        _find(req, response);

    }

    private String __generateSaveSQLFor(Map<String, String> inserts, String tableName)
    {
        List<String> attributes = new ArrayList<>(inserts.keySet());
        List<String> values = new ArrayList<>(inserts.values()).stream().map(o->"'"+o+"'").collect(Collectors.toList());

        String id = (inserts.get("id")==null || inserts.get("id").equals(""))?"":inserts.get("id");
        if(id.equals("")) {
            return "INSERT INTO "+tableName+"\n"+
                    "("+String.join(", ",attributes)+")\n"+
                    "VALUES\n"+
                    "("+String.join(",", values)+")";
        } else {
            List<String> pairs = new ArrayList<>();
            for (int i=0; i<attributes.size(); i++) pairs.add(attributes.get(i)+" = "+values.get(i));
            return "UPDATE "+tableName+"\n"+
                    "SET\n" +
                    String.join(", ",pairs) +"\n" +
                    "WHERE id = "+id;
        }
    }

    private void _find(IRequest req,  IResponse response)
    {
        response.setContentType("text/html");
        String tableName = req.getUrl().getFileName();

        Map<String, String> paramTable = new TreeMap<>();
        Map<String, String> settingTable = _defaultEntitySetting(req, paramTable);
        if(req.getUrl().getParameter().containsKey("id")) paramTable = Map.of("id", req.getUrl().getParameter().get("id"));

        boolean quickSearch = settingTable.get("searchQuickly").equals("true");

        Map<String, List<String>> tables = _tablesSpace();

        Map<String, List<Object>> map =
                (quickSearch) ?
                        __findQuickly(
                                response,
                                req,
                                tables,
                            tableName,
                            paramTable
                        )
                        :
                        __findEntities(
                                tables,
                            tableName,
                            paramTable
                        );

        String result = "";
        if ( quickSearch )
        {
            String keyAttribute = map.keySet().stream().findFirst().get();
            CRUDBuilder b = new CRUDBuilder( tables );
            b.$("<div id=\""+tableName+"_quick_search_result\" class=\"row\">")
                .$("<div class=\"col-sm-12 col-md-12 col-lg-12\">");
            b.$("<span><h3> "+b._snakeToTitle(keyAttribute)+"(s) :</h3></span>");
            b.$("</div>");

            int numberOfFound = map.get(keyAttribute).size();
            for( int i=0; i<numberOfFound; i++ ) {
                Object value = map.get(keyAttribute).get(i);
                b.$("<div class=\"col-sm-12 col-md-6 col-lg-4 contentBox\">");
                b.$("<a style=\"padding:0.25em;\" onclick=\"")
                        .$("$('#"+tableName+"_id_search_input').val('"+map.get("id").get(i)+"');")
                        .$("loadFoundForEntity('").$(
                        tableName
                ).$("');$('#"+tableName+"_quick_search_result').replaceWith('');\">")
                        .$(value.toString())
                        .$("</a>");
                b.$("</div>");
            }
            b.$("</div>");
            result = b.toString();
        }
        else
        {
            result = new CRUDBuilder(tables).entitiesToForm( tableName, map, settingTable ).toString();
        }
        if(result.isBlank())  response.setContent("Nothing found!");
        else response.setContent(result);
    }

    private Map<String, List<Object>> __findQuickly(
            IResponse response,
            IRequest req,
            Map<String, List<String>> tables,
            String innerTableName,
            Map<String, String> paramTable
    ){
        String outerTableName = "";
        if ( innerTableName.contains("->") ) { // Relational quick search!!
            String[] parts = innerTableName.split("->");
            innerTableName = parts[0];
            outerTableName = parts[1];
        }
        if ( !tables.containsKey(innerTableName) ) {
            response.setStatusCode(500);
            response.setContent("Cannot find entity '"+innerTableName+"'! : Table not found in database!");
            _close();
            return null;
        }
        if ( !outerTableName.isBlank() && !tables.containsKey(outerTableName) ) {
            response.setStatusCode(500);
            response.setContent("Cannot find entity '"+outerTableName+"'! : Table not found in database!");
            _close();
            return null;
        }
        if ( req.getMethod().equals("GET") ) {
            String message = "";
            if ( !paramTable.containsKey(innerTableName+"_quick_search_parameter") ) {
                message += "ERROR : Quick-Search expects url parameter '"+innerTableName+"_quick_search_parameter'!\n";
            }
            if ( !outerTableName.isBlank() ){
                if ( !paramTable.containsKey(outerTableName+"_quick_search_parameter") ) {
                    message += "ERROR : Quick-Search expects url parameter '"+outerTableName+"_quick_search_parameter'!\n";
                }
                if ( !paramTable.containsKey("relation_table_name") ) {
                    message += "ERROR : Quick-Search expects url parameter 'relation_table_name'!\n";
                } else if ( !tables.containsKey(paramTable.get("relation_table_name")) ) {
                    message += "ERROR : Relation table with name '"+paramTable.get("relation_table_name")+"' not found!";
                }
                if ( !paramTable.containsKey("key_relation") ) {
                    message += "ERROR : Quick-Search expects url parameter 'key_relation'!\n";
                } else if ( !paramTable.get("key_relation").contains("->") ) {
                    message += "ERROR : Quick-Search expects value '"+paramTable.get("key_relation")
                            +"' of url parameter 'key_relation' to contain '->' identifier!\n";
                }
            }
            if ( !message.isBlank() ) {
                response.setStatusCode(500);
                response.setContent(message);
                _close();
                return null;
            }
        }

        ArrayList<String> searchAttributes = new ArrayList<>();
        Map<String, List<String>> attributesProperties = __attributesPropertiesTableOf(tables.get(innerTableName));

        List<String> attributes = new ArrayList<>(attributesProperties.keySet());

        if ( paramTable.containsKey( innerTableName+"_quick_search_parameter" ) ) {
            for ( String a : attributes ) paramTable.put( a, paramTable.get(innerTableName+"_quick_search_parameter") );
        }

        for ( String a : attributes ) {
            if ( paramTable.containsKey(a) && !paramTable.get(a).equals("") ) searchAttributes.add(a);
        }

        String keyAttribute = "id";
        String[] preferenceList = new String[]{ "description", "name", "title" };
        int matchId = -1;
        for( String currentAttribute : attributes ) {
            for (int i=0; i<preferenceList.length; i++) {
                if ( currentAttribute.equals(preferenceList[i]) && i>matchId ) {
                    keyAttribute = currentAttribute;
                    matchId = i;
                }
            }
        }

        List<Object> values = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, "+keyAttribute+" FROM " + innerTableName);

        if ( !searchAttributes.isEmpty() ) sql.append(" WHERE ");
        for ( int i=0; i < searchAttributes.size(); i++ ) {
            String type = attributesProperties.get(searchAttributes.get(i)).get(0);
            if ( type.toLowerCase().contains("text") || type.toLowerCase().contains("char") ) {
                sql.append(searchAttributes.get(i)).append(" LIKE ? ");
                values.add("%"+paramTable.get(searchAttributes.get(i))+"%");
            } else {
                sql.append(searchAttributes.get(i)).append(" = ? ");
                values.add(paramTable.get(searchAttributes.get(i)));
            }
            if ( i < searchAttributes.size()-1 ) sql.append("OR ");
        }

        return _query(sql.toString(), values);
    }

    private Map<String, List<Object>> __findEntities(
            Map<String, List<String>> tables,
            String tableName,
            Map<String, String> paramTable
    ){
        ArrayList<String> searchAttributes = new ArrayList<>();
        Map<String, List<String>> attributesProperties = __attributesPropertiesTableOf(tables.get(tableName));

        List<String> attributes = new ArrayList<>(attributesProperties.keySet());

        for ( String a : attributes ) {
            if ( paramTable.containsKey(a) && !paramTable.get(a).equals("") ) searchAttributes.add(a);
        }

        List<Object> values = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        if ( !searchAttributes.isEmpty() ) sql.append(" WHERE ");
        for ( int i=0; i < searchAttributes.size(); i++ ) {
            String type = attributesProperties.get(searchAttributes.get(i)).get(0);
            if ( type.toLowerCase().contains("text") || type.toLowerCase().contains("char") ) {
                sql.append(searchAttributes.get(i)).append(" LIKE ? ");
                values.add("%"+paramTable.get(searchAttributes.get(i))+"%");
            } else {
                sql.append(searchAttributes.get(i)).append(" = ? ");
                values.add(paramTable.get(searchAttributes.get(i)));
            }
            if ( i < searchAttributes.size()-1 ) sql.append("OR ");
        }
        return _query(sql.toString(), values);
    }


    private Map< String, Map<String, String> > __findRelationTablesOf (
            String tableName,
            Map<String,
            List<String>> tables,
            Function<String, Boolean> filter
    ) {
        List<String> relationTables = tables
                .keySet()
                .stream()
                .filter( k -> !k.equals(tableName) && k.contains("relation") )
                .collect(Collectors.toList());
        Map<String,Map<String,String>> found = new TreeMap<>();
        for ( String relationTable : relationTables ) found.put(relationTable, new TreeMap<>());
        for ( String relationTable : relationTables ) {
            if( !relationTable.isEmpty() ) {
                List<String> foreignAttributes = tables.get(relationTable);
                for(String attribute : foreignAttributes) {
                    if ( filter.apply(attribute) ) {
                        String attributeName = attribute.split(" ")[0];
                        attribute = attribute.substring(attributeName.length()+1);
                        if(
                            !attributeName.equals("id") &&
                            attributeName.contains("_id") &&
                            attribute.toUpperCase().contains("REFERENCES")
                        ) {
                            found.get(relationTable).put(attributeName, attribute);
                        }
                    }
                }
            }
        }
        return found;
    }

    private String __toSingular( String word ) {
        switch ( word ) {
            case "people" : return "person";
            case "men" : return "man";
            case "mice" : return "mouse";
            case "children" : return "child";
        }
        if ( word.endsWith("s") ) return word.substring( 0, word.length() - 1 );
        return word;
    }

    private String __toPlural( String word ) {
        switch ( word ) {
            case "person" : return "people";
            case "man" : return "men";
            case "mouse" : return "mice";
            case "child" : return "children";
        }
        if ( !word.endsWith("s") ) return word + "s";
        return word;
    }

    private void _delete( IRequest req, IResponse response )
    {
        response.setContentType("text/javascript");
        String tableName = req.getUrl().getFileName();
        Map<String, String> paramTable = req.getUrl().getParameter();
        if( req.getMethod().equals("POST") && !paramTable.containsKey("id") ) paramTable.putAll(new Url(req.getContentString()).getParameter());

        if( !paramTable.containsKey("id") ) {
            response.setStatusCode(500);
            response.setContentType("text/html");
            response.setContent("Deletion failed! Request does not contain 'id' value!");
            return;
        }
        Map< String, List<String> > tables = _tablesSpace();
        Map< String, Map<String, String> > relationTables = __findRelationTablesOf (
                tableName,
                tables,
                s -> s.contains( "REFERENCES " + tableName )
        );
        List<String> deletionIdentifier = new ArrayList<>();
        relationTables.forEach(
            (table, foreignKeys) -> {
                for(String foreignKey : foreignKeys.keySet()) {
                    Map<String, List<Object>> toBeDeleted = _query(
                            "SELECT id FROM "+table+" WHERE "+foreignKey+" = "+paramTable.get("id")
                    );
                    List<Object> ids = toBeDeleted.get("id");
                    for ( Object id : ids ) deletionIdentifier.add(table+"_"+id.toString());
                    String sql = "DELETE FROM "+table+" WHERE "+foreignKey+" = "+paramTable.get("id");
                    _execute(sql, response);
                }
            }
        );
        response.setContent(
                deletionIdentifier
                        .stream()
                        .map(e->"$('#"+e+"').replaceWith('');")
                        .collect(Collectors.joining("\n"))
        );
        String sql = "DELETE FROM "+tableName+" WHERE id = "+paramTable.get("id");
        _execute(sql, response);
    }

    private void _view( IRequest req, IResponse response ) {
        response.setContentType("text/html");

        String fileData = new util().readResource("CRUD/crudworld.html");// send HTTP Headers
        Map<String, List<String>> tables = _tablesSpace();

        fileData = fileData.replace(
                "--<#AVAILABLE#DATABASES#>--",
                new CRUDBuilder(tables).folderFileButtonsForTarget(
                        "storage/dbs",
                        "jdbc_world_url",
                        f -> f.isFile()
                ).toString()
        );
        fileData = fileData.replace(
                "--<#AVAILABLE#SQL#SOURCES#>--",
                new CRUDBuilder(tables).folderFileButtonsForTarget(
                        "storage/sql",
                        "sql_world_source",
                        f -> f.isDirectory()
                ).toString()
        );
        response.setContent(fileData);
    }



    private void _world( IRequest req, IResponse response )
    {
        response.setContentType("text/html");
        Map<String, List<String>> tables = _tablesSpace();
        CRUDBuilder f = new CRUDBuilder(tables);
        f.tabsOf(
                new ArrayList<>(tables.keySet()),
                tableName -> {
                    f.$("<div class = \"mainContentWrapper col-sm-12 col-md-12 col-lg-12\">")
                        .$("<div class = container-fluid>")
                            .$("<div class=\"SearchWrapper row\">")//row?
                                .$("<div class=\"col-sm-12 col-md-12 col-lg-12\">")
                                .$("<h1>").$(f._snakeToTitle(tableName)).$("</h1>")
                                .$("</div>")
                                .$("<div class=\"col-sm-12 col-md-6 col-lg-6\">")
                                    .$("<label>Total stored: "+_query("SELECT COUNT(*) FROM "+tableName).get("COUNT(*)").get(0)+"</label>")
                                .$("</div>")
                                .$("<div class=\"col-sm-12 col-md-6 col-lg-6\">")
                                    .$("<button onclick=\"$('#"+tableName+"_result').html('');\">CLEAR</button>")
                                    .$("<button onclick=\"loadFoundForEntity('").$(tableName).$("')\">SEARCH</button>")
                                    .$("<button onclick=\"loadQuickSearchForEntity('").$(tableName).$("')\">QUICK SEARCH</button>")
                                .$("</div>");

                    Map<String, Map<String, String>> relationTables = __findRelationTablesOf(tableName, tables, s->true);
                    List<String> relationTableList = new ArrayList<>(relationTables.keySet());
                    for ( String relationTableName : relationTableList ) {
                        f._forRelationKeys(
                                relationTables.get(relationTableName),
                                tableName,
                                (innerKey, outerKey, outerTable) ->{
                                    // TODO!
                                }
                        );
                    }
                                List<String> columns = tables.get(tableName);
                                f.tabsOf(
                                        Map.of(
                                                "quick",
                                                searchType -> {
                                                    f.$("<div class=\"SearchHead col-sm-12 col-md-12 col-lg-12\">");
                                                    f.$("<input style=\"width:100%;\"")
                                                            .$("name=\"search\" ")
                                                            .$("placeholder=\"anything\"")
                                                            .$("id=\""+tableName+"_quick_search_input"+"\"")
                                                            .$("oninput=\"");
                                                    for(String c : columns){
                                                        String attributeName = c.split(" ")[0];
                                                        f.$("$('#").$(tableName+"_"+attributeName+"_search_input')")
                                                                .$(".val($('#"+tableName+"_quick_search_input').val());\n"
                                                        );
                                                    }
                                                            f.$("loadQuickSearchForEntity('"+tableName+"');");
                                                            f.$("\"")
                                                            .$(">");
                                                    f.$("</div>");
                                                },
                                                "specific",
                                                searchType -> {
                                                    f.$("<div id=\"" + tableName + "_search\" class=\"SearchHead col-sm-12 col-md-12 col-lg-12\">");
                                                    for(String c : columns){
                                                        String attributeName = c.split(" ")[0];
                                                        f.$("<input ")
                                                                .$("name=\"").$(attributeName).$("\" ")
                                                                .$("placeholder=\"").$(c).$("\"")
                                                                .$("id=\"").$(tableName+"_"+attributeName+"_search_input").$("\"")
                                                        .$(">");
                                                    }
                                                    f.$("</div>");
                                                }
                                        ),
                                        "noRow"
                                );
                            f.$("</div>")
                            .$("<div class=\"row\">")
                                .$("<div class=\"col-sm-12 col-md-12 col-lg-12\">")
                                    .$("<div id=\"").$(tableName).$("_result\" class=\"SearchResult\"></div>")
                                .$("</div>")
                            .$("</div>")
                                    .generateNewButton(
                                    List.of(tableName),
                                    e -> f.entitiesToForm(tableName, e.get(0), Map.of("appendRelations", "true")),
                                    "", ""
                            )
                        .$("</div>")
                    .$("</div>");
                },
                "root"
        );
        response.setContent(f.toString());
    }


    /**
     * <-------------------------------------------------------------------------->
     *     END OF MAIN IMPLEMENTATION - FOLLOWING : NESTED HTML BUILDER CLASS !
     * </------------------------------------------------------------------------->
     */

    interface RelationKeyConsumer {
        void accept(String innerKey, String outerKey, String outerTable);
    }

    private interface FrontendConsumer { FrontendConsumer $(Object s); }

    private class CRUDBuilder
    {
        private StringBuilder _builder = new StringBuilder();
        private Map<String, List<String>> _tables;

        CRUDBuilder(Map<String, List<String>> tables){
            _tables = tables;
        }

        public CRUDBuilder $(Object o) {
            _builder.append( ( o==null ) ? "" : o.toString() );
            return this;
        }

        private String _snakeToClass(String s){
            List<String> parts = Arrays.asList(s.split("_"))
                    .stream()
                    .map(word->word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.toList());
            return String.join("", parts);
        }

        private String _snakeToText(String s){
            s = s.substring(0, 1).toUpperCase() + s.substring(1);
            return s.replace("_", " ");
        }

        private String _snakeToTitle(String s){
            List<String> parts = Arrays.asList(s.split("_"))
                    .stream()
                    .map(word->word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.toList());
            return String.join(" ", parts);
        }

        private CRUDBuilder folderFileButtonsForTarget(String path, String targetSelector, Function<File, Boolean> check){
            File folder = new File( path );
            for ( final File f : folder.listFiles() ) {
                if ( check.apply(f) ) {
                    String filePath = f.getAbsolutePath().replace("\\", "/");
                    $("<button onclick=\"$('#"+targetSelector+"').val('"+filePath+"');\">");
                    $(f.getAbsoluteFile());
                    $("</button>");
                }
            }
            return this;
        }

        private void tabsOf(Map<String, Consumer<String>> tabGenMap, String tabType){
            tabsOf(
                    new ArrayList<>(tabGenMap.keySet()),
                    searchType -> tabGenMap.get(searchType).accept(searchType),
                    tabType
            );
        }

        private void tabsOf(List<String> tabNames, Consumer<String> lambda){
            tabsOf(tabNames, lambda, "default");
        }

        private void tabsOf(List<String> tabNames, Consumer<String> lambda, String tabType)
        {
            String colSizes = "col-sm-12 col-md-12 col-lg-12";
            colSizes = (tabType.contains("root"))?"":colSizes;
            String additionalHeadStyles = (tabType.contains("root"))?"font-size:1em;":"";
            $("<div class=\"tabWrapper "+colSizes+"\">\n<div class=\"tabHead\" style=\""+additionalHeadStyles+"\">\n");
            String selected = "selected";
            for(String type : tabNames) {
                $("<button onclick=\"switchTab(event, '."+_snakeToClass(type)+"Tab')\" class=\""+selected+"\">"+_snakeToTitle(type)+"</button>\n");
                selected = "";
            }
            String additionalClasses = (tabType.contains("root"))?"":"LightTopShadow";
            $("</div>\n<div class=\"tabBody "+additionalClasses+"\">\n");
            String rowClass = (tabType.contains("root")||tabType.contains("noRow"))?"":"row";
            String displayNone = "display:flex";
            for( String type : tabNames ) {
                $("<div class=\""+_snakeToClass(type)+"Tab "+rowClass+"\" style=\""+displayNone+"\">\n");
                lambda.accept(type);
                $("</div>\n");
                displayNone = "display:none";
            }
            $("</div>\n</div>\n");
        }


        private CRUDBuilder generateNewButton (
                List<String> tableNames,
                Consumer< List<Map<String, List<Object>>>> templateLambda,
                String id, String join
        ) {
            String today = new java.sql.Date( System.currentTimeMillis() ).toString();
            List<Map<String, List<Object>>> templates = new ArrayList<>();

            for( String table : tableNames ) {
                Map<String, List<Object>> templateEntity = new HashMap<>();
                List<String> columns = _tables.get(table);
                for(String c : columns) templateEntity.put(c.split(" ")[0], List.of((c.split(" ")[0].equals("created"))?today:""));
                templates.add(templateEntity);
            }

            String table = String.join("_and_", tableNames);
            table = ( id.isBlank() ) ? table : table+"_"+id ;
            table = ( join.isBlank() ) ? table : table + "_joined_on_"+join;
            $("<script>");
            $(" function new_"+table+"() {");
            $("$('#").$(table).$("_result').append(`");
            templateLambda.accept(templates);
            $("`);");
            $(" }");
            $("</script>\n");
            $("<button onclick=\"new_"+table+"()\">");
            $("NEW "+table.replace("_", " ").toUpperCase());
            $("</button>\n");

            return this;
        }

        private String entitiesToForm(
                String tableName,
                Map<String, List<Object>> entities,
                Map<String, String> settingsTable
        ) {
            boolean appendRelations = settingsTable.get("appendRelations").equals("true");
            boolean appendButtons   = (settingsTable.containsKey("appendButtons"))?settingsTable.get("appendButtons").equals("true"):true;

            if(entities.isEmpty()) return "<div>Nothing found...</div>";
            String indexAttribute = entities.keySet().stream()
                    .filter(k->k.contains("id"))
                    .sorted((x, y) -> Integer.compare(y.length(), x.length()))
                    .findFirst()
                    .get();

            Function<Map<String, String>, String> entityID = e -> (e.get(indexAttribute).equals(""))?"new":e.get(indexAttribute);
            Function<Map<String, String>, String> rowID = e -> tableName+"_"+entityID.apply(e)+((appendRelations)?"":"_related");
            return entitiesToForm(
                    tableName,
                    entities,
                    (appendButtons) ? Map.of(
                            "close", e->"$( '#"+rowID.apply(e)+"' ).replaceWith('');",
                            "save", e->
                                    "loadSavedForEntity( "+
                                    "'"+tableName+"',   " +
                                    "'"+entityID.apply(e)+"',    "+
                                    ((appendRelations)?"''":"'?appendRelations=false'")+
                                    ")",
                            "delete", e->"deleteEntity( '"+tableName+"', '"+entityID.apply(e)+"' )"
                    ) : Map.of(),
                    settingsTable
            );
        }

        private String entitiesToForm(
                String tableName,
                Map<String, List<Object>> entities,
                Map<String, Function<Map<String, String>,String>> onclickGenerators,
                Map<String, String> settingsTable
        ) {
            if(entities.isEmpty()) return "<div>Nothing found...</div>";

            boolean appendRelations = settingsTable.get("appendRelations").equals("true");

            CRUDBuilder f = this;
            int rowCount = entities.values().stream().findFirst().get().size();
            String indexAttribute = entities.keySet().stream().filter(k->k.equals("id")).findFirst().get();
            if(indexAttribute.isBlank()) indexAttribute = entities.keySet().stream().filter(k->k.contains("id")).findFirst().get();
            for( int i=0; i < rowCount; i++ )
            {
                int inner = i;
                Map<String, String> currentEntity = new TreeMap<>(entities).entrySet().stream().collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> (entry.getValue().get(inner)!=null)? entry.getValue().get(inner).toString() : ""
                        )
                );

                String entityID = entities.get(indexAttribute).get(i).toString().equals("")?"new":entities.get(indexAttribute).get(i).toString();
                String rowID = tableName+"_"+entityID+((appendRelations)?"":"_related");
                String entityShadow = (appendRelations)?"EntityShadow":"EntityShadowInset";
                f.$("<div id=\""+rowID+"\" class=\"EntityWrapper "+entityShadow+" row\">");
                String colSizes = "col-sm-12 col-md-12 col-lg-12";
                if(!onclickGenerators.isEmpty()){
                    f.$("<div id=\""+rowID+"_buttons\" class=\"EntityButtons "+colSizes+" ml-auto\">"); // ml-auto := float right for col classes...
                    f.$(
                            "<div style=\"float:right;\">" +
                                    "<span style=\"padding:0.25em;\">" +
                                    tableName.replace("_", " ")+
                                    "</span>" +
                            "</div>"
                    );
                    if( onclickGenerators.containsKey("close") ) {
                        f.$(
                                "<div style=\"float:right;\">" +
                                        "<button style=\"padding:0.25em;background-color:lightblue;\" onclick=\"" +
                                        onclickGenerators.get("close").apply(currentEntity)+
                                        "\">" +
                                        "CLOSE" +
                                        "</button>" +
                                        "</div>"
                        );
                    }
                    if ( onclickGenerators.containsKey("save") ) {
                        f.$(
                                "<div style=\"float:right;\">" +
                                        "<button " +
                                        "style=\"padding:0.25em;background-color:lightgreen;\" " +
                                        "onclick=\"" +
                                        onclickGenerators.get("save").apply(currentEntity)+
                                        "\"" +
                                        ">" +
                                        "SAVE" +
                                        "</button>" +
                                        "</div>"
                        );
                    }
                    if ( onclickGenerators.containsKey("delete") ) {
                        f.$(
                                "<div style=\"float:right;\">" +
                                        "<button style=\"padding:0.25em;background-color:salmon;\" onclick=\"" +
                                        onclickGenerators.get("delete").apply(currentEntity)+
                                        "\">" +
                                        "DELETE" +
                                        "</button>" +
                                        "</div>"
                        );
                    }
                    f.$("</div>");
                }

                StringBuilder contentBuilder = new StringBuilder();
                FrontendConsumer contentConsumer = new FrontendConsumer() {
                    public FrontendConsumer $(Object o) {
                        contentBuilder.append((o==null)?"":o.toString());
                        return this;
                    }
                };
                StringBuilder metaBuilder = new StringBuilder();
                FrontendConsumer metaConsumer = new FrontendConsumer() {
                    public FrontendConsumer $(Object o) {
                        metaBuilder.append((o==null)?"":o.toString());
                        return this;
                    }
                };
                currentEntity.forEach(
                (k,currentValue) ->
                {
                    FrontendConsumer ic = contentConsumer;
                    if(k.contains("id")||k.equals("created")||k.equals("deleted")){
                        ic = metaConsumer;
                    }
                    //--- Form variables:
                    String lowerKey = k.toLowerCase();
                    String bootstrapClasses =
                            (lowerKey.contains("id"))
                                    ?(lowerKey.equals("id"))?"col-sm-4 col-md-3 col-lg-2":"col-sm-5 col-md-4 col-lg-3"
                                    : (lowerKey.contains("value")||lowerKey.contains("content"))
                                    ?"col-sm-12 col-md-12 col-lg-12"
                                    :(lowerKey.contains("deleted")||lowerKey.contains("created"))
                                    ?"col-sm-12 col-md-4 col-lg-4"
                                    :"col-sm-12 col-md-6 col-lg-4";
                    String attribute = k.toLowerCase().replace(" ","_");
                    String attributeID = tableName+"_"+entityID+"_"+attribute;
                    //---
                    ic.$("<div class=\""+bootstrapClasses+"\">");
                    ic.$("<div class=\"AttributeWrapper\">");
                    ic.$("<span                           " +
                         "   value=\"0\"                  " + // Counts onInput events to trigger saving
                         "   id=\""+attributeID+"_span\"       " +
                         ">                               "
                    ).$( _snakeToTitle(k) ).$(
                            "</span>" +
                                    "<"+((lowerKey.contains("value")||lowerKey.contains("content"))?"textarea":"input") +
                                    "      id=\""+attributeID+"\" " +
                                    "      class=\""+_snakeToClass(tableName+"_"+attribute)+"\"     " +
                                    "      name=\""+attribute+"\"                       " +
                                    ((lowerKey.contains("value")||lowerKey.contains("content"))?"":"value=\""+currentValue+"\"") +
                                    "      oninput=\"noteOnInputFor('"+attribute+"','"+tableName+"','"+entityID+"')\"                                           " +
                                    ">"+((lowerKey.contains("value")||lowerKey.contains("content"))?currentValue+"</textarea>":"")
                    );
                    ic.$("</div>");
                    ic.$("</div>");
                });
                f.tabsOf(
                        List.of("Content", "Machina"),
                        tab ->
                        {
                            if(tab.equals("Content")) f.$(contentBuilder.toString());
                            else f.$(metaBuilder);
                        },
                        "default"
                );

                // Relation tables
                if(appendRelations && !tableName.contains("relations") && !entityID.equals("new")) {
                    f.buildRelationForms(tableName, entityID);
                }
                f.$("</div>");
            }
            return f.toString();
        }


        private CRUDBuilder _forRelationKeys(
                Map<String,String> relationTable,
                String innerTableName,
                RelationKeyConsumer consumer
        ) {
            List<String> foreignKeys = new ArrayList<>(relationTable.keySet());
            for( String innerKey : foreignKeys ) { // := from one to...
                for( String outerKey : foreignKeys ) { // := many !
                    if(
                            ! innerKey.equals(outerKey) && // The inner key must reference the current table type!
                                    relationTable.get(innerKey).contains("REFERENCES "+innerTableName)
                    ) {
                        String outerTable = relationTable.get(outerKey).split("REFERENCES ")[1].split(" ")[0].trim();
                        consumer.accept(innerKey, outerKey, outerTable);
                    }
                }
            }
            return this;
        }

        private CRUDBuilder buildRelationForms (
                String innerTableName,
                String id
        ) {
            Map<String, Map<String,String>> relationTables = __findRelationTablesOf(innerTableName, _tables, s->true);

            // Building tabs for each relation TABLE ! :
            tabsOf(
                    new ArrayList<>(relationTables.keySet()),
                    relationTableName -> // Example :  tail_tag_relations
                    {
                        List<String> foreignKeys = new ArrayList<>(relationTables.get(relationTableName).keySet());

                        /*
                            The inner foreign key will be queried to be equivalent to
                            the id of the table in the 'tableName' variable!
                        */
                        Map<String, List<String>> hasManyRelations = new TreeMap<>();
                        _forRelationKeys(
                                relationTables.get(relationTableName),
                                innerTableName,
                                (innerKey, outerKey, outerTable)->{
                                    String outerText = outerKey.replace("_id", "");
                                    hasManyRelations.put(
                                            "found_"+__toPlural(outerText),
                                            List.of(innerKey, outerKey) // one : many
                                    );
                                }
                        );


                        //for( String innerKey : foreignKeys ) { // := from one to...
                        //    for( String outerKey : foreignKeys ) { // := many !
                        //        if(
                        //                ! innerKey.equals(outerKey) && // The inner key must reference the current table type!
                        //                        relationTables.get(relationTableName).get(innerKey).contains("REFERENCES "+innerTableName)
                        //        ) {
                        //            String outerText = outerKey.replace("_id", "");
                        //            hasManyRelations.put(
                        //                    "found_"+__toPlural(outerText),
                        //                    List.of(innerKey, outerKey) // one : many
                        //            );
                        //        }
                        //    }
                        //}

                        // Building tabs for each relation type within a given table... :
                        tabsOf(
                                new ArrayList<>( hasManyRelations.keySet() ),
                                relationType ->
                                {
                                    String innerKey = hasManyRelations.get(relationType).get(0);
                                    String outerKey = hasManyRelations.get(relationType).get(1);
                                    String outerTableName = relationTables.get(relationTableName).get(outerKey).split("REFERENCES ")[1].split(" ")[0];
                                    Map<String, List<Object>> relationResult = _query(
                                            "SELECT * FROM "+relationTableName+" WHERE "+innerKey+" = "+id
                                    );
                                    int numberOfFound = relationResult.values().stream().findFirst().get().size();

                                    // This id will be targeted by the "new button" generated at the end of the loop below:
                                    $("<div id=\""+relationTableName+"_and_"+outerTableName+"_"+id+"_joined_on_"+outerKey+"_result"+"\" class=\"col-sm-12 col-md-12 col-lg-12\">");
                                    $("<div class=\"\">");// The 'row' class is deliberately left out here!
                                    // -> creates a nice padding for some reason! :)
                                    for ( int i = 0; i < numberOfFound; i++ )
                                    {
                                        int index = i;
                                        assert numberOfFound == relationResult.values().stream().findFirst().get().size();
                                        Map<String, List<Object>> currentRelationEntity =
                                                new TreeMap<>(relationResult).entrySet().stream().collect(
                                                                Collectors.toMap(
                                                                        Map.Entry::getKey,
                                                                        entry -> {
                                                                            if(entry.getValue().get(index)!=null) {
                                                                                entry.setValue(List.of(entry.getValue().get(index)));
                                                                            } else entry.setValue(List.of(""));
                                                                            return entry.getValue();
                                                                        }
                                                                )
                                                        );
                                        // There should never be more than one current relation entity :
                                        assert currentRelationEntity.get(outerKey).size()==1;

                                        Map<String, List<Object>> currentOuterEntity = _query(
                                                "SELECT * FROM "+outerTableName+ " WHERE id = "+currentRelationEntity.get(outerKey).get(0)
                                        );
                                        // There should not be more than one current
                                        assert currentOuterEntity.get("id").size() == 1;

                                        generateRelationEntity( // Will append the relation entity and the outer entity!
                                                relationTableName,
                                                currentRelationEntity,
                                                outerTableName,
                                                currentOuterEntity,
                                                outerKey
                                        );

                                    } // :=  Entry loop end!
                                    $("</div>");
                                    $("</div>");
                                    $("<div class=\"col-sm-12 col-md-12 col-lg-12\">");
                                    generateNewButton (
                                            List.of (
                                                    relationTableName,
                                                    outerTableName
                                            ),
                                            entities -> {
                                                entities.get(0).put( innerKey, List.of(id) ); // Current inner id !
                                                generateRelationEntity (
                                                    relationTableName,
                                                    entities.get(0),
                                                    outerTableName,
                                                    entities.get(1),
                                                    outerKey
                                                );
                                            },
                                            id, outerKey
                                    );
                                    $("</div>");

                                }
                        );
                    },
                    "noRow"
            );
            return this;
        }

        public String _newUID(){
            String uid = Long.toHexString(System.currentTimeMillis())+Integer.toHexString(Math.abs(new Random().nextInt()));
            return uid + Integer.toHexString(Math.abs(new Random().nextInt())).substring(0, 20-uid.length());
        }

        public CRUDBuilder generateRelationEntity (
                String relationTableName,
                Map<String, List<Object>> currentRelationEntity,
                String outerTableName,
                Map<String, List<Object>> currentOuterEntity,
                String outerKey
        ) {
            String uid = _newUID();
            $("<div style=\"border: 1px solid grey; margin-bottom:1em;\">");
            String relationUID = relationTableName + "_" + uid;
            $("<div " +
                    "id=\"" + relationUID + "\" " +
                    "class=\"col-sm-12 col-md-12 col-lg-12\" " +
                    "style=\"margin-bottom:0.5em;\"" +
                    ">"
            );
            //Function<Map<String, String>, String> entityID = e ->
            //    (currentRelationEntity.get("id").get(0).equals(""))?"new":currentRelationEntity.get("id").get(0).toString();
            //Function<Map<String, String>, String> rowID = e -> relationTableName+"_"+entityID.apply(e)+"_related";

            entitiesToForm(relationTableName, currentRelationEntity, Map.of(), Map.of("appendRelations","false"));
            $("</div>");
            String outerUID = outerTableName + "_" + uid;
            $("<div " +
                    "id=\"" + outerUID + "\" " +
                    "class=\"col-sm-12 col-md-12 col-lg-12\" " +
                    "style=\"background-color:#fff;\"" +
                    ">"
            );
            //Function<Map<String, String>, String> outerEntityID = e -> (e.get("id").equals(""))?"new":e.get("id");
            //Function<Map<String, String>, String> outerRowID = e -> outerTableName+"_"+outerEntityID.apply(e)+"_related";

            String relationIDClass = _snakeToClass(relationTableName+"_id");
            String outerIDClass = _snakeToClass(outerTableName+"_id");
            entitiesToForm(
                    outerTableName,
                    currentOuterEntity,
                    Map.of(
                            "close", e->
                                    "$('#"+outerUID+ "').find('.EntityWrapper').replaceWith('');" +
                                    "$('#"+relationUID+"').find('.EntityWrapper').replaceWith('');"
                            ,
                            "save", e->// vvv ! dirty but powerful little hack :D ! vvv (loaded buttons will not be know about the relation table...)
                                    "let buttons = $('#"+outerUID+"').find('.EntityButtons').html();" +
                                    "let relationID = $('#"+relationUID+"').find('."+relationIDClass+"').val();" +
                                    "let outerID = $('#"+outerUID+"').find('."+outerIDClass+"').val();" +
                                    "loadSavedForRelation(                                                  " +
                                            "  '"+relationTableName+"',                                     " +
                                            "  (relationID==='')?'new':relationID,                          " +
                                            //-------------------------------------------------------------------------
                                            "  '"+outerTableName+"',                                        " +
                                            "  (outerID==='')?'new':outerID,                                " +
                                            "  '"+outerKey+"',                                              " +
                                            "  '"+outerIDClass+"',                " +
                                            "  '"+uid + "',                                                 " +
                                            "  function(){                                                  " +
                                            "       $('#"+outerUID+"').find('.EntityButtons').html(buttons);" +
                                            "  }" +
                                    ");",
                            "delete", e->
                                    "let relationID = $('#"+relationUID+"').find('."+relationIDClass+"').val();" +
                                    "let outerID = $('#"+outerUID+"').find('."+outerIDClass+"').val();" +
                                    "deleteEntity( '"+outerTableName+"', (outerID==='')?'new':outerID );"+
                                    "deleteEntity( '"+relationTableName+"', (relationID==='')?'new':relationID );" +
                                    "$('#"+outerUID+ "').find('.EntityWrapper').replaceWith('');" +
                                    "$('#"+relationUID+"').find('.EntityWrapper').replaceWith('');"
                    ),
                    Map.of("appendRelations","false")
            );
            $("</div>");
            $("</div>");
            return this;
        }

        @Override
        public String toString(){
            return _builder.toString();
        }
    }


}
