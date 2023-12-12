package de.hasait.common.ui.puif;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface StringSetPui {

    String height() default "10em";

}
