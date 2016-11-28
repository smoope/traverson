package com.smoope.utils.traverson;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

public class TraversonJsonHalGetTest extends AbstractJsonHalTest {

    @Test
    public void followUrlWithCollection() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api") && request.getMethod().equals("GET")) {
                    requests++;
                    return generateResponse(Response.COLLECTION);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        CollectionResult result = traverson
            .followUri(baseUrl)
            .get(CollectionResult.class);

        assertThat(requests, CoreMatchers.is(1));
        assertThat(result.getEmbedded().get("jedi").size(), CoreMatchers.is(2));
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("next"), CoreMatchers.notNullValue());
    }

    @Test
    public void followUrlWithItem() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api") && request.getMethod().equals("GET")) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        ItemResult result = traverson
            .followUri(baseUrl)
            .get(ItemResult.class);

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
                if (request.getPath().equals("/api") && request.getMethod().equals("GET")) {
                    requests++;
                    return generateResponse(Response.ROOT);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        TraversonResult result = traverson
            .follow()
            .get(TraversonResult.class);

        assertThat(requests, CoreMatchers.is(1));
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("jedi"), CoreMatchers.notNullValue());
    }

    @Test
    public void followRelationWithCollection() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("GET")) {
                    requests++;
                    return generateResponse(Response.COLLECTION);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        CollectionResult result = traverson
            .follow("jedi")
            .get(CollectionResult.class);

        assertThat(requests, CoreMatchers.is(2));
        assertThat(result.getEmbedded().get("jedi").size(), CoreMatchers.is(2));
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("next"), CoreMatchers.notNullValue());
    }

    @Test
    public void followRelationWithItem() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("GET")) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        ItemResult result = traverson
            .follow("jedi")
            .get(ItemResult.class);

        assertThat(requests, CoreMatchers.is(2));
        assertThat(result.getId(), CoreMatchers.notNullValue());
        assertThat(result.getName(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForSelf(), CoreMatchers.notNullValue());
        assertThat(result.getLinkForRel("lightSaber"), CoreMatchers.notNullValue());
    }
}