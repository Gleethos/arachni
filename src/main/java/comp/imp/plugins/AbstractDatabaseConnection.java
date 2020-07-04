package comp.imp.plugins;

import comp.IPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractDatabaseConnection {

    /**
     * Connection settings: URL, User, Password!
     */
    private String _url, _user, _pwd;

    protected Connection _connection;

    AbstractDatabaseConnection(String url, String name, String password){
        _url = url;
        _user = name;
        _pwd = password;
    }

    public String getURL(){
        return _url;
    }

    protected void _setUrl(String url) {
        _url = url;
    }

    /**
     * Connect to a sample database
     */
    protected void _createAndOrConnectToDatabase() throws SQLException
    {
        this._connection = null;
        if(_user.equals("")||_pwd.equals("")){
             _connection = DriverManager.getConnection(_url);
        } else {
            _connection = DriverManager.getConnection(_url, _user, _pwd);
        }
        _connection.setAutoCommit(false);
    }


    /**
     * Closing Connection!
     */
    protected void _close(){
        Connection conn = _connection;
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        _connection=null;
    }

    /**
     * Prints all tables of a connection!
     */
    protected List<String> _listOfAllTables(){
        String sql = "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%';";
        List<String> names = new ArrayList<>();
        _for(sql, null, rs -> {
            try {
                names.add(rs.getString("name"));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        return names;
    }

    protected Map<String, List<String>> _tablesSpace(){
        String sql = "SELECT * FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%';";
        Map<String, List<String>> space = new HashMap<>();
        _for(sql, null, rs -> {
            try {
                String def = rs.getString("sql");
                def = def.split("\\(")[1];
                def = def.split("\\)")[0];
                String[] payload = def.split(",");
                int inset = 0;
                if(payload[payload.length-1].toLowerCase().contains("foreign")) inset = 1;
                String[] attributes = new String[payload.length-inset];
                for(int i=0; i<attributes.length; i++) attributes[i] = payload[i].trim();
                space.put(rs.getString("name"), List.of(attributes));

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        return space;

    }

    private PreparedStatement _newPreparedStatement(String sql, List<Object> values) throws SQLException {
        PreparedStatement pstmt = _connection.prepareStatement(sql);
        if(values!=null) {
            for(int i=0; i<values.size(); i++){
                pstmt.setObject(i+1, values.get(i));
            }
        }
        return pstmt;
    }

    protected void _for(String sql, Consumer<ResultSet> start, Consumer<ResultSet> each){
        _for(sql, null, start, each);
    }

    protected void _for(String sql, List<Object> values, Consumer<ResultSet> start, Consumer<ResultSet> each){
        if (values!=null && !values.isEmpty()){
            try {
                PreparedStatement pstmt = _newPreparedStatement(sql, values);
                try {
                    ResultSet rs = pstmt.executeQuery();// loop through the result set
                    if(start!=null) start.accept(rs);
                    if(each!=null) do each.accept(rs); while(rs.next());
                    rs.close();
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Statement stmt = _connection.createStatement();
                try {
                    ResultSet rs = stmt.executeQuery(sql);// loop through the result set
                    if(start!=null) start.accept(rs);
                    if(each!=null) do each.accept(rs); while(rs.next());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    protected Map<String, List<Object>> _query(String sql) {
        return _query(sql, null);
    }

    protected Map<String, List<Object>> _query(String sql, List<Object> values){

        Map<String, List<Object>> result = new HashMap<>();
        _for(
                sql, values, // <=- Are used to build prepared statement when 'values' is not null!
                rs -> {
                    try {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int columnsNumber = rsmd.getColumnCount();
                        for (int i = 1; i <= columnsNumber; i++) {
                            result.put(rsmd.getColumnName(i), new ArrayList<>());
                        }
                    } catch (Exception e){e.printStackTrace();}
                },
                rs -> {
                    try {// loop through the result set
                        while (rs.next()) {
                            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                String columnValue = rs.getString(i);
                                ResultSetMetaData rsmd = rs.getMetaData();
                                String column_name = rsmd.getColumnName(i);
                                if(rsmd.getColumnType(i)==java.sql.Types.ARRAY) {
                                    result.get(column_name).add(rs.getArray(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT) {
                                    result.get(column_name).add(rs.getInt(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN) {
                                    result.get(column_name).add(rs.getBoolean(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.BLOB) {
                                    result.get(column_name).add(rs.getBlob(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE) {
                                    result.get(column_name).add(rs.getDouble(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT) {
                                    result.get(column_name).add(rs.getFloat(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER) {
                                    result.get(column_name).add(rs.getInt(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR) {
                                    result.get(column_name).add(rs.getNString(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR) {
                                    result.get(column_name).add(rs.getString(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT) {
                                    result.get(column_name).add(rs.getInt(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT) {
                                    result.get(column_name).add(rs.getInt(column_name));
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.DATE) {
                                    String date = rs.getString(column_name);
                                    result.get(column_name).add((date==null)?null:Date.valueOf(date));
                                    //result.get(column_name).add(rs.getDate(column_name));
                                    //rs.getTimestamp(column_name);
                                }
                                else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                                    result.get(column_name).add(rs.getTimestamp(column_name));
                                } else {
                                    result.get(column_name).add(rs.getObject(column_name));
                                }
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
        return result;
    }

    /**
     * SQL execution on connection!
     * @param sql
     */
    protected void _execute(String sql){
        Connection conn = _connection;
        try {
            Statement stmt = conn.createStatement();
            try {
                stmt.execute(sql);
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Converts a ResultSet into a JSON Object.
     * It can be converted to a String and is sent
     * to the client when requested (Ajax).
     * @param rs
     * @return
     * @throws SQLException
     * @throws JSONException
     */
    protected static JSONArray _toJSON( ResultSet rs ) throws SQLException, JSONException
    {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        while( rs.next() ) {
            int numColumns = rsmd.getColumnCount();
            JSONObject jo = new JSONObject();

            for (int i=1; i<numColumns+1; i++)
            {
                String column_name = rsmd.getColumnName(i);

                if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
                    jo.put(column_name, rs.getArray(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
                    jo.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                    jo.put(column_name, rs.getBoolean(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
                    jo.put(column_name, rs.getBlob(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                    jo.put(column_name, rs.getDouble(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                    jo.put(column_name, rs.getFloat(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                    jo.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                    jo.put(column_name, rs.getNString(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                    jo.put(column_name, rs.getString(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                    jo.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                    jo.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                    jo.put(column_name, rs.getDate(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                    jo.put(column_name, rs.getTimestamp(column_name));
                } else {
                    jo.put(column_name, rs.getObject(column_name));
                }
            }
            json.put(jo);
        }
        rs.close();
        return json;
    }

    protected JSONArray _toCRUD(ResultSet rs, String tableName, String[] tableNames) throws SQLException, JSONException
    {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        String relationTable = null;

        for(String t : tableNames){
            String[] words = t.split("_");
            boolean isRelationalTable = false;
            boolean isRelevant = false;
            for(String w : words) if(w.toLowerCase().contains("relation")) isRelationalTable = true;
            for(String w : words) if(w.toLowerCase().contains(tableName)) isRelevant = true;
            if (isRelationalTable && isRelevant && words.length==2) {
                relationTable = t;
            }
        }

        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for (int i=1; i<numColumns+1; i++)
            {
                String column_name = rsmd.getColumnName(i);

                if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
                    obj.put(column_name, rs.getArray(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                    obj.put(column_name, rs.getBoolean(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
                    obj.put(column_name, rs.getBlob(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                    obj.put(column_name, rs.getDouble(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                    obj.put(column_name, rs.getFloat(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                    obj.put(column_name, rs.getNString(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                    obj.put(column_name, rs.getString(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                    obj.put(column_name, rs.getDate(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                    obj.put(column_name, rs.getTimestamp(column_name));
                }
                else{
                    obj.put(column_name, rs.getObject(column_name));
                }
                if(relationTable!=null && obj.get("id")!=null){
                    Object id = obj.get("id");
                    String sql = "SELECT * FROM "+relationTable+" rt "+" WHERE rt.child_tails_id = "+id.toString();
                    final String targTable = relationTable;
                    _for(sql, cs->{
                        try {
                            obj.put("children", _toCRUD(cs, targTable, tableNames));
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }, null);

                }
            }
            json.put(obj);
        }
        return json;
    }

    /**
     * Converts a ResultSet into a Document data structure.
     * This later on used to generate XML!
     * @param rs This is a ResultSet fetched from a Database.
     * @return
     * @throws ParserConfigurationException
     * @throws SQLException
     */
    protected static Document toDocument(ResultSet rs)
            throws ParserConfigurationException, SQLException
    {
        //Define a new Document object
        Document dataDoc = null;
        try {
            //Create the DocumentBuilderFactory
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            //Create the DocumentBuilder
            DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
            //Instantiate a new Document object
            dataDoc = docbuilder.newDocument();
        } catch (Exception e) {
            System.out.println("Problem creating document: "+e.getMessage());
        }
        ResultSetMetaData resultmetadata = rs.getMetaData();
        //Create a new element called "data"
        Element dataRoot = dataDoc.createElement("data");
        int numCols = resultmetadata.getColumnCount();
        while (rs.next()) {
            //For each row of data
            //Create a new element called "row"
            Element rowEl = dataDoc.createElement("row");
            for (int i=1; i <= numCols; i++) {
                //For each column, retrieve the name and data
                String colName = resultmetadata.getColumnName(i);
                String colVal = rs.getString(i);
                //If there was no data, add "and up"
                if (rs.wasNull()) {
                    colVal = "and up";
                }
                //Create a new element with the same name as the column
                Element dataEl = dataDoc.createElement(colName);
                //Add the data to the new element
                dataEl.appendChild(dataDoc.createTextNode(colVal));
                //Add the new element to the row
                rowEl.appendChild(dataEl);
            }
            //Add the row to the root element
            dataRoot.appendChild(rowEl);
        }
        //Add the root element to the document
        dataDoc.appendChild(dataRoot);
        return dataDoc;
    }



    protected void _executeFile(String name){
        Connection conn = _connection;
        String[] commands;
        File file = new File("db/", name);
        int fileLength = (int) file.length();
        try {
            byte[] fileData = IPlugin.util.readFileData(file, fileLength);
            String query = new String(fileData);
            commands = query.split("--<#SPLIT#>--");
            for(String command : commands){
                _execute(command);
            }
            try {
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
