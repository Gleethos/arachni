package BIF.SWE1.uebungen;

import java.io.InputStream;

import comp.IRequest;
import comp.IResponse;
import comp.IUrl;
import comp.imp.Request;
import comp.imp.Response;
import comp.imp.Url;

public class UEB2 {

	public void helloWorld() {

	}

	public IUrl getUrl(String s) {
		return new Url(s);
	}

	public IRequest getRequest(InputStream inputStream) {
		return new Request(inputStream);
	}

	public IResponse getResponse() {
		return new Response();
	}
}
