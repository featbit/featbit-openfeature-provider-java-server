package co.featbit.openfeature;

import co.featbit.commons.model.FBUser;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

class EvaluationContextConverter {
    static final EvaluationContextConverter INSTANCE = new EvaluationContextConverter();

    private EvaluationContextConverter() {
    }

    FBUser toFBUser(EvaluationContext evaluationContext) {
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
