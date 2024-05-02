package dst.ass2.service.facade.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Annotation is available at runtime
@Target({ElementType.METHOD, ElementType.TYPE}) // Annotation can be used on methods and classes
public @interface RequireAuthentication {
}
// TODO test annotation