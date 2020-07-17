package testsuite.unittests;

import testsuite.provider.Test_7_Provider;
import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.PluginManager;
import comp.imp.plugins.TemperatureReader;
import core.ClientHandler;
import org.junit.*;

import java.io.*;
import java.time.LocalDate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Test_7_CrossPlugins extends AbstractTestFixture<Test_7_Provider> {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Override
    protected Test_7_Provider createInstance() {
        return new Test_7_Provider();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void HelloWorld() throws Exception {
        Test_7_Provider ueb = createInstance();
        ueb.helloWorld();
    }

    @Test
    public void self_test(){
        Test_7_Provider ueb = createInstance();
        assertNotNull("Tests7.getNavigationPlugin returned null", ueb);

        IPlugin obj = ueb.getOraclePlugin();
        assertNotNull("Tests7.getOraclePlugin returned null", obj);

        String url = ueb.getOracleUrl();
        assertNotNull("Tests7.getOracleUrl returned null", url);

    }

    @Test
    public void oracle_returns_list_of_tables() throws Exception
    {
        Test_7_Provider ueb = createInstance();

        IPlugin obj = ueb.getOraclePlugin();
        assertNotNull("Tests7.getOraclePlugin returned null", obj);

        String url = ueb.getOracleUrl();
        assertNotNull("Tests7.getOracleUrl returned null", url);

        IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url, "POST", "SELECT owner, table_name FROM all_tables"));
        assertNotNull("Tests7.GetRequest returned null", req);

        float canHandle = obj.canHandle(req);
        assertTrue(canHandle > 0 && canHandle <= 1);

        IResponse resp = obj.handle(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getContentLength() > 0);

        StringBuilder body = getBody(resp);
        //assertTrue("Not found: Bitte geben Sie eine Anfrage ein", body.toString().contains("Bitte geben Sie eine Anfrage ein"));
        assertTrue("Data in response body missing!", body.toString().contains("OWNER"));
        assertTrue("Data in response body missing!", body.toString().contains("SYS"));
        assertTrue("Data in response body missing!", body.toString().contains("TABLE_NAME"));
        assertTrue("Data in response body missing!", body.toString().contains("DUAL"));
        assertTrue("Data in response body missing!", body.toString().contains("SYSTEM_PRIVILEGE_MAP"));
        assertTrue("Data in response body missing!", body.toString().contains("ODCI_WARNINGS"));
    }

    @Test
    public void static_utility_method_for_content_type_returns(){

        String type = IPlugin.util.getContentType("myImg.png");
        assertTrue(type.equals("image/png"));
        type = IPlugin.util.getContentType("myImg.html");
        assertTrue(type.equals("text/html"));
        type = IPlugin.util.getContentType("myImg.jpg");
        assertTrue(type.equals("image/jpeg"));
        type = IPlugin.util.getContentType("myStyle.css");
        assertTrue(type.equals("text/css"));
        type = IPlugin.util.getContentType("myText.txt");
        assertTrue(type.equals("text/plain"));
        type = IPlugin.util.getContentType("myCode.js");
        assertTrue(type.equals("text/javascript"));
    }

    @Test
    public void static_utility_method_file_not_found_returns() throws Exception{

        Test_7_Provider ueb = createInstance();

        IPlugin obj = ueb.getTemperaturePlugin();
        assertNotNull("Tests7.getTemperaturePlugin returned null", obj);

        String url = ueb.getTemperatureUrl(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2));
        assertNotNull("Tests7.getTemperatureUrl returned null", url);

        IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url, "POST", "SELECT * FROM temperatures"));
        assertNotNull("Tests7.GetRequest returned null", req);

        float canHandle = obj.canHandle(req);
        assertTrue(canHandle > 0 && canHandle <= 1);

        IResponse resp = obj.handle(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getContentLength() > 0);

        IPlugin.util.fileNotFound(resp, "someFile.xyz");
        assertEquals(404, resp.getStatusCode());
        assertTrue(resp.getContentLength() > 0);

        String body = getBody(resp).toString();
        assertTrue(body.contains("File not found"));
        assertTrue(body.contains("<p>File not found! ... Sorry!</p>"));
        assertTrue(body.contains("Webio"));
        assertTrue(body.contains("<div id = \"NotFoundBox\" class = \"warm\">"));
        assertTrue(body.contains("<div id = \"Information\">"));
        assertTrue(body.contains("body"));
    }

    @Test
    public void oracle_returns_list_of_stars() throws Exception
    {
        Test_7_Provider ueb = createInstance();

        IPlugin obj = ueb.getOraclePlugin();
        assertNotNull("Tests7.getOraclePlugin returned null", obj);

        String url = ueb.getOracleUrl();
        assertNotNull("Tests7.getOracleUrl returned null", url);

        IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url, "POST", "SELECT * FROM stars"));
        assertNotNull("Tests7.GetRequest returned null", req);

        float canHandle = obj.canHandle(req);
        assertTrue(canHandle > 0 && canHandle <= 1);

        IResponse resp = obj.handle(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getContentLength() > 0);

        StringBuilder body = getBody(resp);
        //assertTrue("Not found: Bitte geben Sie eine Anfrage ein", body.toString().contains("Bitte geben Sie eine Anfrage ein"));
        assertTrue("Data in response body missing!", body.toString().contains("NAME"));
        assertTrue("Data in response body missing!", body.toString().contains("ID"));
        assertTrue("Data in response body missing!", body.toString().contains("CODE"));
        assertTrue("Data in response body missing!", body.toString().contains("CREATED"));
        assertTrue("Data in response body missing!", body.toString().contains("Sun"));
        assertTrue("Data in response body missing!", body.toString().contains("ORBITAL_OBJECT_ID"));
    }

    @Test
    public void temp_plugin_returns_list_of_temperatures() throws Exception
    {
        Test_7_Provider ueb = createInstance();

        IPlugin obj = ueb.getTemperaturePlugin();
        assertNotNull("Tests7.getNavigationPlugin returned null", obj);

        String url = "Temp";//ueb.getTemperatureUrl(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2));
        assertNotNull("Tests7.getNaviUrl returned null", url);

        IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url, "POST", "SELECT * FROM temperatures"));
        assertNotNull("Tests7.GetRequest returned null", req);

        float canHandle = obj.canHandle(req);
        assertTrue(canHandle > 0 && canHandle <= 1);

        IResponse resp = obj.handle(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getContentLength() > 0);

        StringBuilder body = getBody(resp);
        //assertTrue("Not found: Bitte geben Sie eine Anfrage ein", body.toString().contains("Bitte geben Sie eine Anfrage ein"));
        assertTrue("Data in response body missing!", body.toString().contains("created"));
        assertTrue("Data in response body missing!", body.toString().contains("id"));
        assertTrue("Data in response body missing!", body.toString().contains("value"));
        assertTrue("Data in response body missing!", body.toString().contains(":23.43"));
        assertTrue("Data in response body missing!", body.toString().contains("},{"));
        //assertTrue("Data in response body missing!", body.toString().contains(":45"));
    }

    @Test
    public void test_plugin_count(){
        PluginManager manager = new PluginManager();
        manager.add("Oracle");
        int[] counter = {0};
        manager.getPlugins().forEach((e)->{
            counter[0]++;
        });
        assert counter[0]>2;
    }

    @Test
    public void temp_plugin_has_data(){
        PluginManager manager = new PluginManager();
        TemperatureReader temp = (TemperatureReader) manager.get("TemperatureReader");
        assert temp!=null;
        assert temp.tempCount() > 10_000;
    }

    @Test
    public void test_exception_in_ClientHandler(){
        try{
            new ClientHandler(null, null, null);
        } catch(Exception e){
            assert true;
        }
    }


    @Test
    public void temp_plugin_returns_list_of_temperatures_as_xml() throws Exception
    {
        Test_7_Provider ueb = createInstance();

        IPlugin obj = ueb.getTemperaturePlugin();
        assertNotNull("Tests7.getNavigationPlugin returned null", obj);

        String url = ueb.getTemperatureUrl(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2));
        assertNotNull("Tests7.getNaviUrl returned null", url);

        IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url, "POST", "SELECT * FROM temperatures"));
        assertNotNull("Tests7.GetRequest returned null", req);

        float canHandle = obj.canHandle(req);
        assertTrue(canHandle > 0 && canHandle <= 1);

        IResponse resp = obj.handle(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getContentLength() > 0);

        StringBuilder body = getBody(resp);
        //assertTrue("Not found: Bitte geben Sie eine Anfrage ein", body.toString().contains("Bitte geben Sie eine Anfrage ein"));
        assertTrue("Data in response body missing!", body.toString().contains("created"));
        assertTrue("Data in response body missing!", body.toString().contains("id"));
        assertTrue("Data in response body missing!", body.toString().contains("value"));
        assertTrue("Data in response body missing!", body.toString().contains("<value>16.0509</"));
        assertTrue("Data in response body missing!", body.toString().contains("<created>"));
        //assertTrue("Data in response body missing!", body.toString().contains(":45"));
    }

    @Test
    public void test_plugin_utility_and_sql_setup(){
        String[] commands = new String[0];
        File file = new File("db/temperature", "setup.sql");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = IPlugin.util.readFileData(file, fileLength);
            assertTrue(fileData!=null);
            assertTrue(fileData.length>0);
            String query = new String(fileData);
            assertTrue(query!=null);
            commands = query.split("--<#SPLIT#>--");
            assertTrue("", commands.length>=6);
            for(String command : commands){
                assertTrue("Statement does not contain 'temperatures'!", command.contains("temperatures"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Exception occurred! Could not read 'setup.sql'!", false);
        }

    }

    @Test
    public void setup_test() throws Exception {
        Test_7_Provider ueb = createInstance();
        ueb.helloWorld();
    }

    @Test
    public void index_file_must_contain(){
        File file = new File("webroot/", "index.html");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = IPlugin.util.readFileData(file, fileLength);
            assertTrue(fileData!=null);
            assertTrue(fileData.length>0);
            String index = new String(fileData);
            assertTrue(index!=null);
            assertTrue(index.contains("Java"));
            assertTrue(index.toLowerCase().contains("arachni"));
            assertTrue(index.contains("Oracle"));
            assertTrue(index.contains("query"));
            assertTrue(index.contains("data"));
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Exception occurred! Could not read 'setup.sql'!", false);
        }

    }

    @Test
    public void readme_must_exist_and_contain(){
        File file = new File(".", "README.md");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = IPlugin.util.readFileData(file, fileLength);
            assertTrue(fileData!=null);
            assertTrue(fileData.length>0);
            String index = new String(fileData);
            assertTrue(index!=null);
            assert fileLength>0;
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Exception occurred! Could not read 'setup.sql'!", false);
        }

    }

    @Test
    public void index_file_must_contain2(){
        File file = new File("webroot/", "index.html");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = IPlugin.util.readFileData(file, fileLength);
            assertTrue(fileData!=null);
            assertTrue(fileData.length>0);
            String index = new String(fileData);
            assertTrue(index!=null);
            assertTrue(index.contains("table"));
            assertTrue(index.contains("SubmitToLower"));
            assertTrue(index.contains("$(\"#LoweredContent\").html(data);"));
            assertTrue(index.contains("<input type=\"text\" id=\"ToBeLowered\">"));
            assertTrue(index.contains("id=\"TempResult\""));
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Exception occurred! Could not read 'setup.sql'!", false);
        }

    }

    @Test
    public void index_file_must_contain3(){
        File file = new File("webroot/", "index.html");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = IPlugin.util.readFileData(file, fileLength);
            assertTrue(fileData!=null);
            assertTrue(fileData.length>0);
            String index = new String(fileData);
            assertTrue(index!=null);
            assertTrue(index.contains("Temp"));
            assertTrue(index.contains("SELECT * FROM"));
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Exception occurred! Could not read 'index.html'!", false);
        }

    }

    @Test
    public void html_main_index_file_must_contain(){
        File file = new File(".", "index.html");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = IPlugin.util.readFileData(file, fileLength);
            assertTrue(fileData!=null);
            assertTrue(fileData.length>0);
            String html = new String(fileData);
            assertTrue(html!=null);
            assertTrue(html.contains("<p>Hello world! :) This is the index.html file in the classpath</p>"));
            assertTrue(html.contains("</head>"));
            assertTrue(html.contains("body"));
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Exception occurred! Could not read 'setup.sql'!", false);
        }

    }

    @Test
    public void html_404_file_must_contain(){
        File file = new File("webroot/", "404.html");
        int fileLength = (int) file.length();
        try {
            byte[] fileData = IPlugin.util.readFileData(file, fileLength);
            assertTrue(fileData!=null);
            assertTrue(fileData.length>0);
            String html = new String(fileData);
            assertTrue(html!=null);
            assertTrue(html.contains("<p>File not found! ... Sorry!</p>"));
            assertTrue(html.toLowerCase().contains("arachni"));
            assertTrue(html.contains("<div id = \"NotFoundBox\" class = \"warm\">"));
            assertTrue(html.contains("<div id = \"Information\">"));
            assertTrue(html.contains("body"));
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Exception occurred! Could not read '404.html'!", false);
        }

    }

    @Test
    public void temp_plugin_temp_database_gets_bigger_over_time() throws Exception
    {
        Test_7_Provider ueb = createInstance();

        IPlugin obj = ueb.getTemperaturePlugin();
        assertNotNull("Tests7.getNavigationPlugin returned null", obj);

        String url = ueb.getTemperatureUrl(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2));
        assertNotNull("Tests7.getNaviUrl returned null", url);

        IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url, "POST", "SELECT * FROM temperatures"));
        assertNotNull("Tests7.GetRequest returned null", req);

        float canHandle = obj.canHandle(req);
        assertTrue(canHandle > 0 && canHandle <= 1);

        IResponse resp = obj.handle(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getContentLength() > 0);

        String body = getBody(resp).toString();

        //WAITING!
        Thread.sleep(3500);

        IRequest req2 = ueb.getRequest(RequestHelper.getValidRequestStream(url, "POST", "SELECT * FROM temperatures"));
        assertNotNull("Tests7.GetRequest returned null", req2);
        canHandle = obj.canHandle(req2);
        assertTrue(canHandle > 0 && canHandle <= 1);
        IResponse resp2 = obj.handle(req);
        assertNotNull(resp2);
        assertEquals(200, resp2.getStatusCode());
        assertTrue(resp2.getContentLength() > 0);

        String body2 = getBody(resp2).toString();

        assertTrue(body2.length()>=body.length());
        assertTrue(body2.split("id").length>=body.split("id").length);
    }



}
