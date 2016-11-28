package com.smoope.utils.traverson;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

public class TraversonJsonHalPostTest extends AbstractJsonHalTest {

    @Test
    public void followUrl() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api") && request.getBody().readUtf8() != null
                        && request.getMethod().equals("POST")) {
                    requests++;
                    return generateResponse(Response._201);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        okhttp3.Response result = traverson
            .followUri(baseUrl)
            .post(new Object());

        assertThat(requests, CoreMatchers.is(1));
        assertThat(result.header("Location"), CoreMatchers.notNullValue());

    }

    @Test
    public void followRoot() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api") && request.getBody().readUtf8() != null
                        && request.getMethod().equals("POST")) {
                    requests++;
                    return generateResponse(Response._201);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        okhttp3.Response result = traverson
            .follow()
            .post(new Object());

        assertThat(requests, CoreMatchers.is(1));
        assertThat(result.header("Location"), CoreMatchers.notNullValue());
    }

    @Test
    public void followRelation() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("POST")
                        && request.getBody().readUtf8() != null) {
                    requests++;
                    return generateResponse(Response._201);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        okhttp3.Response result = traverson
            .follow("jedi")
            .post(new Object());

        assertThat(requests, CoreMatchers.is(2));
        assertThat(result.header("Location"), CoreMatchers.notNullValue());
    }
}
