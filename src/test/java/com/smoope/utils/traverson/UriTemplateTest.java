package com.smoope.utils.traverson;

import com.smoope.utils.traverson.utils.UriTemplate;

import org.junit.Assert;
import org.junit.Test;

public class UriTemplateTest {

    @Test
    public void testPath() {
        UriTemplate template = UriTemplate.fromUri("http://localhost{/key}");
        String result = template.expand("key", "12345").toString();

        Assert.assertEquals(result, "http://localhost/12345");

        template = UriTemplate.fromUri("http://localhost/{key}");
        result = template.expand("key", "12345").toString();

        Assert.assertEquals(result, "http://localhost/12345");
    }

    @Test
    public void testParam() {
        UriTemplate template = UriTemplate.fromUri("http://localhost{?key}");
        String result = template.expand("key", "12345").toString();

        Assert.assertEquals(result, "http://localhost?key=12345");

        template = UriTemplate.fromUri("http://localhost?key={key}");
        result = template.expand("key", "12345").toString();

        Assert.assertEquals(result, "http://localhost?key=12345");

        template = UriTemplate.fromUri("http://localhost?key1=12345{&key2}");
        result = template.expand("key2", "12345").toString();

        Assert.assertEquals(result, "http://localhost?key1=12345&key2=12345");

        template = UriTemplate.fromUri("http://localhost{?key1,key2}");
        result = template.expand("key1", "12345", "key2", "12345").toString();

        Assert.assertEquals(result, "http://localhost?key1=12345&key2=12345");

        template = UriTemplate.fromUri("http://localhost{?key1,key2,key3}");
        result = template.expand("key1", "12345", "key2", "12345", "key3", "12345").toString();

        Assert.assertEquals(result, "http://localhost?key1=12345&key2=12345&key3=12345");

        template = UriTemplate.fromUri("http://localhost{?key1,key2,key3,key4}");
        result = template.expand("key1", "12345", "key2", "12345", "key3", "12345", "key4", "12345").toString();

        Assert.assertEquals(result, "http://localhost?key1=12345&key2=12345&key3=12345&key4=12345");
    }
}
