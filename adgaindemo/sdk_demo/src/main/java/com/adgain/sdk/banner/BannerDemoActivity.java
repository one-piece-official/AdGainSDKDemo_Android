package com.adgain.sdk.banner;

import static com.adgain.sdk.utils.TimeUtils.getDateTimeFormat;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adgain.sdk.Constants;
import com.adgain.sdk.utils.UIUtil;
import com.adgain.sdk.api.AdError;
import com.adgain.sdk.api.AdRequest;
import com.adgain.sdk.api.BannerAd;
import com.adgain.sdk.api.BannerAdListener;
import com.adgain.demo.android.R;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BannerDemoActivity extends AppCompatActivity implements BannerAdListener, View.OnClickListener {

    private final Map<Integer, BannerAd> bannerAdMap = new HashMap<>();
    private LinearLayout adButtonsLayout;
    private TextView logView;
    private FrameLayout bannerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_banner);

        adButtonsLayout = findViewById(R.id.ad_buttons_layout);
        logView = findViewById(R.id.logView);
        bannerContainer = findViewById(R.id.banner_container);

        logView.setOnLongClickListener(v -> true);
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());

        WebView.setWebContentsDebuggingEnabled(true);

        addAdButtons();
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        String text = (String) button.getText();
        String codeID = text.substring(text.indexOf("-") + 1);
        Log.d(Constants.LOG_TAG, "banner ---------onClick---------" + text);

        if (text.startsWith("banner LOAD-")) {
            loadAd(codeID);
        } else if (text.startsWith("banner SHOW-")) {
            showAd();
        } else if (text.startsWith("banner DESTROY-")) {
            destroyAd();
        }
    }

    BannerAd mBannerAd;

    private void loadAd(String codeID) {
        Log.d(Constants.LOG_TAG, "banner ---------loadAd---------");
        logMessage("加载 Banner 广告");

        if (mBannerAd != null) {
            mBannerAd.destroyAd();
            bannerAdMap.remove(mBannerAd.hashCode());
        }

        AdRequest adRequest = new AdRequest.Builder()
                .setCodeId(codeID)
                .build();

        mBannerAd = new BannerAd(adRequest, this, true, true);
        bannerAdMap.put(mBannerAd.hashCode(), mBannerAd);

        mBannerAd.loadAd();
    }

    private void showAd() {
        Log.d(Constants.LOG_TAG, "banner ---------showAd---------");

        if (mBannerAd != null) {
            if (mBannerAd.isReady()) {

                View bannerView = mBannerAd.getBannerView();
                if (bannerView != null) {
                    bannerContainer.addView(bannerView, getLayoutParams());
                }


            } else {
                logMessage("Banner 广告未准备好，请先加载");
            }
        } else {
            logMessage("Banner 广告未创建，请先点击 LOAD");
        }
    }

    private static  FrameLayout.LayoutParams getLayoutParams() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = android.view.Gravity.CENTER;
        return layoutParams;
    }

    private void destroyAd() {
        if (mBannerAd != null) {
            mBannerAd.destroyAd();
            logMessage("销毁 Banner");
        } else {
            logMessage("Banner 未加载");
        }
    }

    public void buttonClick(View view) {
        if (view.getId() == R.id.cleanLog_button) {
            cleanLog();
        }
    }

    private void addAdButtons() {
        try {
            String codeID = Constants.BANNER_ADCOID;
            // 创建加载按钮
            UIUtil.createAdButtonsLayout(this, "banner", codeID, adButtonsLayout, this);

            // 添加销毁按钮
            Button destroyButton = new Button(this);
            destroyButton.setText("banner DESTROY-" + codeID);
            destroyButton.setOnClickListener(this);
            adButtonsLayout.addView(destroyButton);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(Constants.LOG_TAG, "-----------addAdButtons-----------");
    }

    private void cleanLog() {
        logView.setText("");
    }

    private void logMessage(String message) {

        Date date = new Date();
        logView.append(getDateTimeFormat().format(date) + " " + message + '\n');
    }

    @Override
    public void onBannerAdLoadSuccess() {
        Log.d(Constants.LOG_TAG, "------Banner----onBannerAdLoadSuccess---------- " + mBannerAd.getExtraInfo() + " " + mBannerAd.getBidPrice());
        logMessage("✅ Banner 加载成功！");
    }

    @Override
    public void onBannerAdShow() {
        Log.d(Constants.LOG_TAG, "----------onBannerAdShow----------");
        logMessage("🎉 onBannerAdShow");
    }

    @Override
    public void onBannerAdClick() {
        Log.d(Constants.LOG_TAG, "----------onBannerAdClick----------");
        logMessage("onBannerAdClick ");
    }

    @Override
    public void onBannerAdClosed() {
        Log.d(Constants.LOG_TAG, "----------onBannerAdClosed----------");
        logMessage("onBannerAdClosed ");
    }

    @Override
    public void onBannerAdLoadError(AdError error) {
        Log.d(Constants.LOG_TAG, "----------onBannerAdLoadError----------" + error.toString() + ":");
        logMessage("onBannerAdLoadError() called with: error = [" + error + "]");
    }

    @Override
    public void onBannerAdShowError(AdError adError) {
        Log.d(Constants.LOG_TAG, "----------onBannerAdShowError----------" + adError.toString() + ":");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (BannerAd bannerAd : bannerAdMap.values()) {
            if (bannerAd != null) {
                Log.d(Constants.LOG_TAG, "banner onDestroy == " + bannerAd);
                bannerAd.destroyAd();
            }
        }
    }
}
