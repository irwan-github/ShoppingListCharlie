package com.mirzairwan.shopping;

import org.junit.Before;
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

        private boolean mProcessingChange;
        private boolean mContentChanged;

        @Before
        public void setup()
        {
                mProcessingChange = false;
                mContentChanged = false;
        }

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

        @Test
        public void checkBoolean()
        {
                mContentChanged = true;
                boolean res = mContentChanged;
                mContentChanged = false;
                mProcessingChange |= res;

        }

        @Test
        public void checkBoolean2()
        {
                boolean a = true;
                boolean res = a | a;
                boolean res2 = a & a;

                boolean res3 = a & false;
                boolean res4 = a & true;

                boolean res5 = a | false;
                boolean res6 = a | true;

        }
}