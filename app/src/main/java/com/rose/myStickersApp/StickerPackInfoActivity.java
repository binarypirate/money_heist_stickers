/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.rose.myStickersApp;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class StickerPackInfoActivity extends BaseActivity {

//    Todo: just bannre ads
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private static final String TAG = "StickerPackInfoActivity";
//    TODO impliment just banner here;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_info);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        setInterstitialAd();
//
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        AdRequest adRequest = new AdRequest.Builder().build();

        displayIntersttialAds();

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Toast.makeText(StickerPackInfoActivity.this, "Add Loaded", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });

        Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        setInterstitialAd();
                    }
                }, 1000L);

        final String trayIconUriString = getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_TRAY_ICON);
        final String website = getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_WEBSITE);
        final String email = getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_EMAIL);
        final String privacyPolicy = getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_PRIVACY_POLICY);
        final String licenseAgreement = getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_LICENSE_AGREEMENT);
        final TextView trayIcon = findViewById(R.id.tray_icon);
        try {
            final InputStream inputStream = getContentResolver().openInputStream(Uri.parse(trayIconUriString));
            final BitmapDrawable trayDrawable = new BitmapDrawable(getResources(), inputStream);
            final Drawable emailDrawable = getDrawableForAllAPIs(R.drawable.sticker_3rdparty_email);
            trayDrawable.setBounds(new Rect(0, 0, emailDrawable.getIntrinsicWidth(), emailDrawable.getIntrinsicHeight()));
            if (Build.VERSION.SDK_INT > 17) {
                trayIcon.setCompoundDrawablesRelative(trayDrawable, null, null, null);
            } else {
                if (ViewCompat.getLayoutDirection(trayIcon) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    trayIcon.setCompoundDrawables(null, null, trayDrawable, null);
                } else {
                    trayIcon.setCompoundDrawables(trayDrawable, null, null, null);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "could not find the uri for the tray image:" + trayIconUriString);
        }

        setupTextView(website, R.id.view_webpage);

        final TextView sendEmail = findViewById(R.id.send_email);
        if (TextUtils.isEmpty(email)) {
            sendEmail.setVisibility(View.GONE);
        } else {
            sendEmail.setOnClickListener(v -> launchEmailClient(email));
        }

        setupTextView(privacyPolicy, R.id.privacy_policy);

        setupTextView(licenseAgreement, R.id.license_agreement);
    }
    public void setInterstitialAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(StickerPackInfoActivity.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd != null){
            mInterstitialAd.show(StickerPackInfoActivity.this);
        }
        super.onBackPressed();
    }

    private void displayIntersttialAds(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(StickerPackInfoActivity.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }

    private void setupTextView(String website, @IdRes int textViewResId) {
        final TextView viewWebpage = findViewById(textViewResId);
        if (TextUtils.isEmpty(website)) {
            viewWebpage.setVisibility(View.GONE);
        } else {
            viewWebpage.setOnClickListener(v -> launchWebpage(website));
        }
    }

    private void launchEmailClient(String email) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", email, null));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.info_send_email_to_prompt)));
    }

    private void launchWebpage(String website) {
        Uri uri = Uri.parse(website);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private Drawable getDrawableForAllAPIs(@DrawableRes int id) {
        if (Build.VERSION.SDK_INT >= 21) {
            return getDrawable(id);
        } else {
            return getResources().getDrawable(id);
        }
    }
}
