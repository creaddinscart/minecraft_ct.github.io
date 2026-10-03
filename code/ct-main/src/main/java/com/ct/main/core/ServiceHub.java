package com.ct.main.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ServiceHub implements com.ct.main.api.ModuleContext.ServiceRegistry {
    private final Map<Class<?>, Object> services = new LinkedHashMap<>();

    @Override
    public <T> void publish(Class<T> type, T instance) {
        services.put(type, instance);
    }

    @Override
    public <T> Optional<T> lookup(Class<T> type) {
        return Optional.ofNullable(type.cast(services.get(type)));
    }
}
