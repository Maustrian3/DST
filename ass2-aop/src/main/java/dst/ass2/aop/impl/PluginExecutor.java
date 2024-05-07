package dst.ass2.aop.impl;

import dst.ass2.aop.IPluginExecutable;
import dst.ass2.aop.IPluginExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
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
        Map<Path, Long> recentlyLoaded = new HashMap<>();

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
                            // May be interrupted at any time
                            return;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            // Skip if overflow occurs (events are discarded or lost because the event buffer is full)
                            if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }

                            Path changedFilePath = (Path) event.context();

                            // Skip non jar files
                            if (!changedFilePath.toString().endsWith(".jar")) {
                                continue;
                            }

                            // Edge case: Some OS fire two events (ENTRY MODIFY and ENTRY CREATE) for the same file at creation
                            // Skip if new event is older than already logged event
                            if (recentlyLoaded.containsKey(changedFilePath)
                                    && recentlyLoaded.get(changedFilePath) <= changedFilePath.toFile().lastModified()) {
                                continue;
                            }

                            recentlyLoaded.put(changedFilePath, path.toFile().lastModified());

                            Path resolvedPath = path.resolve(changedFilePath);
                            executePlugin(new File(resolvedPath.toString()));
                        }

                        key.reset();
                    }
                }, executorService);

            } catch (IOException e) {
                System.out.println(e);
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
                            System.out.println("Plugin had problem while executing:" + e.getMessage());
                        }
                    });
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    System.out.println("Can't create new plugin instance:" + e.getMessage());
                } catch (NoSuchMethodException e) {
                    System.out.println("Can't find constructor:" + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
