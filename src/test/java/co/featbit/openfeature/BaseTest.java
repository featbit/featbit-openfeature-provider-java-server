package co.featbit.openfeature;

import co.featbit.server.FBConfig;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;

abstract class BaseTest {

    protected FBProvider initClientInOfflineMode() throws IOException {
        FBConfig config = new FBConfig.Builder()
                .offline(true)
                .streamingURL("ws://fake-url")
                .eventURL("http://fake-url")
                .build();
        FBProvider provider = new FBProvider("env-secret", config);
        provider.getClient().initializeFromExternalJson(readResource("fbclient_test_data.json"));
        return provider;
    }

    protected String readResource(final String fileName) throws IOException {
        return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
    }

}
