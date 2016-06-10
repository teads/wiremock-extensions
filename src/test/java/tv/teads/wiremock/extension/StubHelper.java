package tv.teads.wiremock.extension;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.RemoteMappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class StubHelper {

    /**
     * Since Wiremock 2.0 introduces complex generic types on RemoteMappingBuilder
     * Scala is not able to handle it. By playing here with the WireMockServer we
     * can stub anything we want.
     */
    public static void stub(
            WireMockServer server,
            String url,
            String body,
            String... transformers) {
        RemoteMappingBuilder mapping = any(urlEqualTo(url));

        ResponseDefinitionBuilder response = aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody(body)
                .withTransformers(transformers);

        server.givenThat(mapping.willReturn(response));
    }
}
