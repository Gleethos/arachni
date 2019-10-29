package BIF.SWE1.uebungen;

import java.io.InputStream;

import comp.IPlugin;
import comp.IPluginManager;
import comp.IRequest;
import comp.imp.PluginManager;
import comp.imp.Request;
import comp.imp.plugins.FileReader;
import comp.imp.Url;

public class UEB5 {

	String _path = "";

	public void helloWorld() {

	}

	public IRequest getRequest(InputStream inputStream) {
		return new Request(inputStream);
	}

	public IPluginManager getPluginManager() {
		return new PluginManager();
	}

	public IPlugin getStaticFilePlugin() {
		return new FileReader();
	}

	public void setStaticFileFolder(String s) {
		_path = s;
	}

	public String getStaticFileUrl(String s) {
		return _path+"/"+s;
	}
}
