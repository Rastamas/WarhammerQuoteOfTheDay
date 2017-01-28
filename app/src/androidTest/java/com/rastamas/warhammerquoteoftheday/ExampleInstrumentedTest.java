package com.rastamas.warhammerquoteoftheday;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    Context context;

    @Before
    public void setUp() throws Exception{
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void put_get_check_isCorrect() throws Exception{
        DBAdapter mDBAdapter = new DBAdapter(context);
        mDBAdapter.open();
        mDBAdapter.putQuote("2017Jan25", "0", "Derp");
        assertTrue(mDBAdapter.quoteExists("2017Jan25"));
        assertEquals("", "Derp", mDBAdapter.getQuote("2017Jan25"));
        mDBAdapter.deleteQuote("2017Jan25");
        assertFalse(mDBAdapter.quoteExists("2017Jan25"));
        mDBAdapter.close();
    }
}
