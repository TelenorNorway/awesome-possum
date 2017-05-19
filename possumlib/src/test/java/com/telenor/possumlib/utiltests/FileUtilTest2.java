package com.telenor.possumlib.utiltests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Attempt to solve problems without Robolectric, using pure android
 */
//@RunWith(AndroidJUnit4.class)
//@SmallTest
public class FileUtilTest2 {
    private File file;
    private List<String> array1 = new ArrayList<>();
    private List<String> array2 = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 2000; i++) {
            array1.add("test");
            array2.add("test2");
        }
    }

    @After
    public void tearDown() throws Exception {
        array1.clear();
        array2.clear();
    }

    @Test
    public void testWriteConcurrently() throws Exception {

    }
}