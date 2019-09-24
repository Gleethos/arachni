package comp;

import java.lang.Iterable;

public interface IPluginManager {
    /**
     * Returns a list of all plugins. Never returns null.
     * TODO: Refactor to List<component.IPlugin>, Enumeration is deprecated
     * @return
     */
	Iterable<IPlugin> getPlugins();
	
	
    /**
     * Adds a new plugin. If the plugin was already added, nothing will happen.
     * @param plugin
     */
    void add(IPlugin plugin);
    
    /**
     * Adds a new plugin by class name. If the plugin was already added, nothing will happen.
     * Throws an exeption, when the type cannot be resoled or the class does not implement component.IPlugin.
     * @param plugin
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    void add(String plugin) throws Exception;


    
    /**
     * Clears all plugins
     */
    void clear();
}
