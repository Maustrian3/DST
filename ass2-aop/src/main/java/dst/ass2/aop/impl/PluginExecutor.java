package dst.ass2.aop.impl;

import dst.ass2.aop.IPluginExecutable;
import dst.ass2.aop.IPluginExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarFile;

public class PluginExecutor implements IPluginExecutor {
    private final List<File> monitoredDirectories;
    private final ExecutorService executorService;
    private volatile boolean running;

    public PluginExecutor() {
        this.monitoredDirectories = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.running = false;
    }

    @Override
    public void monitor(File dir) {
        monitoredDirectories.add(dir);
    }

    @Override
    public void stopMonitoring(File dir) {
        monitoredDirectories.remove(dir);
    }

    @Override
    public void start() {
        running = true;
        monitorDirectories();
    }

    @Override
    public void stop() {
        running = false;
        executorService.shutdown();
    }

    private void monitorDirectories() {
        for (File dir : monitoredDirectories) {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = dir.toPath();
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

                CompletableFuture.runAsync(() -> {
                    while (running) {
                        WatchKey key;
                        try {
                            key = watchService.take();
                        } catch (InterruptedException e) {
                            return;
                        }

                        Set<Path> createdFiles = new HashSet<>(); // Keep track of executed files

                        for (WatchEvent<?> event : key.pollEvents()) {
                            // Skip if overflow occurs (events are discarded or lost because the event buffer is full)
                            if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }

                            Path changedFile = (Path) event.context();

                            if (!changedFile.toString().endsWith(".jar")) {
                                continue;
                            }

                            Path resolvedPath = path.resolve(changedFile);

                            // Only execute plugins which have been created before
                            //if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            //    createdFiles.add(resolvedPath);
                            // } else // FIXME work with creation/modfy time to avoid multiple exectuion of same plugin

                                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE
                                        || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                executePlugin(new File(resolvedPath.toString()));
                            }
                        }

                        key.reset();
                    }
                }, executorService);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void executePlugin(File jarFile) {
        ClassLoader parentClassLoader = getClass().getClassLoader();
        try (JarClassLoader loader = new JarClassLoader(jarFile, parentClassLoader)) {
            List<Class<?>> pluginClasses = loader.loadClassesImplementing(IPluginExecutable.class);

            for (Class<?> pluginClass : pluginClasses) {
                try {
                IPluginExecutable plugin = (IPluginExecutable) pluginClass.getDeclaredConstructor().newInstance();
                executorService.execute(() -> {
                    try {
                        plugin.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
