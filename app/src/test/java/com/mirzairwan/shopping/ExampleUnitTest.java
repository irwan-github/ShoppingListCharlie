package com.mirzairwan.shopping;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Test
    public void addition_isCorrect() throws Exception
    {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void checkZeroValue()
    {
        boolean result = Double.parseDouble("0") == 0;
        System.out.println("result is " + result);
        assertTrue("Zero value", result);

    }
}