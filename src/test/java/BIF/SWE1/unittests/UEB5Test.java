package BIF.SWE1.unittests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import comp.IPlugin;
import comp.IPluginManager;
import comp.IRequest;
import comp.IResponse;
import uebungen.UEB5;

/* Placeholder */
public class UEB5Test extends AbstractTestFixture<UEB5> {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	protected UEB5 createInstance() {
		return new UEB5();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void HelloWorld() throws Exception {
		UEB5 ueb = createInstance();
		ueb.helloWorld();
	}

	/********************* Helper **********************/

	private IPlugin selectPlugin(IPluginManager mgr, IRequest req) {
		IPlugin plugin = null;
		float max = 0;
		for (IPlugin p : mgr.getPlugins()) {
			float canHandle = p.canHandle(req);
			if (canHandle > max) {
				max = canHandle;
				plugin = p;
			}
		}

		return plugin;
	}

	/********************* Milestone 2 **********************/
	/*
	 * Der WebServer kann Plugins laden und benutzen (keine hardcodierten
	 * Stellen im Code mehr) Das statische Dateien Plugin funktioniert (und kann
	 * z.B. die Startseite ausliefern) erste Unittests wurden implementiert bei
	 * einem weiteren Plugin ist ein deutlicher Fortschritt zu sehen
	 */

	@Test
	public void milestone2_return_main_page() throws Exception {
		UEB5 ueb = createInstance();

		IPluginManager mgr = ueb.getPluginManager();
		assertNotNull("UEB5.getPluginManager returned null", mgr);

		IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream("/"));
		assertNotNull("UEB5.GetRequest returned null", req);

		IPlugin plugin = selectPlugin(mgr, req);
		assertNotNull("No plugin found to server the '/' request", plugin);

		IResponse resp = plugin.handle(req);
		assertNotNull(resp);
		assertEquals(200, resp.getStatusCode());
		assertTrue(resp.getContentLength() > 0);
		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		try {
			resp.send(ms);
			assertTrue(ms.size() > 0);
		} finally {
			ms.close();
		}
	}

	@Test
	public void milestone2_return_error_on_invalid_url() throws Exception {
		UEB5 ueb = createInstance();

		IPluginManager mgr = ueb.getPluginManager();
		assertNotNull("UEB5.getPluginManager returned null", mgr);

		IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream("/i_am_a_unknown_url.html"));
		assertNotNull("UEB5.GetRequest returned null", req);

		IPlugin plugin = selectPlugin(mgr, req);

