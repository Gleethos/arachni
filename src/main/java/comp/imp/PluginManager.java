package comp.imp;

import comp.IPlugin;
import comp.IPluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PluginManager implements IPluginManager {

    Map<String, IPlugin> _plugins = new HashMap<>();
    Map<String, IPlugin> _available = new HashMap<>();

    public PluginManager(){
        _loadClasses();
        this.add("TestPlugin");
        this.add("FileReader");
        this.add("Navigator");
        this.add("TemperatureReader");
        this.add("ToLower");
    }

    private void _loadClasses(){
        //TODO: use classloader!
        ServiceLoader<IPlugin> loader = ServiceLoader.load(IPlugin.class);
        try {
            Iterator<IPlugin> instances = loader.iterator();
            while (instances.hasNext()) {
                IPlugin plugin = instances.next();
                String[] split = plugin.getClass().getName().split("\\.");
                String key = split[split.length-1];
                if(!_available.containsKey(key)){
                    _available.put(key, plugin);
                }
            }
        } catch (ServiceConfigurationError serviceError) {
            serviceError.printStackTrace();
        }
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
            _loadClasses();
            if(_available.containsKey(plugin)){
                _plugins.put(plugin, _available.get(plugin));
            } else {
                Map<String, Class> Classes = new HashMap<>();
                ClassLoader classLoader = null;
                Field f;// Class context finding!
                try {
                    f = ClassLoader.class.getDeclaredField("classes");
                    f.setAccessible(true);
                    classLoader = Thread.currentThread().getContextClassLoader();
                    try {
                        classLoader.loadClass(plugin);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    Vector<Class> classes =  (Vector<Class>) f.get(classLoader);
                    for(Class cls : classes){
                        //java.net.URL location = cls.getResource('/' + cls.getName().replace('.',
                        //        '/') + ".class");
                        //System.out.println("<p>"+location +"<p/> ... "+cls.getName());
                        Classes.put(cls.getName(), cls);
                        String[] expl = cls.getName().split("\\.");
                        Classes.put(expl[expl.length-1], cls);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Class c = Classes.get(plugin);
                Constructor constructor = c.getConstructors()[0];
                try {
                    Object instance = constructor.newInstance();
                    _plugins.put(plugin, (IPlugin) instance);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                System.out.println("");

            }
        }
    }

    @Override
    public void clear() {
        _plugins = new HashMap<>();
        _available =  new HashMap<>();
        _loadClasses();
    }
}
