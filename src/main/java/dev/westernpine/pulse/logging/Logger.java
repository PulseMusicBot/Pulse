package dev.westernpine.pulse.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Logger {

    public static final java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();

}
