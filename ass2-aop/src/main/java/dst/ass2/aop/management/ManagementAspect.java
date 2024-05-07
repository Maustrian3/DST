package dst.ass2.aop.management;

import dst.ass2.aop.IPluginExecutable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Aspect
public class ManagementAspect {

    private Map<IPluginExecutable, Long> pluginStartTimes = new HashMap<>();
    private Timer timer = new Timer();

    @Before("execution(void dst.ass2.aop.IPluginExecutable.execute())")
    public void beforeExecute(JoinPoint joinPoint) {
        IPluginExecutable plugin = (IPluginExecutable) joinPoint.getTarget();
        pluginStartTimes.put(plugin, System.currentTimeMillis());
        Long timeout = getTimeoutValue(joinPoint);
        if (timeout != null) {
            scheduleTimeoutTask(plugin, timeout);
        }
    }

    @After("execution(void dst.ass2.aop.IPluginExecutable.execute())")
    public void afterExecute(JoinPoint joinPoint) {
        IPluginExecutable plugin = (IPluginExecutable) joinPoint.getTarget();
        pluginStartTimes.remove(plugin);
    }

    private Long getTimeoutValue(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Timeout timeoutAnnotation = signature.getMethod().getAnnotation(Timeout.class);
        if (timeoutAnnotation != null) {
            return timeoutAnnotation.value();
        }
        return null;
    }


    private void scheduleTimeoutTask(IPluginExecutable plugin, long timeout) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Long startTime = pluginStartTimes.get(plugin);
                if (startTime != null && System.currentTimeMillis() - startTime >= timeout) {
                    // Asynchronously invoke interrupted() method
                    new Thread(() -> plugin.interrupted()).start();
                }
            }
        }, timeout);
    }
}
