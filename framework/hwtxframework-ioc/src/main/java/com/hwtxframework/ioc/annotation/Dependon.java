package com.hwtxframework.ioc.annotation;

import java.lang.annotation.*;

/**
 * Created by panye on 14-8-22.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface Dependon {

    String[] value();
}
