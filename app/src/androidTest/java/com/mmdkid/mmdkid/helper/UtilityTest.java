package com.mmdkid.mmdkid.helper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilityTest {

    @Test
    public void uploadDeviceContactInfo() {
        final Context context = InstrumentationRegistry.getTargetContext();
        Utility.uploadDeviceContactInfo(context);
    }
}