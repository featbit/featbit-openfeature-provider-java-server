package co.featbit.openfeature;

import co.featbit.server.FBConfig;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.Value;

import java.io.IOException;
import java.util.HashMap;

abstract class BaseTest {

    protected EvaluationContext user1 = new ImmutableContext("test-user-1", new HashMap() {{
        put("name", new Value("test-user-1"));
        put("country", new Value("us"));
    }});
    protected EvaluationContext user2 = new ImmutableContext("test-user-2", new HashMap() {{
        put("name", new Value("test-user-2"));
        put("country", new Value("fr"));
    }});
    protected EvaluationContext user3 = new ImmutableContext("test-user-3", new HashMap() {{
        put("name", new Value("test-user-3"));
        put("country", new Value("cn"));
        put("major", new Value("cs"));
    }});
    protected EvaluationContext user4 = new ImmutableContext("test-user-4", new HashMap() {{
        put("name", new Value("test-user-4"));
        put("country", new Value("uk"));
        put("major", new Value("physics"));
    }});
    protected EvaluationContext cnPhoneNumber = new ImmutableContext("18555358000", new HashMap() {{
        put("name", new Value("test-user-5"));
    }});
    protected EvaluationContext frPhoneNumber = new ImmutableContext("0603111111", new HashMap() {{
        put("name", new Value("test-user-6"));
    }});
    protected EvaluationContext email = new ImmutableContext("test-user-7@featbit.com", new HashMap() {{
        put("name", new Value("test-user-7"));
    }});

    protected static FBProvider initClientInOfflineMode() throws IOException {
        FBConfig config = new FBConfig.Builder()
                .offline(true)
                .streamingURL("ws://fake-url")
                .eventURL("http://fake-url")
                .build();
        FBProvider provider = new FBProvider("env-secret", config);
        provider.getClient().initializeFromExternalJson(readResource("fbclient_test_data.json"));
        return provider;
    }

    protected static String readResource(final String fileName) throws IOException {
        return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
    }

}