		if (plugin != null) {
			IResponse resp = plugin.handle(req);
			assertNotNull(resp);
			assertTrue(resp.getStatusCode() != 200);
		} else {
			// No plugin will handle unknown URL
		}
	}

	/********************** PluginManager *********************************/
	@Test
	public void pluginmanager_return_all_plugins() {
		IPluginManager obj = createInstance().getPluginManager();
		assertNotNull("UEB5.GetPluginManager returned null", obj);
		assertNotNull(obj.getPlugins());
		int count = 0;
		Iterator<IPlugin> i = obj.getPlugins().iterator();
		while (i.hasNext()) {
			count++;
			i.next();
		}
		assertTrue(count >= 4);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void pluginmanager_all_plugins_have_unique_type() {
		IPluginManager obj = createInstance().getPluginManager();
		assertNotNull("UEB5.GetPluginManager returned null", obj);
		assertNotNull(obj.getPlugins());
		Set<Class> _typeMap = new HashSet<Class>();
		for (IPlugin p : obj.getPlugins()) {
			assertNotNull(p);
			Class t = p.getClass();
			assertFalse(_typeMap.contains(t));
			_typeMap.add(t);
		}
	}

	@Test
	public void pluginmanager_contains_plugin_for_start_page() throws Exception {
		UEB5 ueb = createInstance();
		IPluginManager obj = ueb.getPluginManager();
		assertNotNull("UEB5.GetPluginManager returned null", obj);
		IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream("/"));
		assertNotNull("UEB5.GetRequest returned null", req);

		assertNotNull(obj.getPlugins());
		IPlugin plugin = selectPlugin(obj, req);
		assertNotNull(plugin);
		IResponse resp = plugin.handle(req);
		assertNotNull(resp);
		assertEquals(200, resp.getStatusCode());
		assertTrue(resp.getContentLength() > 0);
	}

	@Test
	public void pluginmanager_should_add_plugin() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		IPluginManager obj = createInstance().getPluginManager();
		assertNotNull("UEB5.GetPluginManager returned null", obj);
		assertNotNull(obj.getPlugins());
		long count = StreamSupport.stream(obj.getPlugins().spliterator(), false).count();
		try {
			obj.add("BIF.SWE1.unittests.mocks.Ueb5TestPlugin");
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(count + 1, StreamSupport.stream(obj.getPlugins().spliterator(), false).count());
		boolean found = false;
		for (IPlugin p : obj.getPlugins()) {
			if (p instanceof BIF.SWE1.unittests.mocks.Ueb5TestPlugin)
				found = true;
		}

		assertTrue("New plugin was not found.", found);
	}

	@Test
	public void pluginmanager_should_fail_adding_non_existing_plugin() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		IPluginManager obj = createInstance().getPluginManager();
		assertNotNull("UEB5.GetPluginManager returned null", obj);
		assertNotNull(obj.getPlugins());
		boolean thrown = false;
		try {
			obj.add("BIF.SWE1.unittests.mocks.Ueb999TestPlugin");
		} catch (Exception e) {
			thrown = true;
		}
		assertTrue("No exception was thrown", thrown);
	}
	
	@Test
	public void pluginmanager_should_fail_adding_plugin_not_implementing_plugin() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		IPluginManager obj = createInstance().getPluginManager();
		assertNotNull("UEB5.GetPluginManager returned null", obj);
		assertNotNull(obj.getPlugins());
		boolean thrown = false;
		try {
			obj.add("BIF.SWE1.unittests.mocks.Ueb5NoTestPlugin");
		} catch (Exception e) {
			thrown = true;
		}
		assertTrue("No exception was thrown", thrown);
	}

	/********************** Static File Plugin *********************************/
	private final static String static_file_content = "Hello World!";

	private void setupStaticFilePlugin(UEB5 ueb, String fileName) throws FileNotFoundException {
		final File folder = new File("tmp-static-files");

		if (!folder.exists()) {
			folder.mkdirs();
		}

		try (PrintWriter out = new PrintWriter(new File(folder, fileName).getPath())) {
			out.write(static_file_content);
		}

		ueb.setStatiFileFolder(folder.getName());
	}

	@Test
	public void staticfileplugin_hello_world() throws FileNotFoundException {
		UEB5 ueb = createInstance();
		setupStaticFilePlugin(ueb, "foo.txt");

		IPlugin obj = ueb.getStaticFilePlugin();
		assertNotNull("UEB5.getStaticFilePlugin returned null", obj);

		String url = ueb.getStaticFileUrl("bar.txt");
		assertNotNull("IUEB5.GetStaticFileUrl returned null", url);
		assertTrue(url.endsWith("bar.txt"));
	}

	@Test
	public void staticfileplugin_return_file() throws Exception {
		String fileName = String.format("foo-%s.txt", UUID.randomUUID());
		UEB5 ueb = createInstance();
		setupStaticFilePlugin(ueb, fileName);

		IPlugin obj = ueb.getStaticFilePlugin();
		assertNotNull("UEB5.getStaticFilePlugin returned null", obj);

		String url = ueb.getStaticFileUrl(fileName);
		assertNotNull("IUEB5.GetStaticFileUrl returned null", url);

		IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url));
		assertNotNull("UEB5.GetRequest returned null", req);

		float canHandle = obj.canHandle(req);
		assertTrue(canHandle > 0 && canHandle <= 1);

		IResponse resp = obj.handle(req);
		assertNotNull(resp);
		assertEquals(200, resp.getStatusCode());
	}

	@Test
	public void staticfileplugin_return_file_content() throws Exception {
		String fileName = String.format("foo-%s.txt", UUID.randomUUID());
		UEB5 ueb = createInstance();
		setupStaticFilePlugin(ueb, fileName);

		IPlugin obj = ueb.getStaticFilePlugin();
		assertNotNull("UEB5.getStaticFilePlugin returned null", obj);

		String url = ueb.getStaticFileUrl(fileName);
		assertNotNull("IUEB5.GetStaticFileUrl returned null", url);

		IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url));
		assertNotNull("UEB5.GetRequest returned null", req);

		float canHandle = obj.canHandle(req);
		assertTrue(canHandle > 0 && canHandle <= 1);

		IResponse resp = obj.handle(req);
		assertNotNull(resp);
		assertEquals(200, resp.getStatusCode());
		assertEquals(static_file_content.length(), resp.getContentLength());

		ByteArrayOutputStream ms = new ByteArrayOutputStream();

		try {
			resp.send(ms);
			BufferedReader sr = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ms.toByteArray()), "UTF-8"));
			String line;
			String lastLine = null;
			while ((line = sr.readLine()) != null) {
				lastLine = line;
			}
			assertEquals(static_file_content, lastLine);
		} finally {
			ms.close();
		}
	}

	@Test
	public void staticfileplugin_fail_on_missing_file() throws Exception {
		String fileName = String.format("foo-%s.txt", UUID.randomUUID());
		UEB5 ueb = createInstance();
		setupStaticFilePlugin(ueb, fileName);

		IPlugin obj = ueb.getStaticFilePlugin();
		assertNotNull("UEB5.getStaticFilePlugin returned null", obj);

		String url = ueb.getStaticFileUrl("missing-" + fileName);
		assertNotNull("IUEB5.GetStaticFileUrl returned null", url);

		IRequest req = ueb.getRequest(RequestHelper.getValidRequestStream(url));
		assertNotNull("UEB5.GetRequest returned null", req);

		float canHandle = obj.canHandle(req);
		if (canHandle > 0) {

			IResponse resp = obj.handle(req);
			assertNotNull(resp);
			assertEquals(404, resp.getStatusCode());
		} else {
			// static file plugin will not handle missing files.
		}
	}
}
