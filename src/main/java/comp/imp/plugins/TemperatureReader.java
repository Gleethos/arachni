package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Date;

public class TemperatureReader extends AbstractDatabaseConnection implements IPlugin {

    public TemperatureReader() {

        super("jdbc:sqlite:C:/sqlite/db/TempDB", "", "");

        Connection conn = _createAndOrConnectToDatabase();

        // SQL statement for creating a new table
        //String sql = "CREATE TABLE IF NOT EXISTS warehouses (\n"
        //        + "    id integer PRIMARY KEY,\n"
        //        + "    name text NOT NULL,\n"
        //        + "    capacity real\n"
        //        + ");";
        //_createNewTable(conn, sql);

        //---
        File file = new File("db/", "setup.sql");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = util.readFileData(file, fileLength);
            String query = new String(fileData);
            String[] commands = query.split("--<#SPLIT#>--");
            for(String command : commands){
                _execute(command, conn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        _listOfTables(conn);
        _close(conn);

        Thread iot = new Thread(()->{
            long time = 100000000L;
            for(int ti=0; ti<time; ti++){
                try {
                    Thread.sleep(ti);
                } catch (Exception e){

                }
                double temp = Math.cos(1.2324453*ti)*60-15;
                String command =
                        "INSERT INTO temperatures (id, value, created)\n" +
                        "VALUES ("+(6+ti)+", "+temp+", datetime('now'));";
                Connection iotConn = _createAndOrConnectToDatabase();
                //_listOfTables(iotConn);
                _execute(command, iotConn);
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
        String content = "plain/text";
        response.setServerHeader("Webio Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", content);
        response.getHeaders().put("content-length", String.valueOf(contentLength));
        String sql = util.decodeValue(req.getContentString());
        sql = (sql.substring(0, 6).equals("query=")) ? sql.substring(6, sql.length()) : sql;
        String result = "";
        try {
            Statement stmt= conn.createStatement();
            System.out.println("sql: "+sql);
            ResultSet rs = stmt.executeQuery(sql);
            result = convert(rs).toString();
        } catch (SQLException e) {
            e.printStackTrace();
            result+=e.toString();
        }
        byte[] jsonData;
        try {
            jsonData = result.getBytes("UTF-8");
        } catch (Exception e) {
            jsonData = result.getBytes();
        }
        response.setContent(jsonData);
        _close(conn);
        return response;
    }

    /**
     * Create a new table in the test database
     */
    private void _createNewTable(Connection conn, String sql) {
        try {
             Statement stmt = conn.createStatement();// create a new table
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
