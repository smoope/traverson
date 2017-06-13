package com.smoope.utils.traverson;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

public class TraversonJsonHalPutTest extends AbstractJsonHalTest {

    @Test
    public void followUrl() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api") && request.getBody().readUtf8() != null
                        && request.getMethod().equals("PUT")) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        ItemResult result = traverson
            .followUri(baseUrl)
            .put(new Object(), ItemResult.class);

        assertThat(requests, CoreMatchers.is(1));
        assertThat(result.getId(), CoreMatchers.notNullValue());
        assertThat(result.getName(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("lightSaber"), CoreMatchers.notNullValue());
    }

    @Test
    public void followRoot() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api") && request.getBody().readUtf8() != null
                        && request.getMethod().equals("PUT")) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        ItemResult result = traverson
            .follow()
            .put(new Object(), ItemResult.class);

        assertThat(requests, CoreMatchers.is(1));
        assertThat(result.getId(), CoreMatchers.notNullValue());
        assertThat(result.getName(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("lightSaber"), CoreMatchers.notNullValue());
    }

    @Test
    public void followRelation() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("PUT") &&
                    request.getBody().readUtf8() != null) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        ItemResult result = traverson
            .follow("jedi")
            .put(new Object(), ItemResult.class);

        assertThat(requests, CoreMatchers.is(2));
        assertThat(result.getId(), CoreMatchers.notNullValue());
        assertThat(result.getName(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("lightSaber"), CoreMatchers.notNullValue());
    }

    @Test
    public void followMulipleRelationWithItem() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT);
                } else if (request.getPath().contains("/jedi") && (request.getMethod().equals("PUT") || request.getMethod().equals("GET")) &&
                    request.getBody().readUtf8() != null) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        ItemResult result = traverson
            .follow("jedi", "self")
            .put(new Object(), ItemResult.class);

        assertThat(requests, CoreMatchers.is(3));
        assertThat(result.getId(), CoreMatchers.notNullValue());
        assertThat(result.getName(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("lightSaber"), CoreMatchers.notNullValue());
    }

    @Test
    public void followEmbeddedResource() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT_WITH_EMBEDDED);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("PUT") &&
                    request.getBody().readUtf8() != null) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        ItemResult result = traverson
            .follow("jedi")
            .put(new Object(), ItemResult.class);

        assertThat(requests, CoreMatchers.is(1));
        assertThat(result.getId(), CoreMatchers.notNullValue());
        assertThat(result.getName(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("lightSaber"), CoreMatchers.notNullValue());
    }

    @Test
    public void followEmbeddedResourceRelation() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT_WITH_EMBEDDED);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("PUT") &&
                    request.getBody().readUtf8() != null) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        ItemResult result = traverson
            .follow("jedi", "self")
            .put(new Object(), ItemResult.class);

        assertThat(requests, CoreMatchers.is(2));
        assertThat(result.getId(), CoreMatchers.notNullValue());
        assertThat(result.getName(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("lightSaber"), CoreMatchers.notNullValue());
    }
}