package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;
import comp.imp.Url;
import org.sqlite.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CRUD extends AbstractDatabaseConnection implements IPlugin
{
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

        private void tabsOf(List<String> tabNames, Consumer<String> lambda)
        {
            Function<String, String> asClass = s->{
                s = s.replace(" ", "_");
                s = s.replaceFirst("_[a-z]", String.valueOf(Character.toUpperCase(s.charAt(s.indexOf("_") + 1))));
                return s;
            };
            Function<String, String> asText = s -> {
                s = s.substring(0, 1).toUpperCase() + s.substring(1);
                return s.replace("_", " ");
            };
            $("<div class=\"tabWrapper col-sm-12 col-md-12 col-lg-12\">\n");
            $("<div class=\"tabHead\">\n");
            String selected = "selected";
            for(String type : tabNames) {
                $("<button onclick=\"switchTab(event, '."+asClass.apply(type)+"Tab')\" class=\""+selected+"\">"+asText.apply(type)+"</button>\n");
                selected = "";
            }
            $(
                "</div>\n"+
                "<div class=\"tabBody\">\n"
            );
            String displayNone = "";
            for( String type : tabNames ) {
                $("<div class=\""+asClass.apply(type)+"Tab row\" style=\""+displayNone+"\">\n");
                lambda.accept(type);
                $("</div>\n");
                displayNone = "display:none";
            }
            $(
                    "</div>\n"+
                    "</div>\n"
            );
        }

        @Override
        public String toString(){
            return _builder.toString();
        }

    }


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
    public CRUD(String url)
    {
        super(url, "", "");
        try { _createAndOrConnectToDatabase(); } catch (Exception e) { e.printStackTrace(); }
        _executeFile("tailworld_bootstrap.sql");
        _executeFile("tailworld_setup.sql");
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

    private void _save(IRequest req,  IResponse response)
    {
        response.setContent("text/html");
        String tableName = req.getUrl().getFileName();
        Map<String, String> paramTable = req.getUrl().getParameter();

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

        String result = __entitiesToForm( tableName, map, tables, true);
        if(result.isBlank())  response.setContent("Nothing found!");
        else response.setContent(result);
    }


    private String __entitiesToForm(
            String tableName,
            Map<String, List<Object>> entities,
            Map<String, List<String>> tables,
            boolean appendRelations
    ){
        if(entities.isEmpty()) return "<div>Nothing found...</div>";

        CRUDBuilder f = new CRUDBuilder(tables);
        int rowCount = entities.values().stream().findFirst().get().size();
        String indexAttribute = entities.keySet().stream().filter(k->k.equals("id")).findFirst().get();
        if(indexAttribute.isBlank()) indexAttribute = entities.keySet().stream().filter(k->k.contains("id")).findFirst().get();
        for(int i=0; i<rowCount; i++) {
            int inner = i;
            String entityID = entities.get(indexAttribute).get(i).toString().equals("")?"new":entities.get(indexAttribute).get(i).toString();
            String rowID = tableName+"_"+entityID;
            f.$("<div id=\""+rowID+"\" class=\"EntityWrapper row\">");
            f.$(
                    "<div class=\"col-sm-2 col-md-2 col-lg-2 ml-auto\">" + // ml-auto := float right for col classes...
                    "<button style=\"width:100%;\" onclick=\"loadSavedForEntity( '"+tableName+"', '"+entityID+"' )\">" +
                    "SAVE" +
                    "</button>" +
                    "</div>" +
                    "<div class=\"col-sm-2 col-md-2 col-lg-2 ml-auto\">" +
                    "<button style=\"width:100%;\" onclick=\"deleteEntity( '"+tableName+"', '"+entityID+"' )\">" +
                    "DELETE" +
                    "</button>" +
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

            entities.forEach( (k,v) ->
            {
                FrontendConsumer ic = contentConsumer;
                if(k.contains("id")||k.equals("created")||k.equals("deleted")){
                    ic = metaConsumer;
                }
                //--- Form variables:
                String lowerKey = k.toLowerCase();
                String bootstrapClasses =
                        (lowerKey.contains("id"))
                                ?(lowerKey.equals("id"))?"col-sm-2 col-md-1 col-lg-1":"col-sm-2 col-md-2 col-lg-2"
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
                ic.$(
                        "<span                          " +
                        "   value=\"0\"                 " + // Counts onInput events to trigger persisting
                        "   id=\""+attributeID+"\"      " +
                        ">                              "
                ).$(
                        k
                ).$(
                        "</span>" +
                        "<"+((lowerKey.contains("value")||lowerKey.contains("content"))?"textarea":"input") +
                        "      name=\""+attribute+"\"                       " +
                                ((lowerKey.contains("value")||lowerKey.contains("content"))?"":"value=\""+v.get(inner)+"\"") +
                        "      oninput=\"noteOnInputFor('"+attribute+"','"+tableName+"','"+entityID+"')\"                                           " +
                        ">"+((lowerKey.contains("value")||lowerKey.contains("content"))?v.get(inner)+"</textarea>":"")
                );
                ic.$("</div>");
                ic.$("</div>");
            });

            f.tabsOf(
                    List.of("Content", "Machina"),
                    tab->{
                        if(tab.equals("Content")) f.$(contentBuilder.toString());
                        else f.$(metaBuilder);
                    }
            );

            // Relation tables
            if(appendRelations){
                //f.$(__buildRelationForms(tableName, entityID, tables));
                __buildRelationForms(tableName, entityID, tables);
            }
            f.$("</div>");
        }
        return f.toString();
    }

    private String __buildRelationForms(
            String innerTableName,
            String id, Map<String,
            List<String>> tables
    ) {
        if(true) return null;
        CRUDBuilder f = new CRUDBuilder(tables);
        Map<String, Map<String,String>> relationTables = __findRelationTablesOf(innerTableName, tables, s->true);//Todo make type be found....

        f.tabsOf(
                new ArrayList<>(relationTables.keySet()),
                relationTable -> // Example :  tail_tag_relations
                {
                    List<String> foreignKeys = new ArrayList<>(relationTables.get(relationTable).keySet());
                    Map<String, List<String>> fromToMap = new TreeMap<>();
                    /*
                        The inner foreign key will be queried to be equivalent to
                        the id of the table in the 'tableName' variable!
                     */
                    for( String innerKey : foreignKeys ) { // := from one to...
                        for( String outerKey : foreignKeys ) { // := many !
                            if(
                                    ! innerKey.equals(outerKey) && // The inner key must reference the current table type!
                                            relationTables.get(relationTable).get(innerKey).contains("REFERENCES "+innerTableName)
                            ) {

                                String innerText = innerKey.replace("_id", "");
                                String outerText = outerKey.replace("_id", "");
                                fromToMap.put(
                                        "one_"+innerText+"_has_many_"+outerText,
                                        List.of(innerKey, outerKey)
                                );
                            }
                        }
                    }
                    f.tabsOf(
                            new ArrayList<>(fromToMap.keySet()),
                            relationName -> {
                                String innerKey = fromToMap.get(relationName).get(0);
                                String outerKey = fromToMap.get(relationName).get(1);
                                String outerTableName = relationTables.get(relationTable).get(outerKey).split("REFERENCES ")[1].split(" ")[0];
                                Map<String, List<Object>> relationResult = _query(
                                        "SELECT * FROM "+relationTable+" WHERE "+innerKey+" = "+id
                                );
                                int numberOfFound = relationResult.values().stream().findFirst().get().size();
                                for ( int i = 0; i < numberOfFound; i++ ) {
                                    int index = i;
                                    Map<String, List<Object>> currentRelationEntity =
                                            relationResult
                                                    .entrySet()
                                                    .stream()
                                                    .collect(Collectors.toMap(
                                                            entry -> entry.getKey(),
                                                            entry -> {
                                                                entry.setValue(List.of(entry.getValue().get(index)));
                                                                return entry.getValue();
                                                            }
                                                    ));
                                    // There should never be more than one current relation entity :
                                    assert currentRelationEntity.get(outerKey).size()==1;
                                    __entitiesToForm(
                                            relationTable,
                                            currentRelationEntity,
                                            tables,
                                            false
                                    );
                                    Map<String, List<Object>> currentOuterEntity = _query(
                                            "SELECT * FROM "+outerTableName+
                                            " WHERE id = "+currentRelationEntity.get(outerKey).get(0)
                                    );
                                    __entitiesToForm(
                                            outerTableName,
                                            currentOuterEntity,
                                            tables,
                                            false
                                    );
                                }

                            }
                    );
                }
        );
        return null;
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
        StringBuilder form = new StringBuilder( fileData );
        FrontendConsumer f = new FrontendConsumer() {
            public FrontendConsumer $( Object o ) {
                form.append(o.toString());
                return this;
            }
        };
        String today = new java.sql.Date( System.currentTimeMillis() ).toString();
        Map<String, List<String>> tables = _tablesSpace();
        tables.forEach(
            ( table, columns ) ->
            {
                Map<String, List<Object>> templateEntity = new HashMap<>();
                for(String c : columns) templateEntity.put(c.split(" ")[0], List.of((c.split(" ")[0].equals("created"))?today:""));
                f.$("<div class = \"mainContentWrapper\">");
                    f.$("<div class = container-fluid>");
                        f.$("<div id=\"" + table + "_search\" class=\"SearchWrapper\">");//row?
                            f.$("<div class=\"SearchHead col-sm-12 col-md-12 col-lg-12\"><label>").$(table).$(" - search :</label>");
                            f.$("<button onclick=\"loadFoundForEntity('").$(table).$("')\">find!</button>");
                            f.$("</div>");
                            for(String c : columns) f.$("<input name=\"").$(c.split(" ")[0]).$("\" placeholder=\"").$(c).$("\"></input>");
                        f.$("</div>");
                        //f.$("<div class=\"row\">"); //This is not working? why?
                            f.$("<div class=\"col-sm-12 col-md-12 col-lg-12\">");
                                f.$("<div id=\"").$(table).$("_result\" class=\"SearchResult\"></div>");
                            f.$("</div>");
                        //f.$("</div>");
                        f.$("<script>");
                        f.$(" function new_"+table+"() {");
                            f.$("$('#").$(table).$("_result').append(`");
                            f.$(__entitiesToForm(table, templateEntity, tables, false));
                            f.$("`);");
                        f.$(" }");
                        f.$("</script>\n");
                        f.$("<button onclick=\"new_"+table+"()\">");
                            f.$("NEW\n");
                        f.$("</button>\n");
                    f.$("</div>");
                f.$("</div>");
            }
        );
        response.setContent(form.toString());
    }




}
