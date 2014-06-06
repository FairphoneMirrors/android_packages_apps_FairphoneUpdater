/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fairphone.updater;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VersionListActivity extends Activity
{

    public static final String AOSP_VERSIONS = "AOSP";

    public static final String FAIRPHONE_VERSIONS = "FAIRPHONE";

    public static final String VERSION_LIST_TYPE = "VERSION_LIST_TYPE";

    private LinearLayout mVersionListContainer;

    private List<Version> mVersionList;

    private TextView mVersionListHeaderTitle;

    private View mScrollView;

    private View mSelectedVersionLayout;

    private LinearLayout mMoreInfoLayout;

    private TextView mReleaseNotesText;

    private LinearLayout mMoreInfoButtonsLayout;

    private Button mMoreInfoCancelButton;

    private Button mMoreInfoActionButton;

    private ImageView mSelectedVersionImage;

    private SharedPreferences mSharedPreferences;

    private String mVersionListType;

    private TextView mVersionListHeaderSubTitle;

	private ImageView mMoreInfoFairphoneLogo;

	private ImageView mMoreInfoAndroidLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_list);

        mSharedPreferences = getSharedPreferences(FairphoneUpdater.FAIRPHONE_UPDATER_PREFERENCES, MODE_PRIVATE);

        Intent i = getIntent();
        mVersionListType = i.getStringExtra(VERSION_LIST_TYPE);

        mVersionListContainer = (LinearLayout) findViewById(R.id.versionListContainer);

        mVersionListHeaderTitle = (TextView) findViewById(R.id.titleText);
        mVersionListHeaderSubTitle = (TextView) findViewById(R.id.subtitleText);
        setupTitleBar();

        mVersionListHeaderSubTitle.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        mScrollView = findViewById(R.id.versionListScroll);
        mSelectedVersionLayout = findViewById(R.id.selectedVersionContainer);
        mSelectedVersionImage = (ImageView) findViewById(R.id.selectedVersionImage);
        setupMoreInfoLayout();
        setupScrollList();
    }

    public void setupTitleBar()
    {
        Resources resources = getResources();
        mVersionListHeaderSubTitle.setText(Version.getImageTypeDescription(mVersionListType, resources));
        mVersionListHeaderSubTitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed()
    {
        if (mScrollView.getVisibility() == View.VISIBLE)
        {
            super.onBackPressed();
        }
        else
        {
            setupTitleBar();
            toggleVersionDetails();
        }
    }

    public void setupScrollList()
    {
        LinearLayout versionLayout;
        LayoutInflater inflater = getLayoutInflater();

        int listItemLayoutId = R.layout.version_list_item_fairphone;
        if (AOSP_VERSIONS.equalsIgnoreCase(mVersionListType))
        {
            mVersionList = UpdaterData.getInstance().getAOSPVersionList();
            listItemLayoutId = R.layout.version_list_item_android;
        }
        else if (FAIRPHONE_VERSIONS.equalsIgnoreCase(mVersionListType))
        {
            mVersionList = UpdaterData.getInstance().getFairphoneVersionList();
            listItemLayoutId = R.layout.version_list_item_fairphone;
        }

        for (Version version : mVersionList)
        {
            versionLayout = (LinearLayout) inflater.inflate(listItemLayoutId, mVersionListContainer, false);
            versionLayout.setTag(version);
            versionLayout.setClickable(true);

            TextView versionName = (TextView) versionLayout.findViewById(R.id.versionNameText);
            versionName.setText(version.getName());

            //ImageView versionImage = (ImageView) versionLayout.findViewById(R.id.versionImage);
            //Picasso.with(this).load(version.getThumbnailLink()).placeholder(R.drawable.fairphone_updater_current_version).into(versionImage);

            versionLayout.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Version selectedVersion = (Version) v.getTag();

                    if (selectedVersion != null)
                    {
                        toggleVersionDetails();

                        mVersionListHeaderSubTitle.setText(selectedVersion.getName());

                        //                        Picasso.with(getApplicationContext()).load(selectedVersion.getThumbnailLink())
                        //                                .placeholder(R.drawable.fairphone_updater_current_version).into(mSelectedVersionImage);

                        updateMoreInfoLayout(selectedVersion);
                    }
                }
            });

            mVersionListContainer.addView(versionLayout);
        }
    }

    public void setupMoreInfoLayout()
    {
        mMoreInfoLayout = (LinearLayout) mSelectedVersionLayout.findViewById(R.id.moreInfoLayout);
        mMoreInfoFairphoneLogo = (ImageView)findViewById(R.id.updateLogoFairphone);
        mMoreInfoAndroidLogo = (ImageView)findViewById(R.id.updateLogoAndroid);
        mReleaseNotesText = (TextView) mSelectedVersionLayout.findViewById(R.id.releaseNotesText);
        mMoreInfoButtonsLayout = (LinearLayout) mSelectedVersionLayout.findViewById(R.id.actionButtonsContainer);
        mMoreInfoCancelButton = (Button) mSelectedVersionLayout.findViewById(R.id.cancelButton);
        mMoreInfoActionButton = (Button) mSelectedVersionLayout.findViewById(R.id.actionButton);
    }

    public void updateMoreInfoLayout(Version selectedVersion)
    {

        mReleaseNotesText.setText(selectedVersion.getReleaseNotes() + "\n");
        if(Version.IMAGE_TYPE_FAIRPHONE.equalsIgnoreCase(selectedVersion.getImageType())){
        	mMoreInfoFairphoneLogo.setVisibility(View.VISIBLE);
            mMoreInfoAndroidLogo.setVisibility(View.GONE);
        }else if(Version.IMAGE_TYPE_AOSP.equalsIgnoreCase(selectedVersion.getImageType())){
        	mMoreInfoFairphoneLogo.setVisibility(View.GONE);
            mMoreInfoAndroidLogo.setVisibility(View.VISIBLE);
        }
        mMoreInfoButtonsLayout.setVisibility(View.VISIBLE);
        mMoreInfoCancelButton.setVisibility(View.GONE);
        mMoreInfoActionButton.setVisibility(View.VISIBLE);

        updateMoreInfoActionButton(selectedVersion);
    }

    public void updateMoreInfoActionButton(final Version selectedVersion)
    {

        mMoreInfoActionButton.setText(R.string.downloadAndUpdateVersionBtn);
        mMoreInfoActionButton.setEnabled(true);

        mMoreInfoActionButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {

                int versionNumber = selectedVersion != null ? selectedVersion.getNumber() : 0;
                String versionImageType = selectedVersion != null ? selectedVersion.getImageType() : "";

                Editor editor = mSharedPreferences.edit();
                editor.putInt(FairphoneUpdater.PREFERENCE_SELECTED_VERSION_NUMBER, versionNumber);
                editor.putString(FairphoneUpdater.PREFERENCE_SELECTED_VERSION_TYPE, versionImageType);
                editor.putBoolean(FairphoneUpdater.PREFERENCE_SELECTED_VERSION_BEGIN_DOWNLOAD, true);
                editor.commit();

                finish();
            }
        });
    }

    public void toggleVersionDetails()
    {
        if (mScrollView.getVisibility() == View.VISIBLE)
        {
            mScrollView.setVisibility(View.GONE);
            mSelectedVersionLayout.setVisibility(View.VISIBLE);
            mMoreInfoLayout.setVisibility(View.VISIBLE);
            mMoreInfoButtonsLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            mSelectedVersionLayout.setVisibility(View.GONE);
            mMoreInfoLayout.setVisibility(View.GONE);
            mMoreInfoButtonsLayout.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
        }
    }

}
