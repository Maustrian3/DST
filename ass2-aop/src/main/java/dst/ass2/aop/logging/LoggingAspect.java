package dst.ass2.aop.logging;

import dst.ass2.aop.IPluginExecutable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

import java.lang.reflect.Field;
import java.util.logging.Logger;

@Aspect
public class LoggingAspect {

    @Before("execution(void dst.ass2.aop.IPluginExecutable.execute()) && !@annotation(Invisible)")
    public void beforeExecute(JoinPoint joinPoint) {
        IPluginExecutable plugin = (IPluginExecutable) joinPoint.getTarget();
        Logger logger = getLoggerFromPlugin(plugin);
        if (logger != null) {
            logger.info("Plugin " + plugin.getClass().getName() + " started to execute");
        } else {
            System.out.println("Plugin " + plugin.getClass().getName() + " started to execute");
        }
    }

    @After("execution(void dst.ass2.aop.IPluginExecutable.execute()) && !@annotation(Invisible)")
    public void afterExecute(JoinPoint joinPoint) {
        IPluginExecutable plugin = (IPluginExecutable) joinPoint.getTarget();
        Logger logger = getLoggerFromPlugin(plugin);
        if (logger != null) {
            logger.info("Plugin " + plugin.getClass().getName() + " is finished");
        } else {
            System.out.println("Plugin " + plugin.getClass().getName() + " is finished");
        }
    }

    private Logger getLoggerFromPlugin(IPluginExecutable plugin) {
        Class<?> pluginClass = plugin.getClass();
        Field[] fields = pluginClass.getDeclaredFields();
        for (Field field : fields) {
            if (java.util.logging.Logger.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    return (Logger) field.get(plugin);
                } catch (IllegalAccessException e) {
                    // Ignore and continue searching
                }
            }
        }
        return null;
    }
}