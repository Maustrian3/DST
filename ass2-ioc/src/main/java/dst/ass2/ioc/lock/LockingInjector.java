package dst.ass2.ioc.lock;

import dst.ass2.ioc.di.annotation.Component;
import javassist.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;

public class LockingInjector implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = null;

        try {
            ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            // Inject locks only in components
            if (!ctClass.hasAnnotation(Component.class)) {
                return classfileBuffer;
            }

            for (var ctMethod : ctClass.getDeclaredMethods()) {
                if (ctMethod.hasAnnotation(Lock.class)) {
                    injectLocking(ctMethod, classPool);
                }
            }

            return ctClass.toBytecode();

        } catch (CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void injectLocking(CtMethod ctMethod, ClassPool classPool) throws CannotCompileException {
        try {
            // Get the name of the lock
            String lockName = getLockName(ctMethod);

            ctMethod.insertBefore("dst.ass2.ioc.lock.LockManager.getInstance().getLock(\"" + lockName + "\").lock();");
            // return needed for addCatch. (try block needs to end with return/throw)
            ctMethod.insertAfter("dst.ass2.ioc.lock.LockManager.getInstance().getLock(\"" + lockName + "\").unlock(); return $_;", false);
            ctMethod.addCatch("dst.ass2.ioc.lock.LockManager.getInstance().getLock(\"" + lockName + "\").unlock(); throw $e;", classPool.get("java.lang.Exception"));
        } catch (NotFoundException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLockName(CtMethod ctMethod) throws ClassNotFoundException, NotFoundException {
        Object annotation = ctMethod.getAnnotation(Lock.class);

        try {
            return annotation.getClass().getMethod("value").invoke(annotation).toString();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
