package org.pitest.plugin;

/**
 * Plugins that must be present on the classpath of client code at runtime
 * should provide an implementation of this interface.
 *
 * Plugins that share the classpath with client code will need to embed their
 * dependencies within their own jar under different package names so as not to
 * pollute the client classpath.
 *
 */
public interface ClientClasspathPlugin {

  String description();

}
