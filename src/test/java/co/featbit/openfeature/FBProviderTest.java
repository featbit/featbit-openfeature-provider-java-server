package co.featbit.openfeature;

import co.featbit.server.Status;
import dev.openfeature.sdk.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static co.featbit.server.EvaluationReason.*;
import static org.junit.jupiter.api.Assertions.*;

class FBProviderTest extends BaseTest {

    private EvaluationContext user1 = new ImmutableContext("test-user-1", new HashMap() {{
        put("name", new Value("test-user-1"));
        put("country", new Value("us"));
    }});
    private EvaluationContext user2 = new ImmutableContext("test-user-2", new HashMap() {{
        put("name", new Value("test-user-2"));
        put("country", new Value("fr"));
    }});
    private EvaluationContext user3 = new ImmutableContext("test-user-3", new HashMap() {{
        put("name", new Value("test-user-3"));
        put("country", new Value("cn"));
        put("major", new Value("cs"));
    }});
    private EvaluationContext user4 = new ImmutableContext("test-user-4", new HashMap() {{
        put("name", new Value("test-user-4"));
        put("country", new Value("uk"));
        put("major", new Value("physics"));
    }});
    private EvaluationContext cnPhoneNumber = new ImmutableContext("18555358000", new HashMap() {{
        put("name", new Value("test-user-5"));
    }});
    private EvaluationContext frPhoneNumber = new ImmutableContext("0603111111", new HashMap() {{
        put("name", new Value("test-user-6"));
    }});
    private EvaluationContext email = new ImmutableContext("test-user-7@featbit.com", new HashMap() {{
        put("name", new Value("test-user-7"));
    }});

    @Test
    void testBoolEvaluation() throws IOException {
        FBProvider provider = initClientInOfflineMode();
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
        Client client = OpenFeatureAPI.getInstance().getClient();

        Boolean result = client.getBooleanValue("ff-test-bool", false, user1);
        assertTrue(result);
        FlagEvaluationDetails<Boolean> details = client.getBooleanDetails("ff-test-bool", false, user2);
        assertTrue(details.getValue());
        assertEquals(REASON_TARGET_MATCH, details.getReason());
        Value result2 = client.getObjectValue("ff-test-bool", new Value(false), user3);
        assertFalse(result2.asBoolean());
        FlagEvaluationDetails<Value> details2 = client.getObjectDetails("ff-test-bool", new Value(false), user4);
        assertTrue(details2.getValue().asBoolean());
        assertEquals(REASON_FALLTHROUGH, details2.getReason());

        provider.shutdown();
    }

    @Test
    void testNumericVariation() throws IOException {
        FBProvider provider = initClientInOfflineMode();
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
        Client client = OpenFeatureAPI.getInstance().getClient();
        int result = client.getIntegerValue("ff-test-number", -1, user1);
        assertEquals(1, result);
        FlagEvaluationDetails<Integer> details = client.getIntegerDetails("ff-test-number", -1, user2);
        assertEquals(33, details.getValue());
        assertEquals(REASON_RULE_MATCH, details.getReason());
        Value result2 = client.getObjectValue("ff-test-number", new Value(-1D), user3);
        assertEquals(86D, result2.asDouble());
        FlagEvaluationDetails<Value> details2 = client.getObjectDetails("ff-test-number", new Value(-1D), user4);
        assertEquals(9999D, details2.getValue().asDouble());
        assertEquals(REASON_FALLTHROUGH, details2.getReason());
        provider.shutdown();
    }

    @Test
    void testStringVariation() throws IOException {
        FBProvider provider = initClientInOfflineMode();
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
        Client client = OpenFeatureAPI.getInstance().getClient();

        String result = client.getStringValue("ff-test-string", "error", cnPhoneNumber);
        assertEquals("phone number", result);
        FlagEvaluationDetails<String> details = client.getStringDetails("ff-test-string", "error", frPhoneNumber);
        assertEquals("phone number", details.getValue());
        Value result2 = client.getObjectValue("ff-test-string", new Value("error"), email);
        assertEquals("email", result2.asString());
        FlagEvaluationDetails<Value> details2 = client.getObjectDetails("ff-test-string", new Value("error"), user1);
        assertEquals("others", details2.getValue().asString());
        assertEquals(REASON_FALLTHROUGH, details2.getReason());

        provider.shutdown();
    }

    @Test
    void testJsonVariation() throws IOException {
        FBProvider provider = initClientInOfflineMode();
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
        Client client = OpenFeatureAPI.getInstance().getClient();

        Value result = client.getObjectValue("ff-test-json", new Value(new ImmutableStructure(new HashMap<>())), user1);
        assertEquals(200, result.asStructure().asMap().get("code").asInteger());
        FlagEvaluationDetails<Value> details = client.getObjectDetails("ff-test-json", new Value(new ImmutableStructure(new HashMap<>())), user2);
        assertEquals(404, details.getValue().asStructure().asMap().get("code").asInteger());
        assertEquals(REASON_FALLTHROUGH, details.getReason());

        provider.shutdown();
    }

    @Test
    void testSegment() throws IOException {
        FBProvider provider = initClientInOfflineMode();
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
        Client client = OpenFeatureAPI.getInstance().getClient();

        String res = client.getStringValue("ff-test-seg", "error", user1);
        assertEquals("teamA", res);
        res = client.getStringValue("ff-test-seg", "error", user2);
        assertEquals("teamB", res);
        res = client.getStringValue("ff-test-seg", "error", user3);
        assertEquals("teamA", res);
        res = client.getStringValue("ff-test-seg", "error", user4);
        assertEquals("teamB", res);

        provider.shutdown();
    }

    @Test
    void testAysnInitClient() throws Exception {
        final FBProvider provider = initClientInOfflineMode();
        final AtomicReference<Status.StateType> state = new AtomicReference<>();
        OpenFeatureAPI.getInstance().setProvider(provider);
        OpenFeatureAPI.getInstance().onProviderReady(eventDetails -> {
            state.set(provider.getClient().getDataUpdateStatusProvider().getState().getStateType());
        });
        Thread.sleep(100);

        assertEquals(Status.StateType.OK, state.get());
        provider.shutdown();
    }

}
