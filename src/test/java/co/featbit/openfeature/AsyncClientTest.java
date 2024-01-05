package co.featbit.openfeature;

import co.featbit.server.Status;
import dev.openfeature.sdk.OpenFeatureAPI;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsyncClientTest extends BaseTest {

    @Test
    void testAsyncInitClient() throws Exception {
        final FBProvider provider = initClientInOfflineMode();
        final AtomicReference<Status.StateType> state = new AtomicReference<>();
        OpenFeatureAPI.getInstance().onProviderReady(eventDetails -> state.set(provider.getClient().getDataUpdateStatusProvider().getState().getStateType()));
        OpenFeatureAPI.getInstance().setProvider(provider);
        Thread.sleep(100);
        assertEquals(Status.StateType.OK, state.get());
        provider.shutdown();
    }


}
