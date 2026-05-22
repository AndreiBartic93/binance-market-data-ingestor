package org.ingestor.util;

import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

@UtilityClass
public class UpdateUtils {
    public <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
