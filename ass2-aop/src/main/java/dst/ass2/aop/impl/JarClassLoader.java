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

    private final File jarFile;

    public JarClassLoader(File jarFile, ClassLoader parent) throws IOException {
        super(new URL[]{jarFile.toURI().toURL()}, parent);
        this.jarFile = jarFile;
    }

    /*
    TODO: Take care of class loading: there must not be any problem with the concurrent execution of different plugins containing classes with equal names.

     */
    public List<Class<?>> loadClassesImplementing(Class<IPluginExecutable> interfaceClass) throws IOException, ClassNotFoundException {
        List<Class<?>> pluginClasses = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");
                    Class<?> clazz = loadClass(className);
                    if (interfaceClass.isAssignableFrom(clazz)) {
                        pluginClasses.add(clazz.asSubclass(interfaceClass));
                    }
                }
            }
        }

        return pluginClasses;
    }
}
