package co.featbit.openfeature;

import dev.openfeature.sdk.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import static co.featbit.server.EvaluationReason.*;
import static org.junit.jupiter.api.Assertions.*;

class FBProviderTest extends BaseTest {

    private static FBProvider provider;

    @BeforeAll
    static void init() throws IOException {
        provider = initClientInOfflineMode();
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
    }

    @AfterAll
    static void dispose() {
        provider.shutdown();
    }

    @Test
    void testBoolEvaluation() {
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
    }

    @Test
    void testNumericVariation() {
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
    }

    @Test
    void testStringVariation() {
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
    }

    @Test
    void testJsonVariation() {
        Client client = OpenFeatureAPI.getInstance().getClient();

        Value result = client.getObjectValue("ff-test-json", new Value(new ImmutableStructure(new HashMap<>())), user1);
        assertEquals(200, result.asStructure().asMap().get("code").asInteger());
        FlagEvaluationDetails<Value> details = client.getObjectDetails("ff-test-json", new Value(new ImmutableStructure(new HashMap<>())), user2);
        assertEquals(404, details.getValue().asStructure().asMap().get("code").asInteger());
        assertEquals(REASON_FALLTHROUGH, details.getReason());
    }

    @Test
    void testSegment() {
        Client client = OpenFeatureAPI.getInstance().getClient();

        String res = client.getStringValue("ff-test-seg", "error", user1);
        assertEquals("teamA", res);
        res = client.getStringValue("ff-test-seg", "error", user2);
        assertEquals("teamB", res);
        res = client.getStringValue("ff-test-seg", "error", user3);
        assertEquals("teamA", res);
        res = client.getStringValue("ff-test-seg", "error", user4);
        assertEquals("teamB", res);
    }

}
