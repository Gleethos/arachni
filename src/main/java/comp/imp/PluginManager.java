package comp.imp;

import comp.IPlugin;
import comp.IPluginManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class PluginManager implements IPluginManager {

    Map<String, IPlugin> _plugins = new HashMap<>();
    Map<String, IPlugin> _available = new HashMap<>();

    public PluginManager(){
        this.add("TestPlugin");
        this.add("FileReader");
        this.add("Navigator");
        this.add("TemperatureReader");
        this.add("ToLower");
        this.add("Oracle");
    }

    public IPlugin get(String name){
        return _plugins.get(name);
    }

    @Override
    public Iterable<IPlugin> getPlugins() {
        return _plugins.values();
    }

    @Override
    public void add(IPlugin plugin) {
        if(!_plugins.containsKey(plugin.getClass().getName())){
            _plugins.put(plugin.getClass().getName(), plugin);
        }
    }

    @Override
    public void add(String plugin){
        if(!_plugins.containsKey(plugin)){
            try {
                if(!loadPlugin(plugin,"build/classes/java/main/comp/imp/plugins")){
                    if(!loadPlugin(plugin,"build/classes/java/test/BIF/SWE1/unittests/mocks")){
                        throw new IllegalStateException("Plugin not found!");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void clear() {
        _plugins = new HashMap<>();
        _available =  new HashMap<>();
    }

    public boolean loadPlugin(String pluginName, String packagePath) throws MalformedURLException, IOException, ClassNotFoundException {

        File location = new File(packagePath);
        String packagePraefix = _extractPackagePrefix(location.getPath());
        File pluginLocations[] = location.listFiles((File file)->file.getName().endsWith(".jar")||file.getName().endsWith(".class"));
        URL url = null;
        try {
            url = location.toURI().toURL();
        } catch (MalformedURLException e) {

        }
        URL[] urls = new URL[]{ url };
        URLClassLoader classLoader = new URLClassLoader(urls);
        try {
            for(int i=0; i<pluginLocations.length; i++){
                String[] expl = pluginLocations[i].toPath().getFileName().toString().split("\\.");
                String name = expl[0];
                expl = pluginName.split("\\.");
                pluginName = expl[expl.length-1];
                if(name.equals(pluginName)){
                    IPlugin target =
                            (IPlugin)classLoader.loadClass(packagePraefix+name).newInstance();
                    _plugins.put(name, target);
                    try {
                        classLoader.close();
                    } catch (IOException e) {

                    }
                    return true;
                }
            }
        } catch (Exception e) {

        }
        try {
            classLoader.close();
        } catch (IOException e) {

        }
        return false;
    }

    private String _extractPackagePrefix(String path){
        String result = "";
        String[] parts = path.replace("\\", "/").split("/");
        boolean javaFound = false;
        for(String part : parts){
            result += (javaFound&&!part.equals("main")&&!part.equals("test"))
                    ?part+"."
                    :"";
            javaFound = (part.equals("java"))?true:javaFound;
        }
        return result;
    }




}
