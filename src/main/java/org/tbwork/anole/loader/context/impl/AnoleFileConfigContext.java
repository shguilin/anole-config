package org.tbwork.anole.loader.context.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.tbwork.anole.loader.annotion.AnoleConfigLocation;
import org.tbwork.anole.loader.core.loader.AnoleLoader;
import org.tbwork.anole.loader.core.loader.impl.AnoleFileLoader;
import org.tbwork.anole.loader.enums.FileLoadStatus;
import org.tbwork.anole.loader.exceptions.ConfigFileDirectoryNotExistException;
import org.tbwork.anole.loader.util.AnoleLogger;
import org.tbwork.anole.loader.util.AnoleLogger.LogLevel;
import org.tbwork.anole.loader.util.FileUtil;
import org.tbwork.anole.loader.util.PathUtil;


/**
 * <p>Before using Anole to manage your configuration, 
 * you should create the configuration context. It is
 * recommended to use the {@link AnoleConfigLocation} to
 * setup your application. However in some case you may 
 * need to create the configuration context manually.
 * And this is a way to load full-path property files.
 * <p>Usage example:
 *    	
 * <pre>
 *    AnoleFileConfigContext acc = new AnoleFileConfigContext(configLocations, jarPatterns);
 *    //use Anole as you like.
 * </pre>
 * <p> <b>About LogLevel:</b> The anole does not use any log implement
 * in the startup stage, it only providers the standard output to the
 * console window. When the application started, it use SLF4J facade to 
 * print logs. 
 * @author tbwork
 * @see AnoleClasspathConfigContext
 */
public class AnoleFileConfigContext extends AbstractAnoleContext{


	public AnoleFileConfigContext(String [] configLocations, String [] jarPatterns) {
		super(configLocations, jarPatterns);
	}

	@Override
	protected AnoleLoader getAnoleLoader(String[] jarPatterns) {
		return new AnoleFileLoader(jarPatterns);
	}
}
