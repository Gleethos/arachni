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
    public void test_view_CRUD_response() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/view")
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        String path = new File("storage/dbs").getAbsolutePath().replace("\\", "/");
        String value = new File("storage/dbs").getAbsolutePath();
        TestUtility.assertContains(body, new String[]{
                "<button onclick=\"$('#jdbc_world_url').val('"+path+"/TestWorldDB');\">"+value+"\\TestWorldDB</button>"
        });
        path = new File("storage/sql").getAbsolutePath().replace("\\", "/");
        value = new File("storage/sql").getAbsolutePath();
        TestUtility.assertContains(body, new String[]{
                "<button onclick=\"$('#sql_world_source').val('"+path+"/tailworld');\">"+value+"\\tailworld</button>",
                "<button onclick=\"$('#sql_world_source').val('"+path+"/temperature');\">"+value+"\\temperature</button>",
                "<button onclick=\"$('#sql_world_source').val('"+path+"/testworld');\">"+value+"\\testworld</button>"
        });

    }

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


    @Test
    public void saving_non_existing_table_in_testworld_returns_error_message() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tails?appendRelations=false&appendButtons=false",
                        "POST",
                        "description=TEST_DESCRIPTION&name=TEST_NAME" // 'e' is contained in all other 'name' attributes...
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("Cannot save entity 'tails'! : Table not found in database!");
    }

    /**
     * This test addresses a bug that occurred when saving entities with entry values similar to
     * present values in other entities.
     * The save method inside the crud plugin uses the find method after saving in order to return
     * a representation of the saved entity.
     * however the find method would than return many entities instead of the one which was requested to be saved...
     * @throws Exception
     */
    @Test
    public void saving_in_testworld_returns_only_saved() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/attributes?appendRelations=false&appendButtons=false",
                        "POST",
                        "description=MANGO_SHAKE&name=e" // 'e' is contained in all other 'name' attributes...
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();

        // Should not contain descriptions of other attributes :
        assert !body.contains("A kind of relationship...");
        assert !body.contains("Also a kind of relationship...");
        assert !body.contains("Not so nice...");
        assert !body.contains("...  O.o  ...");

        // However the description of the newly saved entity should be included :
        assert body.contains("value=\"MANGO_SHAKE\"");

        // Should NOT contain names of other attributes :
        assert !body.contains("Friendship");
        assert !body.contains("Weirdness");
        assert !body.contains("Enemies");
        assert !body.contains("Mysterious");

        // However the name of the newly saved entity should be included :

        assert body.contains("value=\"e\"");
        assert body.contains("id=\"attributes_5_related\"");
        assert body.contains("id=\"attributes_5_deleted_span\"");
        assert body.contains("class=\"AttributesDeleted\"");
        assert !body.toLowerCase().contains("save");
        assert !body.toLowerCase().contains("close");
    }

    @Test
    public void quick_search_with_POST_method_ignores_url_search_parameter_and_return_expected_result() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/attributes?searchQuickly=true&attribute_quick_search_parameter=Mysterious",
                        "POST",
                        "name=Friendship"
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("id=\"attributes_quick_search_result\"");
        assert body.contains("Friendship");
        assert !body.contains("Mysterious");
    }

    @Test
    public void quick_search_with_GET_method_uses_search_parameter_and_return_expected_result() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/attributes?searchQuickly=true&attributes_quick_search_parameter=Mysterious",
                        "GET",
                        "name=Friendship"
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("id=\"attributes_quick_search_result\"");
        assert body.contains("Mysterious");
        assert !body.contains("Friendship");
    }

    @Test
    public void quick_search_with_GET_method_returns_error_message_because_search_parameter_is_missing() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/attributes?searchQuickly=true",
                        "GET"
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("ERROR : Quick-Search expects url parameter 'attributes_quick_search_parameter'!");
        assert body.contains("500");
    }

    @Test
    public void relational_quick_search_with_GET_method_returns_error_message_because_search_parameter_is_missing() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");

        String body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/attributes->humans?searchQuickly=true", "GET"
        )))).toString();
        assert body.contains("ERROR : Quick-Search expects url parameter 'attributes_quick_search_parameter'!");
        assert body.contains("ERROR : Quick-Search expects url parameter 'humans_quick_search_parameter'!");
        assert body.contains("ERROR : Quick-Search expects url parameter 'relation_table_name'!");
        assert body.contains("ERROR : Quick-Search expects url parameter 'key_relation'!");
        assert body.contains("500");

        body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/attributes->humans?" +
                        "searchQuickly=true&" +
                        "attributes_quick_search_parameter=rie&" +
                        "humans_quick_search_parameter=ean&" +
                        "relation_table_name=human_attribute_relations&" +
                        "key_relation=FAIL",
                "GET"
        )))).toString();
        assert !body.contains("ERROR : Quick-Search expects url parameter 'attributes_quick_search_parameter'!");
        assert !body.contains("ERROR : Quick-Search expects url parameter 'humans_quick_search_parameter'!");
        assert !body.contains("ERROR : Quick-Search expects url parameter 'relation_table_name'!");
        assert !body.contains("ERROR : Quick-Search expects url parameter 'key_relation'!");
        assert body.contains("ERROR : Quick-Search expects value 'FAIL' of url parameter 'key_relation' to contain '->' identifier!");
        assert body.contains("500");
    }

    @Test
    public void relational_quick_search_with_GET_method_returns_error_message_because_relation_table_unknown() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");

        String body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/attributes->humans?" +
                        "searchQuickly=true&" +
                        "attributes_quick_search_parameter=rie&" +
                        "humans_quick_search_parameter=ean&" +
                        "relation_table_name=FAIL&" +
                        "key_relation=attribute_id->human_id",
                "GET"
        )))).toString();
        assert !body.contains("ERROR : Quick-Search expects url parameter 'attributes_quick_search_parameter'!");
        assert !body.contains("ERROR : Quick-Search expects url parameter 'humans_quick_search_parameter'!");
        assert !body.contains("ERROR : Quick-Search expects url parameter 'relation_table_name'!");
        assert !body.contains("ERROR : Quick-Search expects url parameter 'key_relation'!");
        assert !body.contains("ERROR : Quick-Search expects value 'FAIL' of url parameter 'key_relation' to contain '->' identifier!");
        assert body.contains("ERROR : Relation table with name 'FAIL' not found!");
        assert body.contains("500");
    }

    @Test
    public void relational_quick_search_with_GET_method_returns_error_message_because_table_unknown() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");

        String body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/FAIL_TABLE->humans?" +
                        "searchQuickly=true&" +
                        "attributes_quick_search_parameter=rie&" +
                        "humans_quick_search_parameter=ean&" +
                        "relation_table_name=FAIL&" +
                        "key_relation=attribute_id->human_id",
                "GET"
        )))).toString();
        assert body.contains("500");
        assert body.contains("Cannot find entity 'FAIL_TABLE'! : Table not found in database!");
    }

    @Test
    public void relational_quick_search_with_GET_method_returns_error_message_because_outer_table_unknown() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");

        String body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/attributes->OUTER_FAIL_TABLE?" +
                        "searchQuickly=true&" +
                        "attributes_quick_search_parameter=rie&" +
                        "humans_quick_search_parameter=ean&" +
                        "relation_table_name=FAIL&" +
                        "key_relation=attribute_id->human_id",
                "GET"
        )))).toString();
        assert body.contains("500");
        assert body.contains("Cannot find entity 'OUTER_FAIL_TABLE'! : Table not found in database!");
    }

    //@Test
    public void relational_quick_search_with_GET_method_returns_expected_result() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");

        String body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/attributes->humans?" +
                        "searchQuickly=true&" +
                        "attributes_quick_search_parameter=rie&" +
                        "humans_quick_search_parameter=ean&" +
                        "relation_table_name=human_attribute_relations&" +
                        "key_relation=attribute_id->human_id",
                "GET"
        )))).toString();
        assert body.contains("200");
        //TODO!
        //assert body.contains("Cannot find entity 'OUTER_FAIL_TABLE'! : Table not found in database!");
    }

    @Test
    public void quick_search_with_GET_method_returns_expected_result() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/find/attributes?searchQuickly=true&attributes_search_parameter=ne",
                        "GET"
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("ERROR : Quick-Search expects url parameter 'attributes_quick_search_parameter'!");
        assert body.contains("500");
    }

    @Override
    protected Test_9_Provider createInstance() {
        return new Test_9_Provider();
    }

}
