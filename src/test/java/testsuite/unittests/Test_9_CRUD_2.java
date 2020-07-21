package testsuite.unittests;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.plugins.CRUD;
import org.junit.Test;
import testsuite.TestUtility;
import testsuite.provider.Test_9_Provider;

import java.io.File;

public class Test_9_CRUD_2 extends AbstractTestFixture<Test_9_Provider> {

    @Test
    public void test_CRUD_response_in_testworld() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/world")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        assert_default_testworld_response(true, body);
    }

    @Test
    public void test_switching_worlds_via_setJDBC() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestDB", "tailworld"); // From tailworld...
        String path = new File("test/db").getAbsolutePath().replace("\\","/");

        assert crud instanceof CRUD;
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:"+path+"/TestDB");

        String body = getBody(crud.handle(createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/world")
        ))).toString();

        assert_default_testworld_response(false, body);

        body = getBody(crud.handle(createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/setJDBC?db_url=TestWorldDB&sql_source=testworld") // ...to testworld
        ))).toString();

        assert body.contains("JDBC url set to : 'jdbc:sqlite:D:\\development\\java\\studium\\arachni\\storage\\dbs\\TestWorldDB'.\n");
        assert body.contains("SQL source set to : 'testworld'.");

        body = getBody(crud.handle(createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/world")
        ))).toString();

        assert_default_testworld_response(true, body);
    }

    private void assert_default_testworld_response(boolean isTestworld, String body)
    {
        body = body.replace(" ", "");
        assert body.contains("text/html");
        TestUtility.check(isTestworld, body, new String[]{ // Contains all tables by name!
                "humans", "human_relations", "attributes", "human_attribute_relations"
        });
        TestUtility.check(!isTestworld, body, new String[]{"tails", "tail_relations", "tags"});// We are NOT in tailworld!
        TestUtility.check(isTestworld, body, new String[] { // Quick search oninput events :
                // human relations :
                "id=\"human_relations_quick_search_input\"",
                "oninput=\"$('#human_relations_id_search_input').val($('#human_relations_quick_search_input').val());\n" +
                        "$('#human_relations_created_search_input').val($('#human_relations_quick_search_input').val());\n" +
                        "$('#human_relations_deleted_search_input').val($('#human_relations_quick_search_input').val());\n" +
                        "$('#human_relations_description_search_input').val($('#human_relations_quick_search_input').val());\n" +
                        "$('#human_relations_superior_human_id_search_input').val($('#human_relations_quick_search_input').val());\n" +
                        "$('#human_relations_inferior_human_id_search_input').val($('#human_relations_quick_search_input').val());\n" +
                        "loadQuickSearchForEntity('human_relations');\"",
                // humans :
                "id=\"humans_quick_search_input\"",
                "oninput=\"$('#humans_id_search_input').val($('#humans_quick_search_input').val());\n" +
                        "$('#humans_name_search_input').val($('#humans_quick_search_input').val());\n" +
                        "$('#humans_value_search_input').val($('#humans_quick_search_input').val());\n" +
                        "$('#humans_created_search_input').val($('#humans_quick_search_input').val());\n" +
                        "$('#humans_deleted_search_input').val($('#humans_quick_search_input').val());\n" +
                        "loadQuickSearchForEntity('humans');\"",
                // attributes :
                "id=\"attributes_quick_search_input\"",
                "oninput=\"$('#attributes_id_search_input').val($('#attributes_quick_search_input').val());\n" +
                        "$('#attributes_name_search_input').val($('#attributes_quick_search_input').val());\n" +
                        "$('#attributes_created_search_input').val($('#attributes_quick_search_input').val());\n" +
                        "$('#attributes_deleted_search_input').val($('#attributes_quick_search_input').val());\n" +
                        "$('#attributes_description_search_input').val($('#attributes_quick_search_input').val());\n" +
                        "loadQuickSearchForEntity('attributes');\"",
                // human attribute relations :
                "id=\"human_attribute_relations_quick_search_input\"",
                "oninput=\"$('#human_attribute_relations_id_search_input').val($('#human_attribute_relations_quick_search_input').val());\n" +
                        "$('#human_attribute_relations_created_search_input').val($('#human_attribute_relations_quick_search_input').val());\n" +
                        "$('#human_attribute_relations_deleted_search_input').val($('#human_attribute_relations_quick_search_input').val());\n" +
                        "$('#human_attribute_relations_human_id_search_input').val($('#human_attribute_relations_quick_search_input').val());\n" +
                        "$('#human_attribute_relations_description_search_input').val($('#human_attribute_relations_quick_search_input').val());\n" +
                        "$('#human_attribute_relations_attribute_id_search_input').val($('#human_attribute_relations_quick_search_input').val());\n" +
                        "loadQuickSearchForEntity('human_attribute_relations');\"",
        });
        TestUtility.check(isTestworld, body, new String[]{
                "<buttononclick=\"switchTab(event,'.HumanAttributeRelationsTab')\"",
                "<buttononclick=\"switchTab(event,'.HumanRelationAttributeRelationsTab')\"",
                "<buttononclick=\"switchTab(event,'.AttributesTab')\"",
                "<buttononclick=\"switchTab(event,'.HumansTab')\"",
                "<buttononclick=\"switchTab(event,'.HumanRelationsTab')\""
        });
        TestUtility.check(isTestworld, body, new String[] {  // Some more relevant asserts :
                "oninput=\"noteOnInputFor('id','human_relations','new')\"",
                "onclick=\"$('#human_relation_attribute_relations_new').replaceWith('');",
                "$('#human_relations_result').append(`", // This is the beginning of the code of a "new button" appending an empty entity...
        });
        TestUtility.check(true, body, new String[] {  // Global asserts :
                "</div>`);}</script>" // This is a snipped of the end of the code of a "new button" appending an empty entity...
        });
    }



    @Override
    protected Test_9_Provider createInstance() {
        return new Test_9_Provider();
    }
}
