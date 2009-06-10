package pivot.wtkx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that causes a loaded WTKX element to be bound to the annotated
 * field.
 *
 * @author gbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WTKX {
    /**
     * The ID of the WTKX variable that references the element to bind. It
     * should be a valid <tt>wtkx:id</tt> from the loaded WTKX resource. If
     * unspecified, the name of the annotated field will be used.
     */
    public String id() default "\0";
}
