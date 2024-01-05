package co.featbit.openfeature;

import co.featbit.commons.model.EvalDetail;
import co.featbit.commons.model.FBUser;
import co.featbit.server.EvaluationReason;
import dev.openfeature.sdk.*;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class Converter {
    static final class EvaluationContextConverter {
        static final EvaluationContextConverter INSTANCE = new EvaluationContextConverter();

        private EvaluationContextConverter() {
        }

        FBUser toFBUser(EvaluationContext evaluationContext) {
            if (evaluationContext == null) {
                throw new IllegalArgumentException("The evaluation context must not be null.");
            }
            Map<String, Value> attributes = evaluationContext.asMap();
            String targetingKey = evaluationContext.getTargetingKey();
            Value keyAsValue = attributes.get("key");
            Value keyIdAsValue = attributes.get("keyid");
            targetingKey = getTargetingKey(targetingKey, keyAsValue, keyIdAsValue);
            final FBUser.Builder builder = new FBUser.Builder(targetingKey).userName(targetingKey);
            return buildSingleUser(builder, attributes);
        }

        private String getTargetingKey(String targetingKey, Value keyAsValue, Value keyIdAsValue) {
            if (StringUtils.isNotBlank(targetingKey)) {
                return targetingKey;
            } else if (keyAsValue != null && keyAsValue.isString() && StringUtils.isNotBlank(keyAsValue.asString())) {
                return keyAsValue.asString();
            } else if (keyIdAsValue != null && keyIdAsValue.isString() && StringUtils.isNotBlank(keyIdAsValue.asString())) {
                return keyIdAsValue.asString();
            } else {
                throw new IllegalArgumentException("The evaluation context did not contain a valid targeting key.");
            }
        }

        private FBUser buildSingleUser(FBUser.Builder builder, Map<String, Value> attributes) {
            Value nameAsValue = attributes.get("name");
            Value userNameAsValue = attributes.get("username");
            if (nameAsValue != null && nameAsValue.isString() && StringUtils.isNotBlank(nameAsValue.asString())) {
                builder.userName(nameAsValue.asString());
            } else if (userNameAsValue != null && userNameAsValue.isString() && StringUtils.isNotBlank(userNameAsValue.asString())) {
                builder.userName(userNameAsValue.asString());
            }
            attributes.forEach((key, value) -> {
                if (value != null && value.isString()) {
                    builder.custom(key, value.asString());
                }
            });
            return builder.build();
        }

    }

    static final class EvaluationDetailConverter {
        static final EvaluationDetailConverter INSTANCE = new EvaluationDetailConverter();

        private EvaluationDetailConverter() {
        }


        <T> ProviderEvaluation<T> toProviderEvaluation(EvalDetail<T> detail) {
            return buildProviderEvaluation(detail.getVariation(),
                    detail.getReason(),
                    detail.isDefaultVariation());
        }

        private <T> ProviderEvaluation<T> buildProviderEvaluation(T value, String reason, boolean isDefault) {
            ProviderEvaluation.ProviderEvaluationBuilder<T> builder = ProviderEvaluation.<T>builder()
                    .value(value)
                    .reason(reason);
            if (isDefault && isErrorCode(reason)) {
                builder.errorCode(getErrorCode(reason));
            }
            return builder.build();
        }

        private boolean isErrorCode(String reason) {
            switch (reason) {
                case EvaluationReason.REASON_USER_NOT_SPECIFIED:
                case EvaluationReason.REASON_CLIENT_NOT_READY:
                case EvaluationReason.REASON_FLAG_NOT_FOUND:
                case EvaluationReason.REASON_ERROR:
                case EvaluationReason.REASON_WRONG_TYPE:
                    return true;
                default:
                    return false;
            }
        }

        private ErrorCode getErrorCode(String reason) {
            switch (reason) {
                case EvaluationReason.REASON_USER_NOT_SPECIFIED:
                    return ErrorCode.TARGETING_KEY_MISSING;
                case EvaluationReason.REASON_CLIENT_NOT_READY:
                    return ErrorCode.PROVIDER_NOT_READY;
                case EvaluationReason.REASON_FLAG_NOT_FOUND:
                    return ErrorCode.FLAG_NOT_FOUND;
                case EvaluationReason.REASON_WRONG_TYPE:
                    return ErrorCode.TYPE_MISMATCH;
                default:
                    return ErrorCode.GENERAL;
            }

        }


    }


    static final class ValueConverter {
        static final ValueConverter INSTANCE = new ValueConverter();

        private ValueConverter() {
        }

        Value toValue(Object value) {
            if (value instanceof String) {
                return new Value((String) value);
            } else if (value instanceof Boolean) {
                return new Value(((Boolean) value).booleanValue());
            } else if (value instanceof Number) {
                if (((Number) value).doubleValue() == ((Number) value).intValue())
                    return new Value(((Number) value).intValue());
                return new Value(((Number) value).doubleValue());
            } else if (value instanceof List) {
                List<Value> lv = (List<Value>) ((List) value).stream()
                        .map(this::toValue)
                        .collect(Collectors.toList());
                return new Value(lv);
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

}
