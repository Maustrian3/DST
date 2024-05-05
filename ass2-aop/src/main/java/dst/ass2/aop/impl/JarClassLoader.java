package dst.ass2.aop.impl;

import dst.ass2.aop.IPluginExecutable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoader extends URLClassLoader {

    public JarClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public List<Class<?>> loadClassesImplementingIPluginExecutable(File jarFile) throws IOException, ClassNotFoundException {
        List<Class<?>> pluginClasses = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    // Reformat class path into class name
                    String className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");
                    Class<?> clazz = loadClass(className);
                    // Is IPluginExecutable superinterface of clazz?
                    if (IPluginExecutable.class.isAssignableFrom(clazz)) {
                        pluginClasses.add(clazz);
                    }
                }
            }
        }

        return pluginClasses;
    }
}
