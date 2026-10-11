package io.kestra.plugin.superhuman.docs.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.kestra.plugin.superhuman.docs.exceptions.SuperhumanDocsAuthenticationException;
import io.kestra.plugin.superhuman.docs.exceptions.SuperhumanDocsException;
import io.kestra.plugin.superhuman.docs.exceptions.SuperhumanDocsRateLimitException;
import io.kestra.plugin.superhuman.docs.exceptions.SuperhumanDocsResourceNotFoundException;
import io.kestra.plugin.superhuman.docs.models.PagedResponse;
import io.kestra.plugin.superhuman.docs.models.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SuperhumanDocsConnectionTest {
    private static final String TOKEN = "test-token";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private SuperhumanDocsConnection connection;

    @BeforeEach
    void setUp() {
        connection = new SuperhumanDocsConnection(
            TOKEN,
            LoggerFactory.getLogger(SuperhumanDocsConnectionTest.class),
            wireMock.baseUrl() + "/apis/v1/"
        );
    }

    @Test
    void defaultBaseUrlIsOfficialApi() {
        assertThat(SuperhumanDocsConnection.DEFAULT_BASE_URL, is("https://coda.io/apis/v1"));
    }

    @Test
    void sendsBearerTokenAndParsesResponse() throws Exception {
        wireMock.stubFor(get(urlEqualTo("/apis/v1/docs/doc1/tables/tbl1"))
            .willReturn(okJson("""
                {"id": "tbl1", "type": "table", "name": "Tasks", "rowCount": 3, "unknownField": true}
                """)));

        Table table = connection.get("docs/doc1/tables/tbl1", Table.class);

        assertThat(table.getId(), is("tbl1"));
        assertThat(table.getName(), is("Tasks"));
        wireMock.verify(getRequestedFor(urlEqualTo("/apis/v1/docs/doc1/tables/tbl1"))
            .withHeader("Authorization", equalTo("Bearer " + TOKEN)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void followsNextPageLink() throws Exception {
        String nextLink = wireMock.baseUrl() + "/apis/v1/docs/doc1/tables?pageToken=page2";
        wireMock.stubFor(get(urlEqualTo("/apis/v1/docs/doc1/tables"))
            .willReturn(okJson("""
                {"items": [{"id": "tbl1"}], "nextPageToken": "page2", "nextPageLink": "%s"}
                """.formatted(nextLink))));
        wireMock.stubFor(get(urlEqualTo("/apis/v1/docs/doc1/tables?pageToken=page2"))
            .willReturn(okJson("""
                {"items": [{"id": "tbl2"}]}
                """)));

        PagedResponse<Object> first = connection.get("/docs/doc1/tables", PagedResponse.class);
        assertThat(first.getItems(), hasSize(1));
        assertThat(first.getNextPageToken(), is("page2"));
        assertThat(first.hasMorePages(), is(true));

        PagedResponse<Object> second = connection.get(first.getNextPageLink(), PagedResponse.class);
        assertThat(second.getItems(), hasSize(1));
        assertThat(second.hasMorePages(), is(false));
        wireMock.verify(getRequestedFor(urlEqualTo("/apis/v1/docs/doc1/tables?pageToken=page2"))
            .withHeader("Authorization", equalTo("Bearer " + TOKEN)));
    }

    @Test
    void maps401ToAuthenticationException() {
        wireMock.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(401)));

        SuperhumanDocsAuthenticationException e = assertThrows(
            SuperhumanDocsAuthenticationException.class,
            () -> connection.get("docs", Object.class)
        );
        assertThat(e.getStatusCode(), is(401));
        assertThat(e.getMessage(), containsString("Superhuman Docs API Error (HTTP 401)"));
    }

    @Test
    void maps404ToResourceNotFoundException() {
        wireMock.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(404)));

        SuperhumanDocsResourceNotFoundException e = assertThrows(
            SuperhumanDocsResourceNotFoundException.class,
            () -> connection.get("docs/missing", Object.class)
        );
        assertThat(e.getStatusCode(), is(404));
        assertThat(e.getMessage(), containsString("/apis/v1/docs/missing"));
    }

    @Test
    void maps429ToRateLimitExceptionWithRetryAfter() {
        wireMock.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(429).withHeader("Retry-After", "7")));

        SuperhumanDocsRateLimitException e = assertThrows(
            SuperhumanDocsRateLimitException.class,
            () -> connection.get("docs", Object.class)
        );
        assertThat(e.getStatusCode(), is(429));
        assertThat(e.getRetryAfterSeconds(), is(7));
    }

    @Test
    void mapsOtherErrorsToBaseExceptionWithBody() {
        wireMock.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(500).withBody("boom")));

        SuperhumanDocsException e = assertThrows(
            SuperhumanDocsException.class,
            () -> connection.get("docs", Object.class)
        );
        assertThat(e.getStatusCode(), is(500));
        assertThat(e.getMessage(), containsString("boom"));
    }
}
