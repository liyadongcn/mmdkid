package com.mmdkid.mmdkid;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.ActionLog;
import com.mmdkid.mmdkid.singleton.ActionLogs;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class AppTest {
private static final String TAG = "AppTest";
    @Test
    public void setLogs() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        for (int i =0; i < 15 ; i++){
            ActionLog log = new ActionLog();
            log.mModelType = "post";
            log.mModelID = i+1;
            log.mUserID =2;
            log.mAction = ActionLog.ACTION_VIEW;
            log.mElasticDocID = Utility.getRandomString(15);
            ActionLogs.getInstance(appContext).add(log);
        }
        App app = (App)appContext.getApplicationContext();
        app.setLogs(ActionLogs.getInstance(appContext).getLogList());
    }

    @Test
    public void getLogs() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        App app = (App)appContext.getApplicationContext();
        ArrayList<ActionLog> logList = app.getLogs();
        for (ActionLog log: logList){
            Log.d(TAG,"Get one log : "
                    + log.mUserID
                    + ">>>" + log.mAction
                    + ">>>" + log.mElasticDocID
                    + ">>>" + log.mModelType
                    + ">>>" + log.mModelID
                    + ">>>" + log.mContentLength
                    + ">>>" + log.mStartTimestamp/1000
                    + ">>>" + log.mStopTimestamp/1000
                    + ">>>" + log.mDuration/1000
                    + ">>>" + Double.toString(log.mReadingSpeed));
        }
    }

    @Test
    public void clearLogs() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        App app = (App)appContext.getApplicationContext();
        app.clearLogs();
    }
}