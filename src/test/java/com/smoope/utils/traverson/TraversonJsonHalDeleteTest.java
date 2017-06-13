package com.smoope.utils.traverson;

import static com.smoope.utils.traverson.AbstractTraversonTest.Response.ROOT_WITH_EMBEDDED;
import static com.smoope.utils.traverson.AbstractTraversonTest.Response._404;
import static org.junit.Assert.assertThat;

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

    @Test
    public void followMulipleRelationWithItem() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("GET")) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else if (request.getPath().contains("/jedi") && (request.getMethod().equals("DELETE") || request.getMethod().equals("GET"))) {
                    requests++;
                    return generateResponse(Response._204);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        traverson
            .follow("jedi", "self")
            .delete();

        assertThat(requests, CoreMatchers.is(3));
    }

    @Test
    public void followEmbeddedResource() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(ROOT_WITH_EMBEDDED);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("DELETE")) {
                    requests++;
                    return generateResponse(Response._204);
                } else {
                    return generateResponse(_404);
                }
            }
        });

        traverson
            .follow("jedi")
            .delete();

        assertThat(requests, CoreMatchers.is(2));
    }

    @Test
    public void followEmbeddedResourceRelation() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/api")) {
                    requests++;
                    return generateResponse(Response.ROOT_WITH_EMBEDDED);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("GET")) {
                    requests++;
                    return generateResponse(Response.ITEM);
                } else if (request.getPath().contains("/jedi") && request.getMethod().equals("DELETE")) {
                    requests++;
                    return generateResponse(Response._204);
                } else {
                    return generateResponse(Response._404);
                }
            }
        });

        traverson
            .follow("jedi", "self")
            .delete();

        assertThat(requests, CoreMatchers.is(2));
    }
}