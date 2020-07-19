package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;
import java.sql.*;
import java.util.Date;

public class Oracle extends AbstractDatabaseConnection implements IPlugin
{
    public Oracle()
    {
        super(
                "jdbc:oracle:thin:@infdb.technikum-wien.at:1521:o10",
                "w19bif3_if17b032",
                "dbsw19"
        );
    }

    @Override
    public float canHandle(IRequest req) {
        float abillity = BASELINE;
        if(req.getUrl().getRawUrl().contains("Oracle")){
            abillity *= 1 + (0.7 * (1-abillity));
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
        String content = "plain/text";
        response.setServerHeader("Webio Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", content);
        String sql = util.decodeValue(req.getContentString());
        sql = (sql.startsWith("query=")) ? sql.substring(6, sql.length()) : sql;
        String result = "";

        try {
            _createAndOrConnectToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            response.setContent(e.getMessage());
            return response;
        }
        Connection conn = _connection();
        try {
            Statement stmt= conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            result = _toJSON(rs).toString();
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
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        _close();
        return response;
    }





}
