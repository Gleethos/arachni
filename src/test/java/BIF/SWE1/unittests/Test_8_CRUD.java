package BIF.SWE1.unittests;

import BIF.SWE1.uebungen.Test8Provider;
import BIF.SWE1.uebungen.Tests7Provider;
import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Request;
import org.junit.Test;

public class Test_8_CRUD  extends AbstractTestFixture<Test8Provider> {

    @Test
    public void test_CRUD() throws Exception {

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

        assert res.getContentType().contains("text/html");

    }


    @Override
    protected Test8Provider createInstance() {
        return new Test8Provider();
    }
}
