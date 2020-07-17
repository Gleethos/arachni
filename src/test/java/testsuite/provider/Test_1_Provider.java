package testsuite.provider;

import comp.IUrl;
import comp.imp.Url;

public class Test_1_Provider {

	public IUrl getUrl(String path) {
		return new Url(path);
	}

	public void helloWorld() {
	}
}
