package BIF.SWE1.unittests;

import BIF.SWE1.uebungen.Test8Provider;
import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.plugins.CRUD;
import org.junit.Test;

import java.io.File;

public class Test_8_CRUD  extends AbstractTestFixture<Test8Provider> {

    @Test
    public void test_CRUD_search_fields() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("200 OK");
        assert body.contains("id=\"set_db_url_response_box\"");
        assert body.contains("name=\"parent_tail_id");
        assert body.contains("name=\"id");
        assert body.contains("name=\"child_tail_id");
        assert body.contains("id=\"tags_search\"");
        assert body.contains("id=\"tail_relations_search\"");
        assert body.contains("<button onclick=");
        assert body.contains("each(function () {");
        assert body.contains("text/html");
        assert body.contains("load");
        assert body.contains("tails");
        assert body.contains("input");
        assert body.contains("textarea");
        assert !body.contains("content-length: 0");
        assert res.getContentType().contains("text/html");
        String compact = body.replace(" ", "").replace("\n","");
        assert compact.contains(".each(function(){params[this.name]=this.value;});");
        assert compact.contains("children('input,textarea')");
    }

    @Test
    public void test_CRUD_finding() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tail_relations?" +
                                "id=1&" +
                                "parent_tail_id=&child_tail_id=&" +
                                "description=&created=&deleted="
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("200 OK");
        assert body.contains("parent_tail_id");
        assert body.contains("child_tail_id");
        assert body.contains("<input");
        assert body.contains("<span");
        assert body.contains("name=\"child_tail_id\"");
        assert body.contains("name=\"id\"");
        assert body.contains("id=\"tail_relations_1_id\"");
        assert body.contains("id=\"tail_relations_1_child_tail_id\"");
        assert body.contains("id=\"tail_relations_1_description\"");
        assert !body.contains("id=\"\"");
        assert !body.contains("id=\"description_2\"");
        assert !body.contains("id=\"id_2\"");
        assert !body.contains("content-length: 0");
        String compact = body.replace(" ", "");
        assert compact.replace(" ","").contains("<spanvalue=\"0\"");
        assert compact.replace(" ", "").contains("oninput=\"noteOnInputFor('id','tail_relations'");
        assert compact.contains("col-sm-");
        assert compact.contains("col-md-");
        assert compact.contains("col-lg-4");
        assert compact.contains("col-sm-12");
        assert !compact.contains("textarea");

        assert res.getContentType().contains("text/html");
    }

    @Test
    public void test_CRUD_saving_GET_requests() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tail_relations?" +
                                "id=1&" +
                                "parent_tail_id=&child_tail_id=&" +
                                "description=&created=&deleted="
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("200 OK");
        assert body.contains("parent_tail_id");
        assert body.contains("child_tail_id");
        assert body.contains("<input");
        assert body.contains("<span");
        assert body.contains("name=\"child_tail_id\"");
        assert body.contains("name=\"id\"");
        assert body.contains("id=\"tail_relations_1_id\"");
        assert body.contains("id=\"tail_relations_1_child_tail_id\"");
        assert body.contains("id=\"tail_relations_1_description\"");
        assert !body.contains("id=\"\"");
        assert !body.contains("id=\"tail_relations_2_description\"");
        assert !body.contains("id=\"tail_relations_2_id\"");
        String compact = body.replace(" ", "");
        assert compact.replace(" ","").contains("<spanvalue=\"0\"");
        assert compact.replace(" ", "").contains("oninput=\"noteOnInputFor('id','tail_relations'");

        assert res.getContentType().contains("text/html");

        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tail_relations?" +
                                "parent_tail_id=&child_tail_id=&" +
                                "description=ThisIsASavingTest&created=Today&deleted=Tomorrow"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains(
                "Execution for the following query failed:\n" +
                "'INSERT INTO tail_relations\n" +
                "(deleted, created, description)\n" +
                "VALUES\n" +
                "('Tomorrow','Today','ThisIsASavingTest')'\n" +
                "\n" +
                "Reason:\n" +
                "[SQLITE_CONSTRAINT]  Abort due to constraint violation (NOT NULL constraint failed: tail_relations.parent_tail_id)"
        );

        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tail_relations?" +
                                "parent_tail_id=1&child_tail_id=1&" +
                                "description=ThisIsASavingTest&created=Today&deleted=Tomorrow"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains("ThisIsASavingTest");
        assert body.contains("Today");
        assert body.contains("Tomorrow");
        assert body.contains("Today");
        assert body.contains("oninput=\"noteOnInputFor('parent_tail_id','tail_relations'");
        assert body.contains("value=\"ThisIsASavingTest\"");
        assert body.contains("name=\"child_tail_id\"");
        assert !body.contains("textarea");

        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails?" +
                                "name=MyLittleTail&value=TailContentJadida&created=AlsoToday&deleted=AlsoTomorrow"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains("value=\"MyLittleTail\"");
        assert !body.contains("value=\"TailContentJadida\"");
        assert body.contains("value=\"AlsoToday\"");
        assert body.contains("value=\"AlsoTomorrow\"");
        assert body.contains("id=\"tails_4_name\"");
        assert body.contains("id=\"tails_4_id\"");
        assert body.contains("oninput=\"noteOnInputFor('name','tails','4')");
        assert body.contains("id=\"tails_4\"");
        assert body.contains("textarea");
        assert !body.contains("content-length: 0");
        compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','4')\">TailContentJadida</textarea>");
        assert compact.contains("<spanvalue=\"0\"id=\"tails_4_value\">");
    }

    @Test
    public void test_CRUD_saving_tail_with_POST() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails",
                                "POST",
                                "name=SuperTail&value=TailContent...&created=&deleted=InTheFuture"
                )
        );
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        assert body.contains("value=\""+date+"\""); // Autofill for created!
        assert body.contains("value=\"InTheFuture\"");
        assert body.contains("value=\"SuperTail\"");
        assert !body.contains("value=\"TailContent...\"");
        String compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','4')\">TailContent...</textarea>");
        assert !body.contains("content-length: 0");
    }

    @Test
    public void test_CRUD_saving_tail_fails_with_POST() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails",
                        "POST",
                        "name=&value=&created=&deleted=InTheFuture"
                )
        );
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        assert body.contains(
                "Execution for the following query failed:\n" +
                "'INSERT INTO tails\n" +
                "(created, deleted)\n" +
                "VALUES\n" +
                "('"+date+"','InTheFuture')'\n" +
                "\n" +
                "Reason:\n" +
                "[SQLITE_CONSTRAINT]  Abort due to constraint violation (NOT NULL constraint failed: tails.name)"
        );
        assert res.getStatusCode()==500;
        assert res.getContentType().equals("text/html");
        assert !body.contains("value=\""+date+"\"");
        assert !body.contains("value=\"InTheFuture\"");
        assert !body.contains("value=\"SuperTail\"");
        assert !body.contains("value=\"TailContent...\"");
        assert !body.contains("content-length: 0");
    }

    @Test
    public void test_CRUD_saving_new_tail_with_POST() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails",
                        "POST",
                        "deleted=&created=&name=TestName&id=&value=hiA"
                )
        );
        req.getHeaders().put("content-length","45");
        req.getHeaders().put("referer","http://localhost:8080/tailworld.html");
        req.getHeaders().put("accept-language","en-GB,en;q=0.5");
        req.getHeaders().put("cookie","Idea-dfa430ed=00a8de2c-5a58-4fe7-aa8d-ae8ece943c59; __gads=ID=b5cd3b2ed6ee2db3:T=1573557692:S=ALNI_MaIqX3lhpUYUxw5YHPOGrMD0dTJig; __qca=P0-1847549801-1573557691786; username-localhost-8888=\"2|1:0|10:1593445795|23:username-localhost-8888|44:YmZjZDA5Y2ZhOTJlNDg2Yjk3OTk4Mjc4NDdkYTM1NTk=|c9eb0d13dc03197cc7b480ffe1560107fbe7b8017685cb68b520f5a7ef038c75\"; Idea-ad60603c=e96276ac-0f55-4228-a170-c4aba16b47d6; _xsrf=2|6e787767|f081c7352a7dc2074e5028b80c973cb3|1593445795");
        req.getHeaders().put("origin","http://localhost:8080");
        req.getHeaders().put("accept","text/html, */*; q=0.01");
        req.getHeaders().put("post","/CRUD/save/tails HTTP/1.1");
        req.getHeaders().put("host","localhost:8080");
        req.getHeaders().put("x-requested-with","XMLHttpRequest");
        req.getHeaders().put("content-type","application/x-www-form-urlencoded; charset=UTF-8");
        req.getHeaders().put("connection","keep-alive");
        req.getHeaders().put("accept-encoding","gzip, deflate");
        req.getHeaders().put("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:78.0) Gecko/20100101 Firefox/78.0");
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        assert body.contains("value=\""+date+"\""); // Autofill for created!
        assert body.contains("value=\"TestName\"");
        assert !body.contains("value=\"hiA\"");
        assert !body.contains("content-length: 0");
        String compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','4')\">hiA</textarea>");

    }

    @Test
    public void test_CRUD_finding_POST_request() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tails?", "POST",
                                "name=First"
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("200 OK");
        assert body.contains("EntityWrapper");
        assert body.contains("value=\"First Tail\"");
        assert body.contains("value=\"Second Tail\""); // It also contains second tail as relation entity!
        assert !body.contains("value=\"Third Tail\"");
        assert body.contains("oninput=\"noteOnInputFor('id','tails','1')\"");
        assert body.contains("id=\"tails_1_created\"");
        assert !body.contains("content-length: 0");
        String compact = body.replace(" ", "");
        assert compact.contains("name=\"deleted\"value=\"\"");
        assert res.getContentType().contains("text/html");
    }

    @Test
    public void test_CRUD_finding_and_deleting_with_POST_request() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB");
        // 1. Finding:
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tails?", "POST",
                        "name=First"
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("200 OK");
        assert body.contains("EntityWrapper");
        assert body.contains("value=\"First Tail\"");
        assert body.contains("value=\"Second Tail\""); // It also contains the second tail as relational entity!
        assert !body.contains("value=\"Third Tail\"");
        assert body.contains("oninput=\"noteOnInputFor('id','tails','1')\"");
        assert body.contains("id=\"tails_1_created\"");
        assert res.getContentType().contains("text/html");
        String compact = body.replace(" ", "");
        assert compact.contains("name=\"deleted\"value=\"\"");

        // 2. Deleting (failing):
        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/delete/tails?", "POST",
                        "name=ThisShouldFail"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains("Deletion failed! Request does not contain 'id' value!");
        assert body.contains("500 Internal Server Error");
        assert body.contains("text/html");

        // 3. Deleting (successful):
        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/delete/tails?", "POST",
                        "id=1"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains("200 OK");
        assert body.contains("text/javascript");
        assert !body.contains("Deletion failed! Request does not contain 'id' value!");
        assert body.contains("$('#tail_relations_1').replaceWith('');");
        assert body.contains("$('#tail_tag_relations_1').replaceWith('');");

        // 4. Finding:
        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tails?", "POST",
                        "name=First"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains("200 OK");
        assert body.contains("Nothing found!");
        assert body.contains("content-type: text/html");
        assert body.contains("content-length: 14");
    }

    @Test
    public void test_CRUD_deleting_with_foreign_auto_delete_with_POST_request() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB");
        // 1. add relation between first and second tail:
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tail_relations?", "POST",
                        "parent_tail_id=1&child_tail_id=2&description=SomeDescription"
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("200 OK");
        assert body.contains("EntityWrapper");
        assert body.contains("value=\"SomeDescription\"");
        assert body.contains("oninput=\"noteOnInputFor('id','tail_relations','2')\"");
        assert body.contains("id=\"tail_relations_2_created\"");
        assert body.contains("id=\"tail_relations_2\"");
        assert body.contains("id=\"tail_relations_2_parent_tail_id\"");
        assert res.getContentType().contains("text/html");

        // 2. try deleting first tail (failing):
        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/delete/tails?", "POST",
                        "id=1"
                )
        );
        res = crud.handle(req); // This should work... -> despite relation entries!
        body = getBody(res).toString();
        assert body.contains("200 OK");
        assert body.contains("text/javascript");
        assert body.contains("$('#tail_relations_1').replaceWith('');\n");
        assert body.contains("$('#tail_relations_2').replaceWith('');\n");
        assert body.contains("$('#tail_tag_relations_1').replaceWith('');");

        // 3. Finding relation referencing deleted tail! (should find nothing!):
        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tail_relations?", "POST",
                        "parent_tail_id=1"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains("200 OK");
        assert body.contains("Nothing found!");
        assert body.contains("content-type: text/html");
        assert body.contains("content-length: 14");

        // 3. Finding tag/tail relation referencing deleted tail! (should find nothing!):
        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tail_tag_relations?", "POST",
                        "id=1"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains("200 OK");
        assert body.contains("Nothing found!");
        assert body.contains("content-type: text/html");
        assert body.contains("content-length: 14");
    }


    @Test
    public void test_CRUD_setJDBC() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB");
        String path = new File("test/db").getAbsolutePath().replace("\\","/");
        assert crud instanceof CRUD;
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:"+path+"/TestDB");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/setJDBC?url=THIS_IS_A_TEST_VALUE")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("JDBC url set to : 'THIS_IS_A_TEST_VALUE'");
        assert ((CRUD)crud).getURL().equals("THIS_IS_A_TEST_VALUE");
        assert res.getContentType().contains("text/html");

        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/setJDBC?url=jdbc:sqlite:"+path+"/TestDB")
        );
        res = crud.handle(req);
        body = getBody(res).toString();

        assert body.contains("JDBC url set to : 'jdbc:sqlite:"+path+"/TestDB'");
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:"+path+"/TestDB");
        assert res.getContentType().contains("text/html");
    }



    @Override
    protected Test8Provider createInstance() {
        return new Test8Provider();
    }
}
