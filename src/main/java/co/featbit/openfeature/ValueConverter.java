package co.featbit.openfeature;

import dev.openfeature.sdk.ImmutableStructure;
import dev.openfeature.sdk.Value;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ValueConverter {
    static final ValueConverter INSTANCE = new ValueConverter();

    private ValueConverter() {
    }

    Value toValue(Object value) {
        if (value instanceof String) {
            return new Value((String) value);
        } else if (value instanceof Boolean) {
            return new Value(((Boolean) value).booleanValue());
        } else if (value instanceof Integer) {
            return new Value(((Integer) value).intValue());
        } else if (value instanceof Number) {
            return new Value(((Number) value).doubleValue());
        } else if (value instanceof List) {
            return new Value(((List<Value>) value).stream()
                    .map(this::toValue)
                    .collect(Collectors.toList()));
        } else if (value instanceof Map) {
            Map<String, Value> converted = new HashMap<>();
            ((Map) value).forEach((k, v) -> {
                converted.put(k.toString(), toValue(v));
            });
            return new Value(new ImmutableStructure(converted));
        } else if (value instanceof Instant) {
            return new Value((Instant) value);
        } else {
            return new Value();
        }
    }
}
