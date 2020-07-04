package BIF.SWE1.unittests;

import BIF.SWE1.uebungen.Test8Provider;
import BIF.SWE1.uebungen.Tests7Provider;
import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Request;
import comp.imp.plugins.CRUD;
import org.junit.Test;

public class Test_8_CRUD  extends AbstractTestFixture<Test8Provider> {

    @Test
    public void test_CRUD_search_fields() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin();
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/search")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("200 OK");
        assert body.contains("$('#'+selector+'_result').html(\"\").load('CRUD/find/in/'+selector+'?'+param.join('&'));\n");
        assert body.contains("<button onclick=");
        assert body.contains("_search').children('input').each(function () {");
        assert body.contains("text/html");
        assert body.contains("load");
        assert body.contains("tails");
        assert body.contains("id=\"tail_relations_search\"");

        assert res.getContentType().contains("text/html");
    }

    @Test
    public void test_CRUD_finding() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin();
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/in/tail_relations?" +
                                "id%20INTEGER%20NOT%20NULL%20PRIMARY%20KEY%20AUTOINCREMENT=1&" +
                                "parent_tail_id%20INTEGER%20NOT%20NULL=&child_tail_id%20INTEGER%20NOT%20NULL=&" +
                                "description%20TEXT%20NOT%20NULL=&created%20TEXT%20NOT%20NULL=&deleted%20TEXT="
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("200 OK");
        assert body.contains("parent_tail_id");
        assert body.contains("child_tail_id");
        assert body.contains("<input");
        assert body.contains("<span");

        assert res.getContentType().contains("text/html");
    }

    @Test
    public void test_CRUD_setJDBC() throws Exception
    {
        Test8Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin();
        assert crud instanceof CRUD;
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:C:/sqlite/db/TestDB");

        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/setJDBC?url=THIS_IS_A_TEST_VALUE")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert body.contains("JDBC url set to : 'THIS_IS_A_TEST_VALUE'");
        assert ((CRUD)crud).getURL().equals("THIS_IS_A_TEST_VALUE");
        assert res.getContentType().contains("text/html");

        req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/setJDBC?url=jdbc:sqlite:C:/sqlite/db/TestDB")
        );
        res = crud.handle(req);
        body = getBody(res).toString();

        assert body.contains("JDBC url set to : 'jdbc:sqlite:C:/sqlite/db/TestDB'");
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:C:/sqlite/db/TestDB");
        assert res.getContentType().contains("text/html");
    }



    @Override
    protected Test8Provider createInstance() {
        return new Test8Provider();
    }
}
