package comp.imp.plugins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

public abstract class AbstractDatabaseConnection {

    private String _url, _user, _pwd;

    AbstractDatabaseConnection(String url, String name, String password){
        _url = url;
        _user = name;
        _pwd = password;
    }

    /**
     * Connect to a sample database
     */
    protected Connection _createAndOrConnectToDatabase()
    {
        Connection conn = null;
        if(_user.equals("")||_pwd.equals("")){
            try {
                conn = DriverManager.getConnection(_url);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            try {
                conn = DriverManager.getConnection(_url, _user, _pwd);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        if (conn != null) {
            try {
                DatabaseMetaData meta = conn.getMetaData();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return conn;
        }
        return null;
    }


    /**
     * Closing Connection!
     */
    protected void _close(Connection conn){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void _listOfTables(Connection conn){
        String sql =
                "SELECT\n"+
                        "name\n"+
                        "FROM\n"+
                        "sqlite_master\n"+
                        "WHERE\n"+
                        "type ='table' AND\n"+
                        "name NOT LIKE 'sqlite_%';";
        try {//(Connection conn = DriverManager.getConnection(url)){
            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(sql);// loop through the result set
                while (rs.next()) {
                    System.out.println("\n"+rs.getString("name") + "");
                    _selectAllFrom(rs.getString("name"), conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void _selectAllFrom(String tableName, Connection conn){
        String sql = "SELECT * FROM "+tableName+";\n";
        try {//(Connection conn = DriverManager.getConnection(url)){
            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(sql);
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print("| " + rsmd.getColumnName(i) + " |");
                }
                // loop through the result set
                while (rs.next()) {
                    System.out.print("\n");
                    for (int i = 1; i <= columnsNumber; i++) {
                        String columnValue = rs.getString(i);
                        System.out.print("| " + columnValue + " |");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    protected static void _execute(String command, Connection conn){
        try {
            Statement stmt = conn.createStatement();
            try {
                stmt.execute(command);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }



    protected static JSONArray convert(ResultSet rs ) throws SQLException, JSONException
    {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for (int i=1; i<numColumns+1; i++) {
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
            }
            json.put(obj);
        }
        return json;
    }



}
