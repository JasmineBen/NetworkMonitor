package com.conan.networkmonitor;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import org.greenrobot.eventbus.EventBus;


public class MonitorService extends AccessibilityService {
    public static final String COMMAND = "COMMAND";
    public static final String COMMAND_OPEN = "COMMAND_OPEN";
    public static final String COMMAND_CLOSE = "COMMAND_CLOSE";
    public static final String PACKAGE_NAME = "PACKAGE_NAME";
    MonitorWindowManager mMonitorWindowManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void initMonitorWindowManager(){
        if(mMonitorWindowManager == null)
            mMonitorWindowManager = new MonitorWindowManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initMonitorWindowManager();

        String command = intent.getStringExtra(COMMAND);
        String pckName = intent.getStringExtra(PACKAGE_NAME);
        if(command != null) {
            if (command.equals(COMMAND_OPEN))
                mMonitorWindowManager.addView(pckName);
            else if (command.equals(COMMAND_CLOSE))
                mMonitorWindowManager.removeView();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            EventBus.getDefault().post(new ActivityChangedEvent(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            ));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static class ActivityChangedEvent{
        private final String mPackageName;
        private final String mClassName;

        public ActivityChangedEvent(String packageName, String className) {
            mPackageName = packageName;
            mClassName = className;
        }

        public String getPackageName() {
            return mPackageName;
        }

        public String getClassName() {
            return mClassName;
        }
    }
}
