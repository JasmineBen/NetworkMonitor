package com.conan.networkmonitor;

import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FloatingView extends LinearLayout {
    public static final String TAG = "FloatingView";

    private final Context mContext;
    private final WindowManager mWindowManager;
    private TextView mTvClassName;
    private TextView mTvRx;
    private TextView mTvTx;
    private TextView mTvTotal;
    private ImageView mIvClose;

    private NetworkStatsManager networkStatsManager;
    private NetworkStatsHelper networkStatsHelper;

    private long lastRx, lastTx, lastTotal;
    private String mPckName;

    public FloatingView(Context context,String pckName) {
        super(context);
        mContext = context;
        mPckName = pckName;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.layout_floating, this);
        mTvClassName = findViewById(R.id.tv_class_name);
        mTvRx = findViewById(R.id.tv_rx);
        mTvTx = findViewById(R.id.tv_tx);
        mTvTotal = findViewById(R.id.tv_total);
        mIvClose = findViewById(R.id.iv_close);

        mIvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "关闭悬浮框", Toast.LENGTH_SHORT).show();
                mContext.startService(
                        new Intent(mContext, MonitorService.class)
                                .putExtra(MonitorService.COMMAND, MonitorService.COMMAND_CLOSE)
                );
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTopClassModified(MonitorService.ActivityChangedEvent event) {
        String packageName = event.getPackageName(),
                className = event.getClassName();
        if (networkStatsManager == null) {
            networkStatsManager = (NetworkStatsManager) mContext.getSystemService(Context.NETWORK_STATS_SERVICE);
            networkStatsHelper = NetworkStatsHelper.getInstance(networkStatsManager);
        }
        if (packageName.contains(mPckName)) {
            long currentTime = System.currentTimeMillis();
            long rx = 0;
            long tx = 0;
            long total = 0;
            int uid = 0;
            if (PackageManagerHelper.isPackage(mContext, packageName)) {
                uid = PackageManagerHelper.getPackageUid(mContext, packageName);

                rx = networkStatsHelper.getAllRxBytes(mContext, uid, currentTime);
                tx = networkStatsHelper.getAllTxBytes(mContext, uid, currentTime);
                total = networkStatsHelper.getAllBytes(mContext, packageName, uid, currentTime);
                Log.d(TAG, "uid:" + uid + ";className:" + className + ";rx:" + rx + ";tx:" + tx + ";total:" + total);
            }
            mTvClassName.setText(
                    (className.startsWith(packageName) ?
                            className.substring(packageName.length()) :
                            className) + "---uid:(" + uid + ")"
            );
            String rxStr = "下行流量：" + AppUtils.getKb(rx);
            if (rx - lastRx > 1024) {
                rxStr += "(+" + AppUtils.getKb(rx - lastRx) + ")";
                lastRx = rx;
            }
            mTvRx.setText(rxStr);
            String txStr = ("上行流量：" + AppUtils.getKb(tx));
            if (tx - lastTx > 1024) {
                txStr += "(+" + AppUtils.getKb(tx - lastTx) + ")";
                lastTx = tx;
            }
            mTvTx.setText(txStr);
            String totalStr = "总流量：" + AppUtils.getKb(total);
            if (total - lastTotal > 1024) {
                totalStr += "(+" + AppUtils.getKb(total - lastTotal) + ")";
                lastTotal = total;
            }
            mTvTotal.setText(totalStr);
        }
    }

    Point preP, curP;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preP = new Point((int) event.getRawX(), (int) event.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:
                curP = new Point((int) event.getRawX(), (int) event.getRawY());
                int dx = curP.x - preP.x,
                        dy = curP.y - preP.y;

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
                layoutParams.x += dx;
                layoutParams.y += dy;
                mWindowManager.updateViewLayout(this, layoutParams);

                preP = curP;
                break;
        }

        return false;
    }
}
