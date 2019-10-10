package BIF.SWE1.uebungen;

import java.io.InputStream;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Request;
import comp.imp.Response;
import comp.imp.plugins.FileReader;
import comp.imp.plugins.TestPlugin;

public class UEB3 {

	public void helloWorld() {

	}

	public IRequest getRequest(InputStream inputStream) {
		return new Request(inputStream);
	}

	public IResponse getResponse() {
		return new Response();
	}

	public IPlugin getTestPlugin() {
		return new TestPlugin();//FileReader();//TestPlugin();
	}
}
