package co.featbit.openfeature;

import co.featbit.commons.model.EvalDetail;
import co.featbit.server.FBClientImp;
import co.featbit.server.FBConfig;
import co.featbit.server.exterior.FBClient;
import com.google.common.collect.ImmutableList;
import dev.openfeature.sdk.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class FBProvider extends EventProvider {
    private static final class FBProviderMetaData implements Metadata {
        @Override
        public String getName() {
            return "FeatBit.OpenFeature.JavaServerProvider";
        }
    }

    private final Metadata metadata = new FBProviderMetaData();
    private final Converter.EvaluationContextConverter evaluationContextConverter = Converter.EvaluationContextConverter.INSTANCE;
    private final Converter.EvaluationDetailConverter evaluationDetailConverter = Converter.EvaluationDetailConverter.INSTANCE;
    private final Converter.ValueConverter valueConverter = Converter.ValueConverter.INSTANCE;
    private ProviderState currState = ProviderState.NOT_READY;
    private final FBClient client;

    public FBProvider(String sdkKey, FBConfig config) {
        this.client = new FBClientImp(sdkKey, config);
    }


    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String flagKey, Boolean defaultValue, EvaluationContext ctx) {
        return evaluationDetailConverter.toProviderEvaluation(client.boolVariationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), defaultValue));

    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String flagKey, String defaultValue, EvaluationContext ctx) {
        return evaluationDetailConverter.toProviderEvaluation(client.variationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), defaultValue));
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String flagKey, Integer defaultValue, EvaluationContext ctx) {
        return evaluationDetailConverter.toProviderEvaluation(client.intVariationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), defaultValue));
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String flagKey, Double defaultValue, EvaluationContext ctx) {
        return evaluationDetailConverter.toProviderEvaluation(client.doubleVariationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), defaultValue));
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String flagKey, Value defaultValue, EvaluationContext ctx) {
        if (defaultValue == null || defaultValue.isNull() || defaultValue.isString()) {
            String dv = defaultValue == null || defaultValue.isNull() ? null : defaultValue.asString();
            EvalDetail<String> res = client.variationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), dv);
            return evaluationDetailConverter.toProviderEvaluation(EvalDetail.of(new Value(res.getVariation()), res));
        } else if (defaultValue.isBoolean()) {
            EvalDetail<Boolean> res = client.boolVariationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), defaultValue.asBoolean());
            return evaluationDetailConverter.toProviderEvaluation(EvalDetail.of(new Value(res.getVariation()), res));
        } else if (defaultValue.asObject() instanceof Double) {
            EvalDetail<Double> res = client.doubleVariationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), defaultValue.asDouble());
            return evaluationDetailConverter.toProviderEvaluation(EvalDetail.of(new Value(res.getVariation()), res));
        } else if (defaultValue.asObject() instanceof Integer) {
            EvalDetail<Integer> res = client.intVariationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), defaultValue.asInteger());
            return evaluationDetailConverter.toProviderEvaluation(EvalDetail.of(new Value(res.getVariation()), res));
        } else if (defaultValue.isList()) {
            EvalDetail<List> res = client.jsonVariationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), List.class, null);
            return evaluationDetailConverter.toProviderEvaluation(EvalDetail.of(valueConverter.toValue(res.getVariation()), res));
        } else if (defaultValue.isStructure()) {
            EvalDetail<Map> res = client.jsonVariationDetail(flagKey, evaluationContextConverter.toFBUser(ctx), Map.class, null);
            return evaluationDetailConverter.toProviderEvaluation(EvalDetail.of(valueConverter.toValue(res.getVariation()), res));
        } else {
            throw new IllegalArgumentException("Unsupported default value type");
        }
    }

    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        client.getDataUpdateStatusProvider().addStateListener(state -> {
            switch (state.getStateType()) {
                case INITIALIZING:
                    currState = ProviderState.NOT_READY;
                    break;
                case INTERRUPTED:
                    currState = ProviderState.ERROR;
                    String message = state.getErrorTrack() == null ? " Unknown Error" : state.getErrorTrack().getMessage();
                    emitProviderError(ProviderEventDetails.builder().message(message).build());
                    break;
                case OK:
                    if (currState != ProviderState.READY) {
                        currState = ProviderState.READY;
                        emitProviderReady(ProviderEventDetails.builder().message("FeatBit provider is ready").build());
                    }
                    break;
                case OFF:
                    currState = ProviderState.STALE;
                    emitProviderStale(ProviderEventDetails.builder().message("FeatBit provider is OFF").build());
                    break;
            }
        });
        client.getFlagTracker().addFlagChangeListener(event -> {
            emitProviderConfigurationChanged(ProviderEventDetails.builder().flagsChanged(ImmutableList.of(event.getKey())).build());
        });
        if (client.isInitialized()) {
            currState = ProviderState.READY;
            return;
        }
        // Wait for the client to be ready within 3 minutes
        if (!client.getDataUpdateStatusProvider().waitForOKState(Duration.ofMinutes(3))) {
            // throw an exception for the OpenFeature SDK, which will handle this error
            throw new RuntimeException("Failed to initialize FeatBit Java SDK within 3 minutes");
        }
    }

    @Override
    public void shutdown() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProviderState getState() {
        return currState;
    }

    public FBClient getClient() {
        return client;
    }

}
