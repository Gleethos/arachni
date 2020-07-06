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
        assert body.contains(").children('input').each(function () {");
        assert body.contains("text/html");
        assert body.contains("load");
        assert body.contains("tails");
        assert res.getContentType().contains("text/html");
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
        assert body.contains("id=\"id_1\"");
        assert body.contains("id=\"child_tail_id_1\"");
        assert body.contains("id=\"description_1\"");
        assert !body.contains("id=\"\"");
        assert !body.contains("id=\"description_2\"");
        assert !body.contains("id=\"id_2\"");
        String compact = body.replace(" ", "");
        assert compact.replace(" ","").contains("<spanvalue=\"0\"");
        assert compact.replace(" ", "").contains("oninput=\"noteOnInputFor('id','tail_relations'");
        assert compact.contains("col-sm-");
        assert compact.contains("col-md-");
        assert compact.contains("col-lg-4");
        assert compact.contains("col-sm-12");

        assert res.getContentType().contains("text/html");
    }

    @Test
    public void test_CRUD_saving() throws Exception
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
        assert body.contains("id=\"id_1\"");
        assert body.contains("id=\"child_tail_id_1\"");
        assert body.contains("id=\"description_1\"");
        assert !body.contains("id=\"\"");
        assert !body.contains("id=\"description_2\"");
        assert !body.contains("id=\"id_2\"");
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

        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails?" +
                                "name=MyLittleTail&value=TailContentJadida&created=AlsoToday&deleted=AlsoTomorrow"
                )
        );
        res = crud.handle(req);
        body = getBody(res).toString();
        assert body.contains("value=\"MyLittleTail\"");
        assert body.contains("value=\"TailContentJadida\"");
        assert body.contains("value=\"AlsoToday\"");
        assert body.contains("value=\"AlsoTomorrow\"");
        assert body.contains("id=\"name_3\"");
        assert body.contains("id=\"id_3\"");
        assert body.contains("oninput=\"noteOnInputFor('name','tails','3')");
        assert body.contains("id=\"tails_3\"");
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
