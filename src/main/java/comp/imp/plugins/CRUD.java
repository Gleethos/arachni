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
        super("jdbc:sqlite:C:/sqlite/db/TailDB", "", "");
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
        try { _createAndOrConnectToDatabase(); } catch (Exception e) { e.printStackTrace(); }
        List<String> filesFound = new ArrayList<>();
        File folder = new File("db/"+worldfolder);
        for (final File f : folder.listFiles()) {
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

    private void _save(IRequest req,  IResponse response)
    {
        response.setContent("text/html");
        String tableName = req.getUrl().getFileName();
        Map<String, String> paramTable = req.getUrl().getParameter();
        boolean appendRelations = true;
        if(paramTable.containsKey("appendRelations")) {
            appendRelations = paramTable.get("appendRelations").toLowerCase().equals("true")?true:false;
            paramTable.remove("appendRelations"); // Should be remove because other parameters will be viewed as attributes!
        }

        if(req.getMethod().equals("POST")) paramTable.putAll(new Url(req.getContentString()).getParameter());
        Map<String, List<String>> tables = _tablesSpace();
        Map<String, List<String>> attributes = __attributesTableOf(tables.get(tableName));

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
        if(!appendRelations) req.getUrl().getParameter().put("appendRelations", "false");
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
        response.setContent("text/html");
        ArrayList<String> cols = new ArrayList<>();
        String tableName = req.getUrl().getFileName();
        
        Map<String, String> paramTable = req.getUrl().getParameter();
        boolean appendRelations = true;
        if(paramTable.containsKey("appendRelations")) {
            appendRelations = paramTable.get("appendRelations").toLowerCase().equals("true")?true:false;
            paramTable.remove("appendRelations");
        }
        boolean quickSearch = false;
        if(paramTable.containsKey("searchQuickly")) {
            quickSearch = paramTable.get("searchQuickly").toLowerCase().equals("true")?true:false;
            paramTable.remove("searchQuickly");
        }
        if(req.getMethod().equals("POST") && !paramTable.containsKey("id")) paramTable.putAll(new Url(req.getContentString()).getParameter());

        Map<String, List<String>> tables = _tablesSpace();
        Map<String, List<String>> attributes = __attributesTableOf(tables.get(tableName));

        List<String> columns = attributes.keySet().stream().collect(Collectors.toList());

        for(String column : columns) {
            if(paramTable.containsKey(column) && !paramTable.get(column).equals("")) cols.add(column);
        }
        List<Object> values = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        if ( !cols.isEmpty() ) sql.append(" WHERE ");
        for ( int i=0; i<cols.size(); i++ ) {
            String type = attributes.get(cols.get(i)).get(0);
            if ( type.toLowerCase().contains("text") || type.toLowerCase().contains("char") ) {
                sql.append(cols.get(i)).append(" LIKE ? ");
                values.add("%"+paramTable.get(cols.get(i))+"%");
            } else {
                sql.append(cols.get(i)).append(" = ? ");
                values.add(paramTable.get(cols.get(i)));
            }
            if ( i < cols.size()-1 ) sql.append("OR ");
        }
        Map<String, List<Object>> map = _query(sql.toString(), values);
        String result = "";
        if(quickSearch)
        {
            String keyAttribute = "id";
            String[] preferenceList = new String[]{ "description", "name", "title" };
            int matchId = -1;
            for(String currentAttribute : new ArrayList<>(attributes.keySet())) {
                for (int i=0; i<preferenceList.length; i++) {
                    if ( currentAttribute.equals(preferenceList[i]) && i>matchId ) {
                        keyAttribute = currentAttribute;
                        matchId = i;
                    }
                }
            }
            CRUDBuilder b = new CRUDBuilder(tables);
            b.$("<div id=\""+tableName+"_quick_search_result\" class=\"row\">")
                .$("<div class=\"col-sm-12 col-md-12 col-lg-12\">");
            b.$("<span><h3> "+b._snakeToTitle(keyAttribute)+"(s) :</h3></span>");
            b.$("</div>");

            int numberOfFound = map.get("id").size();
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
            result = new CRUDBuilder(tables).entitiesToForm( tableName, map, appendRelations ).toString();
        }
        if(result.isBlank())  response.setContent("Nothing found!");
        else response.setContent(result);
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

    private void _view( IRequest req, IResponse response )
    {
        response.setContentType("text/html");
        String fileData = new util().readResource("CRUD/default.html");// send HTTP Headers
        Map<String, List<String>> tables = _tablesSpace();
        CRUDBuilder f = new CRUDBuilder(tables);
        f.$(fileData);
        f.tabsOf(
                new ArrayList<>(tables.keySet()),
                table -> {
                    f.$("<div class = \"mainContentWrapper col-sm-12 col-md-12 col-lg-12\">")
                        .$("<div class = container-fluid>")
                            .$("<div class=\"SearchWrapper row\">")//row?
                                .$("<div class=\"col-sm-12 col-md-12 col-lg-12\">")
                                .$("<h1>").$(f._snakeToTitle(table)).$("</h1>")
                                .$("</div>")
                                .$("<div class=\"col-sm-12 col-md-6 col-lg-6\">")
                                    .$("<label>Total stored: "+_query("SELECT COUNT(*) FROM "+table).get("COUNT(*)").get(0)+"</label>")
                                .$("</div>")
                                .$("<div class=\"col-sm-12 col-md-6 col-lg-6\">")
                                    .$("<button onclick=\"$('#"+table+"_result').html('');\">CLEAR</button>")
                                    .$("<button onclick=\"loadFoundForEntity('").$(table).$("')\">SEARCH</button>")
                                    .$("<button onclick=\"loadQuickSearchForEntity('").$(table).$("')\">QUICK SEARCH</button>")
                                .$("</div>");
                                List<String> columns = tables.get(table);
                                f.tabsOf(
                                        Map.of(
                                                "quick",
                                                searchType -> {
                                                    f.$("<div class=\"SearchHead col-sm-12 col-md-12 col-lg-12\">");
                                                    f.$("<input style=\"width:100%;\"")
                                                            .$("name=\"search\" ")
                                                            .$("placeholder=\"anything\"")
                                                            .$("id=\""+table+"_quick_search_input"+"\"")
                                                            .$("oninput=\"");
                                                    for(String c : columns){
                                                        String attributeName = c.split(" ")[0];
                                                        f.$("$('#").$(table+"_"+attributeName+"_search_input')")
                                                                .$(".val($('#"+table+"_quick_search_input').val());\n"
                                                        );
                                                    }
                                                            f.$("loadQuickSearchForEntity('"+table+"');");
                                                            f.$("\"")
                                                            .$(">");
                                                    f.$("</div>");
                                                },
                                                "specific",
                                                searchType -> {
                                                    f.$("<div id=\"" + table + "_search\" class=\"SearchHead col-sm-12 col-md-12 col-lg-12\">");
                                                    for(String c : columns){
                                                        String attributeName = c.split(" ")[0];
                                                        f.$("<input ")
                                                                .$("name=\"").$(attributeName).$("\" ")
                                                                .$("placeholder=\"").$(c).$("\"")
                                                                .$("id=\"").$(table+"_"+attributeName+"_search_input").$("\"")
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
                                    .$("<div id=\"").$(table).$("_result\" class=\"SearchResult\"></div>")
                                .$("</div>")
                            .$("</div>").generateNewButton( table )
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
            String colSizes = (tabType.contains("compacted"))?"col-sm-12 col-md-10 col-lg-10":"col-sm-12 col-md-12 col-lg-12";
            colSizes = (tabType.contains("root"))?"":colSizes;
            String additionalHeadStyles = (tabType.contains("root"))?"font-size:1em;":"";
            $("<div class=\"tabWrapper "+colSizes+"\">\n<div class=\"tabHead\" style=\""+additionalHeadStyles+"\">\n");
            String selected = "selected";
            for(String type : tabNames) {
                $("<button onclick=\"switchTab(event, '."+_snakeToClass(type)+"Tab')\" class=\""+selected+"\">"+_snakeToText(type)+"</button>\n");
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

        private void generateNewButton( String table, boolean appendRelations ){
            generateNewButton(
                    List.of(table),
                    e->entitiesToForm( table, e.get(0), appendRelations ),
                    ""
            );
        }

        private CRUDBuilder generateNewButton( String table ){
            return generateNewButton(
                    List.of(table),
                    e -> entitiesToForm(table, e.get(0), false),
                    ""
            );
        }

        private CRUDBuilder generateNewButton (
                List<String> tableNames,
                Consumer< List<Map<String, List<Object>>>> templateLambda,
                String id
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
            table = (id.isBlank())?table:table+"_"+id;
            $("<script>");
            $(" function new_"+table+"() {");
            $("$('#").$(table).$("_result').append(`");
            templateLambda.accept(templates);
            $("`);");
            $(" }");
            $("</script>\n");
            $("<button onclick=\"new_"+table+"()\">");
            $("NEW\n");
            $("</button>\n");
            // TODO : Make button creation possible for relation...
            return this;
        }

        private String entitiesToForm(
                String tableName,
                Map<String, List<Object>> entities,
                boolean appendRelations
        ) {
            if(entities.isEmpty()) return "<div>Nothing found...</div>";

            int importantFieldsNumber = entities
                    .keySet()
                    .stream()
                    .filter(k->!(k.contains("id") || k.equals("created") || k.equals("deleted")))
                    .collect(Collectors.toList())
                    .size();
            boolean compacted = importantFieldsNumber < 2;

            CRUDBuilder f = this;
            int rowCount = entities.values().stream().findFirst().get().size();
            String indexAttribute = entities.keySet().stream().filter(k->k.equals("id")).findFirst().get();
            if(indexAttribute.isBlank()) indexAttribute = entities.keySet().stream().filter(k->k.contains("id")).findFirst().get();
            for(int i=0; i<rowCount; i++) {
                int inner = i;
                String entityID = entities.get(indexAttribute).get(i).toString().equals("")?"new":entities.get(indexAttribute).get(i).toString();
                String rowID = tableName+"_"+entityID;
                String entityShadow = (appendRelations)?"EntityShadow":"EntityShadowInset";
                f.$("<div id=\""+rowID+"\" class=\"EntityWrapper "+entityShadow+" row\">");
                String colSizes = (compacted)?"col-sm-12 col-md-2 col-lg-1":"col-sm-12 col-md-12 col-lg-12";
                f.$(
                        "<div class=\""+colSizes+" ml-auto\">" + // ml-auto := float right for col classes...
                                "<div style=\"float:right;\">" +
                                "<span style=\"padding:0.25em;\">" +
                                tableName.replace("_", " ")+
                                "</span>" +
                                "</div>" +
                                "<div style=\"float:right;\">" +
                                "<button style=\"padding:0.25em;\" onclick=\"$( '#"+rowID+"' ).replaceWith('');\">" +
                                "CLOSE" +
                                "</button>" +
                                "</div>" +
                                "<div style=\"float:right;\">" +
                                "<button " +
                                "style=\"padding:0.25em;\" " +
                                "onclick=\"loadSavedForEntity( "+
                                    "'"+tableName+"',   " +
                                    "'"+entityID+"',    "+
                                    ((appendRelations)?"''":"'?appendRelations=false'")+
                                ")\"" +
                                ">" +
                                "SAVE" +
                                "</button>" +
                                "</div>" +
                                "<div style=\"float:right;\">" +
                                "<button style=\"padding:0.25em;\" onclick=\"deleteEntity( '"+tableName+"', '"+entityID+"' )\">" +
                                "DELETE" +
                                "</button>" +
                                "</div>" +
                                "</div>"
                );

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
                entities.forEach(
                (k,v) ->
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
                    if(compacted) {
                        bootstrapClasses.replace("-2", "-3");
                    }
                    String attribute = k.toLowerCase().replace(" ","_");
                    String attributeID = tableName+"_"+entityID+"_"+attribute;
                    String currentValue = (v.get(inner)==null)?"":v.get(inner).toString();
                    //---
                    ic.$("<div class=\""+bootstrapClasses+"\">");
                    ic.$("<div class=\"AttributeWrapper\">");
                    ic.$("<span                          " +
                         "   value=\"0\"                 " + // Counts onInput events to trigger persisting
                         "   id=\""+attributeID+"\"      " +
                         ">                              "
                    ).$( _snakeToTitle(k) ).$(
                            "</span>" +
                                    "<"+((lowerKey.contains("value")||lowerKey.contains("content"))?"textarea":"input") +
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
                        (compacted)?"compacted":"default"
                );

                // Relation tables
                if(appendRelations && !tableName.contains("relations")) f.buildRelationForms(tableName, entityID);
                f.$("</div>");
            }
            return f.toString();
        }

        private CRUDBuilder buildRelationForms(
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

                        Map<String, List<String>> hasManyRelations = new TreeMap<>();
                        /*
                            The inner foreign key will be queried to be equivalent to
                            the id of the table in the 'tableName' variable!
                        */
                        for( String innerKey : foreignKeys ) { // := from one to...
                            for( String outerKey : foreignKeys ) { // := many !
                                if(
                                        ! innerKey.equals(outerKey) && // The inner key must reference the current table type!
                                                relationTables.get(relationTableName).get(innerKey).contains("REFERENCES "+innerTableName)
                                ) {
                                    String innerText = innerKey.replace("_id", "");
                                    String outerText = outerKey.replace("_id", "");
                                    hasManyRelations.put(
                                            innerText+"_has_many_"+outerText,
                                            List.of(innerKey, outerKey) // one : many
                                    );
                                }
                            }
                        }

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
                                    $("<div id=\""+relationTableName+"_and_"+outerTableName+"_"+id+"_result"+"\" class=\"col-sm-12 col-md-12 col-lg-12\">");
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
                                                currentOuterEntity
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
                                                entities.get(0).put(innerKey, List.of(id)); // Current inner id !
                                                generateRelationEntity(
                                                    relationTableName,
                                                    entities.get(0),
                                                    outerTableName,
                                                    entities.get(1)
                                                );
                                            },
                                            id
                                    );
                                    $("</div>");

                                }
                        );
                    },
                    "noRow"
            );
            return this;
        }

        public CRUDBuilder generateRelationEntity (
                String relationTableName,
                Map<String, List<Object>> currentRelationEntity,
                String outerTableName,
                Map<String, List<Object>> currentOuterEntity
        ) {
            $("<div " +
                    "id=\"" + "\" " +
                    "class=\"col-sm-12 col-md-12 col-lg-12\" " +
                    "style=\"margin-bottom:0.5em;\"" +
                    ">"
            );
            entitiesToForm( relationTableName, currentRelationEntity, false );
            $("</div>");

            $("<div " +
                    "id=\"" + "\" " +
                    "class=\"col-sm-12 col-md-12 col-lg-12\" " +
                    "style=\"background-color:#fff; margin-bottom:1em;\"" +
                    ">"
            );
            entitiesToForm( outerTableName, currentOuterEntity, false );
            $("</div>");
            //"$('#input1, #input2').bind(\"focus blur change keyup\", function(){ .... });"
            return this;
        }

        @Override
        public String toString(){
            return _builder.toString();
        }
    }


}
