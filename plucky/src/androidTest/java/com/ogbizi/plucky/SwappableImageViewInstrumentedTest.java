package com.ogbizi.plucky;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SwappableImageViewInstrumentedTest {

    private SwappableImageView swappableImageView;

    @Before
    public void setUp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        swappableImageView = new SwappableImageView(appContext);
        swappableImageView.setDrawables(1, 1, 2, 3, 4);
        swappableImageView.setLooping(false);
    }

    @After
    public void tearDown() throws Exception {
        swappableImageView = null;
    }

    @Test
    public void setBehavior() {
        SwappableImageBehavior b = mock(SwappableImageBehavior.class);
        swappableImageView.setBehavior(b);
        verify(b).onAttach(swappableImageView);
    }

    @Test
    public void setAndGetDrawables() {
        swappableImageView.setDrawables(1, 1, 2);
        assertEquals(1, swappableImageView.getCurrentIndex());
        List drawables = swappableImageView.getDrawables();
        assertEquals(2, drawables.size());
        assertTrue(drawables.containsAll(Arrays.asList(1, 2)));
    }

    @Test
    public void setNext() {
        int currentIndex = swappableImageView.getCurrentIndex();
        int testResInt = 99;
        swappableImageView.setNext(testResInt);
        List drawables = swappableImageView.getDrawables();
        assertEquals(testResInt, drawables.get(currentIndex + 1));
    }

    @Test
    public void setPrevious() {
        int index = swappableImageView.getCurrentIndex();
        int testResInt = 99;
        swappableImageView.setPrevious(testResInt);
        List drawables = swappableImageView.getDrawables();
        assertEquals(testResInt, drawables.get(index));
    }

    @Test
    public void setIsLooping() {
        boolean looping = !swappableImageView.isLooping();
        swappableImageView.setLooping(looping);
        assertEquals(looping, swappableImageView.isLooping());
    }

    @Test
    public void getIndex() {
        swappableImageView.setLooping(true);
        swappableImageView.setCurrentIndex(0);
        assertEquals(3, swappableImageView.getPreviousIndex());
        assertEquals(1, swappableImageView.getNextIndex());
        swappableImageView.setCurrentIndex(3);
        assertEquals(0, swappableImageView.getNextIndex());
        swappableImageView.setLooping(false);
        assertEquals(3, swappableImageView.getNextIndex());
        assertEquals(2, swappableImageView.getPreviousIndex());
    }

    @Test
    public void showNextAndPrevious() {
        final SwappableImageView swappableImageView = this.swappableImageView;
        final SwappableImageBehavior b1 = mock(SwappableImageBehavior.class);
        final SwappableImageBehavior b2 = mock(SwappableImageBehavior.class);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                swappableImageView.setBehavior(b1);

                swappableImageView.setCurrentIndex(99);
                swappableImageView.showNext(true);
                verify(b1, never()).onStart(anyBoolean(), any(ImageView.class), any(ImageView.class));

                swappableImageView.setCurrentIndex(0);
                swappableImageView.showNext(false);
                verify(b1).onStart(eq(false), any(ImageView.class), any(ImageView.class));

                swappableImageView.setBehavior(b2);

                swappableImageView.setCurrentIndex(0);
                swappableImageView.showNext(false);
                verify(b2, never()).onStart(anyBoolean(), any(ImageView.class), any(ImageView.class));

                swappableImageView.setLooping(true);
                swappableImageView.showPrevious(true);
                verify(b2).onEnd(eq(true), any(ImageView.class), any(ImageView.class));
            }
        }, 0);
    }
}