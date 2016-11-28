package com.smoope.utils.traverson;


import lombok.Getter;
import lombok.Setter;

import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public abstract class AbstractTraversonTest {

    public enum Response {
        ROOT,
        COLLECTION,
        ITEM,
        OAUTH,
        OAUTH_ERROR,
        _201,
        _204,
        _400,
        _401,
        _403,
        _404,
        _405,
        _500,
        _503,
        _307
    }

    @Getter
    @Setter
    public class ItemResult extends TraversonResult<HashMap> {
        private String id;

        private String name;
    }

    @Getter
    @Setter
    public class CollectionResult extends TraversonResult<List<ItemResult>> {

    }

    private static String DEFAULT_CONTENT_TYPE = "application/hal+json; charset=utf-8";

    protected String authUrl;

    protected String baseUrl;

    protected Traverson traverson;

    protected int requests;

    @Rule
    public final MockWebServer server = new MockWebServer();

    @Before
    public void setUp() {
        authUrl = server.url("/auth").toString();
        baseUrl = server.url("/api").toString();
        requests = 0;
    }

    public MockResponse generateResponse(final Response response) {
        switch (response) {
            case ROOT:
                return new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", DEFAULT_CONTENT_TYPE)
                        .addHeader("Cache-Control", "no-store, max-age=86400")
                        .setBody(getResponse("root.hal"));
            case COLLECTION:
                return new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", DEFAULT_CONTENT_TYPE)
                    .addHeader("Cache-Control", "no-store, max-age=86400")
                    .setBody(getResponse("collection.hal"));
            case ITEM:
                return new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", DEFAULT_CONTENT_TYPE)
                    .addHeader("Cache-Control", "no-store, max-age=86400")
                    .setBody(getResponse("item.hal"));
            case OAUTH:
                return new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", DEFAULT_CONTENT_TYPE)
                    .addHeader("Cache-Control", "no-cache")
                    .setBody(getResponse("oauth"));
            case OAUTH_ERROR:
                return new MockResponse()
                    .setResponseCode(400)
                    .addHeader("Content-Type", DEFAULT_CONTENT_TYPE)
                    .addHeader("Cache-Control", "no-cache")
                    .setBody(getResponse("oauthError"));
            case _201:
                return new MockResponse()
                    .setResponseCode(201)

                    .addHeader("Location", baseUrl + "/created");
            case _204:
                return new MockResponse().setResponseCode(204);
            case _400:
                return new MockResponse().setResponseCode(400);
            case _401:
                return new MockResponse().setResponseCode(401);
            case _403:
                return new MockResponse().setResponseCode(403);
            case _405:
                return new MockResponse().setResponseCode(404);
            case _500:
                return new MockResponse().setResponseCode(500);
            case _503:
                return new MockResponse().setResponseCode(503);
            case _307:
                return new MockResponse()
                        .setResponseCode(307)
                        .addHeader("Location", baseUrl + "/media");
            default: return new MockResponse().setResponseCode(404);
        }
    }

    protected File getMedia() {
        URL resourceUrl = getClass().getResource("/temp.png");
        File file = null;
        try {
            file = new File(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return file;
    }

    private String getResponse(final String name) {
        URL resourceUrl = getClass().getResource(String.format("/responses/%s.json", name));

        String response = "";
        try {
            File file = new File(resourceUrl.toURI());
            Scanner scanner = new Scanner(file);
            response = scanner.useDelimiter("\\A").next();
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.replace("http://old-republic.com", baseUrl);
    }
}
