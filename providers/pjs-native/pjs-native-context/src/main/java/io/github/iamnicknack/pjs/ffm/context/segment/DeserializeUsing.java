package io.github.iamnicknack.pjs.ffm.context.segment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeserializeUsing {
    Class<? extends MemorySegmentDeserializer<?>> value();
}
