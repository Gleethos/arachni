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
                "<button onclick=\"$('#sql_world_source').val('"+path+"/taleworld');\">"+value+"\\taleworld</button>",
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
        IPlugin crud = provider.getCRUDPlugin("SwitchTestDB", "taleworld"); // From taleworld...
        String path = new File("test/db").getAbsolutePath().replace("\\","/");

        assert crud instanceof CRUD;
        assert ((CRUD)crud).getURL().equals("jdbc:sqlite:"+path+"/SwitchTestDB");

        String body = getBody(crud.handle(createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/world")
        ))).toString();

        assert_default_testworld_response(false, body);

        body = getBody(crud.handle(createInstance().getRequest(
                RequestHelper.getValidRequestStream("CRUD/setJDBC?db_url=SwitchTestDB&sql_source=testworld") // ...to testworld
        ))).toString();
        path = new File("storage/dbs").getAbsolutePath();
        assert body.contains("JDBC url set to : 'jdbc:sqlite:"+path+"\\SwitchTestDB'.\n");
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
        TestUtility.check(!isTestworld, body, new String[]{"tales", "tale_relations", "tags"});// We are NOT in taleworld!
        String template =
                "<script>\n" +
                "functionset_search_parameters_for_IDENTIFIER(value){\n" +
                "if(typeofvalue==='string'||valueinstanceofString){\n";
        String template2 =
                "}else{\n" +
                "for(varainvalue){\n$('#IDENTIFIER_'+a+'_search_input').val(value[a]);\n}\n" +
                "}\n" +
                "}\n" +
                "</script>";
        TestUtility.check(isTestworld, body, new String[]{
                "<script>\n" +
                "functionset_search_parameters_for_human_attribute_relations(value){\n" +
                "if(typeofvalue==='string'||valueinstanceofString){\n" +
                "$('#human_attribute_relations_id_search_input').val(value);\n" +
                "$('#human_attribute_relations_created_search_input').val(value);\n" +
                "$('#human_attribute_relations_deleted_search_input').val(value);\n" +
                "$('#human_attribute_relations_human_id_search_input').val(value);\n" +
                "$('#human_attribute_relations_description_search_input').val(value);\n" +
                "$('#human_attribute_relations_attribute_id_search_input').val(value);\n" +
                "}else{\n" +
                "for(varainvalue){\n$('#human_attribute_relations_'+a+'_search_input').val(value[a]);\n}\n" +
                "}\n" +
                "}\n" +
                "</script>",
                template.replace("IDENTIFIER", "humans"),
                template2.replace("IDENTIFIER", "humans"),
                template.replace("IDENTIFIER", "attributes"),
                template2.replace("IDENTIFIER", "attributes"),
        });
        TestUtility.check(isTestworld, body, new String[] { // Quick search oninput events :
                // human relations :
                "id=\"human_relations_quick_search_input\"",
                "oninput=\"\n" +
                        "set_search_parameters_for_human_attribute_relations($('#human_attribute_relations_quick_search_input').val());\n" +
                        "$('#human_attribute_relations_quick_search_button').trigger('onclick');\n" +
                        "set_search_parameters_for_human_attribute_relations('');\n" +
                        "\"",
                // humans :
                "id=\"humans_quick_search_input\"",
                "oninput=\"\n" +
                        "set_search_parameters_for_humans($('#humans_quick_search_input').val());\n" +
                        "$('#humans_quick_search_button').trigger('onclick');\n" +
                        "set_search_parameters_for_humans('');\n" +
                        "\"",
                // attributes :
                "id=\"attributes_quick_search_input\"",
                "oninput=\"\n" +
                        "set_search_parameters_for_attributes($('#attributes_quick_search_input').val());\n" +
                        "$('#attributes_quick_search_button').trigger('onclick');\n" +
                        "set_search_parameters_for_attributes('');\n" +
                        "\"",
                // human attribute relations :
                "id=\"human_attribute_relations_quick_search_input\"",
                "oninput=\"\n" +
                        "set_search_parameters_for_human_attribute_relations($('#human_attribute_relations_quick_search_input').val());\n" +
                        "$('#human_attribute_relations_quick_search_button').trigger('onclick');\n" +
                        "set_search_parameters_for_human_attribute_relations('');\n" +
                        "\"",
        });
        TestUtility.check(isTestworld, body, new String[]{
                "<buttonid=\"human_attribute_relations_tab_button\"onclick=\"switchTab(event,'.HumanAttributeRelationsTab')\"",
                "<buttonid=\"human_relation_attribute_relations_tab_button\"onclick=\"switchTab(event,'.HumanRelationAttributeRelationsTab')\"",
                "<buttonid=\"attributes_tab_button\"onclick=\"switchTab(event,'.AttributesTab')\"",
                "<buttonid=\"humans_tab_button\"onclick=\"switchTab(event,'.HumansTab')\"",
                "<buttonid=\"human_relations_tab_button\"onclick=\"switchTab(event,'.HumanRelationsTab')\""
        });
        TestUtility.check(isTestworld, body, new String[] {  // Some more relevant asserts :
                //"oninput=\"noteOnInputFor('id','human_relations','new')\"",
                "onclick=\"$('#human_relation_attribute_relations_new').replaceWith('');",
                "$('#human_relations_result').append(newForm);", // This is the beginning of the code of a "new button" appending an empty entity...
        });
        TestUtility.check(true, body, new String[] {  // Global asserts :
                "array.sort(function(){return0.5-Math.random();});",
                "newForm=`<div",
                "</div>`;", // This is a snipped of the end of the code of a "new button" appending an empty entity...
                "_result').append(newForm);",
                "').join(newUID);",
                "newForm=newForm.split('"
        });
    }


    @Test
    public void saving_non_existing_table_in_testworld_returns_error_message() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");
        IRequest req = createInstance().getRequest(
                RequestHelper.getValidRequestStream(
                        "CRUD/save/tales?appendRelations=false&appendButtons=false",
                        "POST",
                        "description=TEST_DESCRIPTION&name=TEST_NAME" // 'e' is contained in all other 'name' attributes...
                )
        );
        IResponse res = crud.handle(req);
        String body = getBody(res).toString();
        assert body.contains("Cannot save entity 'tales'! : Table not found in database!");
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
                        "CRUD/find/attributes?searchQuickly=true&attribute_inner_quick_search_parameter=Mysterious",
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
                        "CRUD/find/attributes?searchQuickly=true&attributes_inner_quick_search_parameter=Mysterious",
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
    public void quick_search_with_GET_method_returns_expected_result_like_with_POST() throws Exception
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
        assert body.contains("$('#attributes_quick_search_result').replaceWith('')");
        assert body.contains("200");
    }

    @Test
    public void relational_quick_search_with_GET_method_returns_error_message_because_search_parameter_is_missing() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");

        String body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/attributes->humans?searchQuickly=true", "GET"
        )))).toString();
        assert body.contains("ERROR : Quick-Search expects relational search url to end with 'inner table'->'relation table'->'outer table'!");
        assert body.contains("ERROR : Quick-Search expects url parameter 'key_relation'!");

        assert body.contains("500");

        body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/attributes->human_attribute_relations->humans?" +
                        "searchQuickly=true&" +
                        "attributes_inner_quick_search_parameter=rie&" +
                        "humans_outer_quick_search_parameter=ean&" +
                        "key_relation=FAIL",
                "GET"
        )))).toString();
        assert !body.contains("ERROR : Quick-Search expects relational search url to end with 'inner table'->'relation table'->'outer table'!");
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
                "CRUD/find/attributes->FAIL->humans?" +
                        "searchQuickly=true&" +
                        "attributes_inner_quick_search_parameter=rie&" +
                        "humans_outer_quick_search_parameter=ean&" +
                        "key_relation=attribute_id->human_id",
                "GET"
        )))).toString();
        assert !body.contains("ERROR : Quick-Search expects url parameter 'attributes_inner_quick_search_parameter'!");
        assert !body.contains("ERROR : Quick-Search expects url parameter 'humans_outer_quick_search_parameter'!");
        assert !body.contains("ERROR : Quick-Search expects url parameter 'key_relation'!");
        assert !body.contains("ERROR : Quick-Search expects value 'FAIL' of url parameter 'key_relation' to contain '->' identifier!");
        assert body.contains("ERROR : Relation table with name 'FAIL' not found in database!");
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
                        "attributes_inner_quick_search_parameter=rie&" +
                        "humans_outer_quick_search_parameter=ean&" +
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
                "CRUD/find/attributes->FAIL->OUTER_FAIL_TABLE?" +
                        "searchQuickly=true&" +
                        "attributes_inner_quick_search_parameter=rie&" +
                        "humans_outer_quick_search_parameter=ean&" +
                        "key_relation=attribute_id->human_id",
                "GET"
        )))).toString();
        assert body.contains("500");
        assert body.contains("Cannot find entity 'OUTER_FAIL_TABLE'! : Table not found in database!");
    }

    @Test
    public void relational_quick_search_with_GET_method_returns_expected_result() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");

        String body = getBody(crud.handle(createInstance().getRequest(RequestHelper.getValidRequestStream(
                "CRUD/find/attributes->human_attribute_relations->humans?" +
                        "searchQuickly=true&" +
                        "attributes_inner_quick_search_parameter=a&" +
                        "humans_outer_quick_search_parameter=e&" +
                        "human_attribute_relations_relation_quick_search_parameter=w&" +
                        "key_relation=attribute_id->human_id",
                "GET"
        )))).toString();
        assert body.contains("200");
        assert body.contains(">Mysterious<");
        assert body.contains(">Weirdness<");
        assert body.contains(">Jup! John is mysterious. <");
        assert body.contains(">John Doe<");
        assert body.contains(">John is a weired person...<");
        assert body.contains(".replaceWith('')");
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
        assert body.contains("$('#attributes_quick_search_result').replaceWith('')");
        assert body.contains("200");
    }

    /**
     *      This test creates 50 new records.
     *      It checks if they are stored correctly and makes sure
     *      that when trying to find them the number of returned
     *      entries does not supersede certain limits for quick-search and
     *      default search requests...
     */
    @Test
    public void saving_stress_test_in_testworld_and_find_returns_not_all() throws Exception
    {
        Test_9_Provider provider = createInstance();
        IPlugin crud = provider.getCRUDPlugin("TestWorldDB", "testworld");

        for( int i=0; i<50; i++ ){
            IRequest req = createInstance().getRequest(RequestHelper.getValidRequestStream(
                    "CRUD/save/humans?appendRelations=false&appendButtons=false",
                    "POST", "value=STRESS_TEST_VALUE&name=STRESS_TEST_NAME"
            ));
            IResponse res = crud.handle(req);
            String body = getBody(res).toString();
            // However the description of the newly saved entity should be included :
            assert body.contains(">STRESS_TEST_VALUE<");
            assert body.contains("value=\"STRESS_TEST_NAME\"");
            assert body.split("STRESS_TEST_VALUE").length==2;
            assert body.split("STRESS_TEST_NAME").length==2;
            // However the name of the newly saved entity should be included :
            assert body.contains("switchTab(event, '.ContentTab')");
            assert !body.toLowerCase().contains("save");
            assert !body.toLowerCase().contains("close");
        }
        test_quick_search :
        {
            IRequest req = createInstance().getRequest(RequestHelper.getValidRequestStream(
                    "CRUD/find/humans?searchQuickly=true",
                    "POST", "name=STRESS_TEST_NAME"
            ));
            IResponse res = crud.handle(req);
            String body = getBody(res).toString();
            assert body.contains("<div class=\"col-sm-12 col-md-12 col-lg-12\"><b>... 4 more results ...</b></div></div>");
            assert body.split("STRESS_TEST_NAME").length == 47;
        }
        test_default_search :
        {
            IRequest req = createInstance().getRequest(RequestHelper.getValidRequestStream(
                    "CRUD/find/humans",
                    "POST", "name=STRESS_TEST_NAME"
            ));
            IResponse res = crud.handle(req);
            String body = getBody(res).toString();
            assert body.contains("<div class=\"col-sm-12 col-md-12 col-lg-12\"><b>... 34 more results ...</b></div>");
            assert body.split("STRESS_TEST_NAME").length == 17;
        }

    }


    @Override
    protected Test_9_Provider createInstance() {
        return new Test_9_Provider();
    }

}
