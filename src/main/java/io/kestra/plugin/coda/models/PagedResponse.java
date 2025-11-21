package io.kestra.plugin.coda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Generic wrapper for paginated responses from the Coda API.
 */
@Builder
@Getter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedResponse<T> {
    @Schema(
        title = "Items",
        description = "The list of items in this page"
    )
    @JsonProperty("items")
    private List<T> items;

    @Schema(
        title = "Next Page Token",
        description = "Token to fetch the next page of results"
    )
    @JsonProperty("nextPageToken")
    private String nextPageToken;

    @Schema(
        title = "Next Page Link",
        description = "Full URL to fetch the next page of results"
    )
    @JsonProperty("nextPageLink")
    private String nextPageLink;

    @Schema(
        title = "Has More Pages",
        description = "Whether there are more pages to fetch"
    )
    public boolean hasMorePages() {
        return nextPageToken != null || nextPageLink != null;
    }
}
