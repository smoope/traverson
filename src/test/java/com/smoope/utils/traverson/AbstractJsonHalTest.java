package com.smoope.utils.traverson;

import org.junit.Before;

public class AbstractJsonHalTest extends AbstractTraversonTest {

    @Before
    public void setUp() {
        super.setUp();

        traverson = new Traverson.Builder(baseUrl).build();
    }
}