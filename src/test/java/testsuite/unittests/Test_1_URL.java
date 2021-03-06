package testsuite.unittests;

import static org.junit.Assert.*;

import testsuite.provider.Test_1_Provider;
import comp.IUrl;
import org.junit.*;

public class Test_1_URL extends AbstractTestFixture<Test_1_Provider> {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	protected Test_1_Provider createInstance() {
		return new Test_1_Provider();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void helloWorld() throws Exception {
		Test_1_Provider ueb = createInstance();
		ueb.helloWorld();
	}

	@Test
	public void url_should_create_empty() {
		IUrl obj = createInstance().getUrl(null);
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEmptyOrNull(obj.getPath());
	}

	@Test
	public void url_should_return_raw_url_0() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals("/test_CRUD.jpg", obj.getRawUrl());
	}

	@Test
	public void url_should_return_raw_url_1() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg?x=y");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals("/test_CRUD.jpg?x=y", obj.getRawUrl());
	}

	@Test
	public void url_should_return_raw_url_2() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg?x=1&y=2");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals("/test_CRUD.jpg?x=1&y=2", obj.getRawUrl());
	}

	@Test
	public void url_should_create_with_path() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals("/test_CRUD.jpg", obj.getPath());
	}

	@Test
	public void url_should_parse_parameter() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg?x=1");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertNotNull(obj.getParameter().get("x"));
		assertEquals("1", obj.getParameter().get("x"));
	}

	@Test
	public void url_should_parse_more_parameter() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg?x=1&y=2");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertNotNull(obj.getParameter().get("x"));
		assertEquals("1", obj.getParameter().get("x"));
		
		assertNotNull(obj.getParameter().get("y"));
		assertEquals("2", obj.getParameter().get("y"));
	}

	@Test
	public void url_should_parse_return_path_without_parameter() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg?x=1");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals("/test_CRUD.jpg", obj.getPath());
	}
	
	@Test
	public void url_should_count_parameter() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg?x=7");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals(1, obj.getParameterCount());
	}
	
	@Test
	public void url_should_count_parameter_2() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg?x=7&y=foo");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals(2, obj.getParameterCount());
	}
	
	@Test
	public void url_should_count_parameter_0() {
		IUrl obj = createInstance().getUrl("/test_CRUD.jpg");
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals(0, obj.getParameterCount());
	}
	
	@Test
	public void url_should_count_parameter_empty() {
		IUrl obj = createInstance().getUrl(null);
		assertNotNull("UEB1.GetUrl returned null", obj);

		assertEquals(0, obj.getParameterCount());
	}
}
