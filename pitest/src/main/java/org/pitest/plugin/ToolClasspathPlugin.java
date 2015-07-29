package org.pitest.plugin;

/**
 * Plugins that do not need to be available at runtime chould implement this
 * interface
 *
 */
public interface ToolClasspathPlugin {

  String description();

}
