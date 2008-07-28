package pivot.beans;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Inherited 
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanInfo {
    String icon();
}
