package dst.ass2.ioc.lock;

import dst.ass2.ioc.di.annotation.Component;
import javassist.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class LockingInjector implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        ClassPool classPool = ClassPool.getDefault();
        try {
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            if (ctClass.hasAnnotation(Component.class)) {
                return classfileBuffer;
            }

            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
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
            String lockName = getLockName(ctMethod);
            String lockVariableName = "lock_" + lockName;
            ctMethod.addLocalVariable(lockVariableName, CtClass.booleanType);
            ctMethod.insertBefore("dst.ass2.ioc.lock.LockManager.getInstance().lock(\"" + lockName + "\");");
            ctMethod.insertAfter("dst.ass2.ioc.lock.LockManager.getInstance().unlock(\"" + lockName + "\");");
            ctMethod.insertAfter("return;"); // Needed for addCatch. (try block needs to end with return/throw)
            ctMethod.addCatch("dst.ass2.ioc.lock.LockManager.getInstance().unlock(\"" + lockName + "\"); throw $e;",
                    classPool.get("java.lang.Exception"));
        } catch (NotFoundException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLockName(CtMethod ctMethod) throws ClassNotFoundException, NotFoundException {
        Annotation annotation = (Annotation) ctMethod.getAnnotation(Lock.class);
        StringMemberValue value = (StringMemberValue) annotation.getMemberValue("value");
        return value.getValue();
    }

}
