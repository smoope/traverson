package com.smoope.utils.traverson;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

public class TraversonJsonHalDeleteTest extends AbstractJsonHalTest {

    @Test
    public void followUrl() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getMethod().equals("DELETE")) {
                    requests++;
                    return generateResponse(Response._204);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        traverson.followUri(baseUrl).delete();

        Assert.assertThat(requests, CoreMatchers.is(1));
    }

    @Test
    public void followRoot() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api") && request.getMethod().equals("DELETE")) {
                    requests++;
                    return generateResponse(Response._204);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        traverson.follow().delete();

        Assert.assertThat(requests, CoreMatchers.is(1));
    }

    @Test
    public void followRelation() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("DELETE")) {
                    requests++;
                    return generateResponse(Response._204);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        traverson.follow("jedi").delete();

        Assert.assertThat(requests, CoreMatchers.is(2));
    }
}