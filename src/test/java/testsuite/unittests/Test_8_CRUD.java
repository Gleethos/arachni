package testsuite.unittests;

import testsuite.TestUtility;
import testsuite.provider.Test_8_Provider;
import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.plugins.CRUD;
import org.junit.Test;

import java.io.File;

public class Test_8_CRUD extends AbstractTestFixture<Test_8_Provider> {

    @Test
    public void test_default_CRUD_response() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("200 OK");
        assert body.contains(".children('.tabBody')");
        assert body.contains("confirm(\"Do you really want to");
        assert body.contains("eval(response);");
        assert body.contains("parseInt(element.attr('value'))");
        assert body.contains("'CRUD/delete/' + tableName+' #' + tableName+'_'+id+' > *',");
        assert !body.contains("content-length: 0");
        assert res.getContentType().contains("text/html");
        String compact = body.replace(" ", "").replace("\n","");
        assert compact.contains(".each(function(){params[this.name]=this.value;});");
        assert compact.contains("children('input,textarea')");
        assert compact.contains("switchTab(src,target)");
        assert compact.contains("tabBody.children().css(\"display\",\"none\");");
        assert compact.contains(".children('textarea').first().focus();");
        assert compact.contains("$('#'+tableName+'_search').children('input,textarea')");
    }

    @Test
    public void test_CRUD_search_fields() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/view")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("200 OK");
        assert body.contains("id=\"sql_world_source\"");
        assert body.contains("id=\"jdbc_world_url\"");
        assert !body.contains("name=\"parent_tail_id");
        assert !body.contains("name=\"id");
        assert !body.contains("name=\"child_tail_id");
        assert !body.contains("id=\"tags_search\"");
        assert !body.contains("id=\"tail_relations_search\"");
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
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");
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
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");
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
        assert compact.contains("<spanvalue=\"0\"id=\"tails_4_value_span\">");
        assert compact.contains("<spanvalue=\"0\"id=\"tails_4_created_span\">");
        assert compact.contains("<spanvalue=\"0\"id=\"tails_4_deleted_span\">");
        assert compact.contains("<spanvalue=\"0\"id=\"tails_4_name_span\">");
        assert compact.contains("<textareaid=\"tails_4_value\"class=\"TailsValue\"name=\"value\"oninput=\"noteOnInputFor('value','tails','4')\">TailContentJadida</textarea>");
    }

    @Test
    public void test_CRUD_saving_tail_with_POST() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");

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
        TestUtility.assertContains(body, new String[]{
                "id=\"tails_4_buttons\"", "class=\"EntityWrapper EntityShadow row\"",
                "id=\"tails_4\"",
                "id=\"tags_new_related_buttons\"",
                "value=\""+date+"\"", // Autofill for created!
                "value=\"InTheFuture\"",
                "value=\"SuperTail\"",
                "onclick=\"new_tail_tag_relations_and_tags_4_joined_on_tag_id()\"",
                "oninput=\"noteOnInputFor('id','tags','new')",
                "class=\"TagsDescription\""
        });
        TestUtility.assertNotContains(body, new String[]{
                "value=\"TailContent...\"",
                "content-length: 0",
                "id=\"tails_4_related\"", // A new tail will not have any relations!
                "id=\"tails_4_related_buttons\"",
        });
        String compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','4')\">TailContent...</textarea>");
    }

    @Test
    public void test_CRUD_saving_RELATED_tail_with_POST_WITH_buttons() throws Exception
    {
        /*
          NOTE: if an entity that is being searched for is without relation, then IT IS THE RELATION!
          This is being tested below: ( 'appendRelations=false'  ==  RELATED! )
         */
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails?appendButtons=true&appendRelations=false",
                        "POST",
                        "name=SuperTail&value=TailContent...&created=&deleted=InTheFuture"
                )
        );
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        TestUtility.assertContains(body, new String[]{
                // NOTE: if an entity that is being searched for is without relation, then IT IS THE RELATION!
                "id=\"tails_4_related\"",  // <=-~ ( ! ) - RELATED
                "id=\"tails_4_related_buttons\"",  // <=-~ ( ! ) - WITH BUTTONS
                "value=\""+date+"\"", // Autofill for created!
                "value=\"InTheFuture\"",
                "value=\"SuperTail\"", "EntityShadowInset"
        });
        TestUtility.assertNotContains(body, new String[]{
                "id=\"tags_new_related_buttons\"", // <- Important! No new entitiy!
                "id=\"tails_4\"",
                "value=\"TailContent...\"",
                "content-length: 0",
        });
        String compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','4')\">TailContent...</textarea>");
    }

    @Test
    public void test_CRUD_saving_RELATED_tail_with_POST_WITHOUT_buttons() throws Exception
    {
        /*
          NOTE: if an entity that is being searched for is without relation, then IT IS THE RELATION!
          This is being tested below: ( 'appendRelations=false'  ==  RELATED! )
         */
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails?appendButtons=false&appendRelations=false",
                        "POST",
                        "name=SuperTail&value=TailContent...&created=&deleted=InTheFuture"
                )
        );
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        TestUtility.assertContains(body, new String[]{
            "id=\"tails_4_related\"",
            "value=\""+date+"\"", // Autofill for created!
            "value=\"InTheFuture\"",
            "value=\"SuperTail\"", "EntityShadowInset", "id=\"tails_4_related\""
        });
        TestUtility.assertNotContains(body, new String[]{
            "id=\"tails_4_buttons\"", "class=\"EntityWrapper EntityShadow row\"",
            "id=\"tags_new_related_buttons\"",
            "id=\"tails_4_related_buttons\"", // <=-~ ( ! ) - WITHOUT BUTTONS
            "id=\"tails_4\"",
            "value=\"TailContent...\"",
            "content-length: 0", "buttons", "SAVE", "DELETE", "CLEAR"
        });
        String compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','4')\">TailContent...</textarea>");
    }

    @Test
    public void test_CRUD_saving_tail_with_POST_WITHOUT_buttons_in_main_entity() throws Exception
    {
        /*
            What is meant by main entity:
            The entity that is being searched for (which is the one being saved in this test).
            It will be searched for after saving in order to build the html frontend...
            This test tries to only make the CRUD generate buttons for relations...
         */
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails?appendButtons=false&appendRelations=true",
                        "POST",
                        "name=SuperTail&value=TailContent...&created=&deleted=InTheFuture"
                )
        );
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        TestUtility.assertContains(body, new String[]{
                "class=\"EntityWrapper EntityShadow row\"",
                "id=\"tags_new_related_buttons\"", // <- 'New relations' entity inside RELATION buttons!
                "value=\""+date+"\"", // Autofill for created!
                "value=\"InTheFuture\"",
                "value=\"SuperTail\""
        });
        TestUtility.assertNotContains(body, new String[]{
                "id=\"tails_4_buttons\"", // <=-~ ( ! ) - WITHOUT BUTTONS
                "value=\"TailContent...\"",
                "content-length: 0",
        });
        String compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','4')\">TailContent...</textarea>");
    }

    @Test
    public void test_finding_tail_with_POST_via_QUICKSEARCH() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tails?searchQuickly=true",
                        "POST",
                        "name=d"
                )
        );
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        assert !body.contains("value=\""+date+"\""); // Autofill for created!
        assert !body.contains("value=\"bla bla\"");
        String compact = body.replace(" " , "");
        assert !compact.contains("oninput=\"noteOnInputFor('value','tails','1')\">blabla</textarea>");
        assert !compact.contains("name=\"deleted\"value=\"\"");
        assert !compact.contains("name=\"name\"value=\"FirstTag\"");
        assert !compact.contains("onclick=\"deleteEntity('tags','1')");
        assert !compact.contains("id=\"tags_1_description\"");
        assert !compact.contains("oninput=\"noteOnInputFor('deleted','tail_tag_relations','1')\"");
        assert !body.contains("content-length: 0");

        assert compact.contains("id=\"tails_quick_search_result\"");
        assert compact.contains("onclick=\"$('#tails_id_search_input').val('2');loadFoundForEntity('tails');$('#tails_quick_search_result').replaceWith('');\"");
        assert compact.contains("ThirdTail");
        assert compact.contains("SecondTail");
        assert compact.contains("Name");
        assert res.getContentLength()<620;
    }

    @Test
    public void test_saving_tail_with_POST_with_relations() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails",
                        "POST",
                        "id=1"
                )
        );
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        assert body.contains("value=\""+date+"\""); // Autofill for created!
        assert !body.contains("value=\"bla bla\"");
        String compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','1')\">blabla</textarea>");
        assert compact.contains("name=\"deleted\"value=\"\"");
        assert compact.contains("name=\"name\"value=\"FirstTag\"");
        assert compact.contains("deleteEntity('tails','1')");
        assert compact.contains("deleteEntity('tails',(outerID==='')?'new':outerID);");
        assert compact.contains("deleteEntity('tail_relations',(relationID==='')?'new':relationID);");
        assert compact.contains("deleteEntity('tails',(outerID==='')?'new':outerID)");
        assert compact.contains("deleteEntity('tail_relations',(relationID==='')?'new':relationID)");
        assert compact.contains("id=\"tags_1_description\"");
        assert compact.contains("oninput=\"noteOnInputFor('deleted','tail_tag_relations','1')\"");
        assert !body.contains("content-length: 0");
    }

    @Test
    public void test_saving_tail_with_POST_WITHOUT_relations() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails?appendRelations=false",
                        "POST",
                        "id=1"
                )
        );
        IResponse res = crud.handle(req);
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String body = getBody(res).toString();
        assert body.contains("value=\""+date+"\""); // Autofill for created!
        assert !body.contains("value=\"bla bla\"");
        String compact = body.replace(" " , "");
        assert compact.contains("oninput=\"noteOnInputFor('value','tails','1')\">blabla</textarea>");
        assert compact.contains("name=\"deleted\"value=\"\"");
        assert !compact.contains("name=\"name\"value=\"FirstTag\"");
        assert !compact.contains("onclick=\"deleteEntity('tags','1')");
        assert !compact.contains("id=\"tags_1_description\"");
        assert !compact.contains("oninput=\"noteOnInputFor('deleted','tail_tag_relations','1')\"");
        assert !body.contains("content-length: 0");
    }

    @Test
    public void test_CRUD_saving_tail_fails_with_POST() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");
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
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB-saving", "tailworld");
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
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tails?", "POST",
                                "name=First"// Appending relations is applied...
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
        assert compact.contains("name=\"name\"value=\"FirstTag\"");
        assert compact.contains("onclick=\"deleteEntity('tails','1')");
        assert compact.contains("deleteEntity('tags',(outerID==='')?'new':outerID);");
        assert compact.contains("deleteEntity('tail_tag_relations',(relationID==='')?'new':relationID);");
        assert compact.contains("id=\"tags_1_description\"");
        assert compact.contains("oninput=\"noteOnInputFor('deleted','tail_tag_relations','1')\"");
        assert res.getContentType().contains("text/html");
        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tails?", "POST",
                        "name=First&appendRelations=true"
                )
        );
        res = crud.handle(req);
        String otherBody = getBody(res).toString();
        assert body.length()==otherBody.length();
        double similarity = TestUtility.similarity(body, otherBody);
        assert similarity > 0.98;
        assert TestUtility.similarity("ac","rt")==0.0;
        assert TestUtility.similarity("ac","at")==0.5;
    }

    @Test
    public void test_CRUD_finding_without_relations_POST_request() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tails?", "POST",
                        "name=First&appendRelations=false"
                        // This should NOT work!^ Should be ignored... (includes relations...)
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
        assert compact.contains("name=\"name\"value=\"FirstTag\"");
        assert compact.contains("onclick=\"deleteEntity('tails','1')");
        assert compact.contains("deleteEntity('tags',(outerID==='')?'new':outerID);");
        assert compact.contains("deleteEntity('tail_tag_relations',(relationID==='')?'new':relationID);");
        assert compact.contains("id=\"tags_1_description\"");
        assert compact.contains("oninput=\"noteOnInputFor('deleted','tail_tag_relations','1')\"");
        assert res.getContentType().contains("text/html");

        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/tails?appendRelations=false", "POST",
                        "name=First"// This should NOT work! Should be ignored...
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();

        assert body.contains("200 OK");
        assert body.contains("EntityWrapper");
        assert body.contains("value=\"First Tail\"");
        assert !body.contains("value=\"Second Tail\""); // Now it does not contain second tail as relation entity!
        assert !body.contains("value=\"Third Tail\"");
        assert body.contains("oninput=\"noteOnInputFor('id','tails','1')\"");
        assert body.contains("id=\"tails_1_created\"");
        assert !body.contains("content-length: 0");
        compact = body.replace(" ", "");
        assert compact.contains("name=\"deleted\"value=\"\"");
        assert !compact.contains("name=\"name\"value=\"FirstTag\"");
        assert !compact.contains("onclick=\"deleteEntity('tags','1')");
        assert !compact.contains("id=\"tags_1_description\"");
        assert !compact.contains("oninput=\"noteOnInputFor('deleted','tail_tag_relations','1')\"");
        assert !compact.contains("deleteEntity('tags',(outerID==='')?'new':outerID);");
        assert !compact.contains("deleteEntity('tail_tag_relations',(relationID==='')?'new':relationID);");
        assert res.getContentType().contains("text/html");

    }

    @Test
    public void test_CRUD_finding_and_deleting_with_POST_request() throws Exception
    {
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");
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
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");
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
        Test_8_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld");
        String path = new File("test/db").getAbsolutePath().replace("\\","/");
        assert crud instanceof CRUD;
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:"+path+"/TestDB");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/setJDBC?db_url=THIS_IS_A_TEST_VALUE")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        String expected = "jdbc:sqlite:"+new File("storage/dbs/").getAbsolutePath()+"\\THIS_IS_A_TEST_VALUE";
        assert body.contains("JDBC url set to : '"+expected+"'");
        assert ((CRUD)crud).getURL().equals(expected);
        assert res.getContentType().contains("text/html");

        File test_file = new File("storage/dbs/THIS_IS_A_TEST_VALUE");
        assert test_file.exists();
        assert test_file.delete();
        assert !test_file.exists(); // Note : The file will be created AGAIN when switching to another database!
        // ... like right here:
        req = createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/setJDBC?db_url=jdbc:sqlite:"+path+"/TestDB"
        ));
        res = crud.handle(req);
        body = getBody(res).toString();

        /*
            ! IMPORTANT !  ...
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            The assertions below might not seem to make too much sense, however they are very important in testing
            the way in which the crud plugin switches between database files.
            These assertions below grant (especially the 'assert test_file.delete();') that the previously
            created database is being UNLOCKED (the file) when switching to the new database...
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         */
        assert test_file.exists(); // File will be created a second time by the crud plugin ! (internal reasons...)
        assert test_file.delete(); // Would not be possible if the file was still locked ! (connection not closed...)
        assert !test_file.exists();
        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

        assert body.contains("JDBC url set to : 'jdbc:sqlite:"+path+"/TestDB'");
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:"+path+"/TestDB");
        assert res.getContentType().contains("text/html");

        req = createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/setJDBC?db_url="+path+"/TestDB"// Without prefix should work too!
        ));
        res = crud.handle(req);
        body = getBody(res).toString();

        assert body.contains("JDBC url set to : 'jdbc:sqlite:"+path+"/TestDB'");
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:"+path+"/TestDB");
        assert res.getContentType().contains("text/html");

    }



    @Override
    protected Test_8_Provider createInstance() {
        return new Test_8_Provider();
    }
}
