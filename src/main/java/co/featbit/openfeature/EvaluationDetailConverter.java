package co.featbit.openfeature;

import co.featbit.commons.model.EvalDetail;
import co.featbit.server.EvaluationReason;
import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.ProviderEvaluation;

class EvaluationDetailConverter {
    static final EvaluationDetailConverter INSTANCE = new EvaluationDetailConverter();

    private EvaluationDetailConverter() {
    }


    <T> ProviderEvaluation<T> toProviderEvaluation(EvalDetail<T> detail) {
        return buildProviderEvaluation(detail.getVariation(),
                detail.getVariationIndex(),
                detail.getReason(),
                detail.isDefaultVariation());
    }

    private <T> ProviderEvaluation<T> buildProviderEvaluation(T value, String variant, String reason, boolean isDefault) {
        ProviderEvaluation.ProviderEvaluationBuilder<T> builder = ProviderEvaluation.<T>builder()
                .value(value)
                .reason(reason);
        if (!isDefault) {
            builder.variant(variant);
        }
        if (isErrorCode(reason)) {
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
