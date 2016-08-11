/*
 * Copyright (c) 2016, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationEmptyFragmentPresenter;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.inject.Inject;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by laavanye on 1/8/16.
 */
public class InterpretationEmptyFragment extends BaseFragment{
    public static final String TAG = InterpretationEmptyFragment.class.getSimpleName();
    private static final String STATE_IS_LOADING = "state:isLoading";

    // Progress bar
    SmoothProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interpretations_empty, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO to include super or not
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        mProgressBar = (SmoothProgressBar) view.findViewById(R.id.progress_bar);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_LOADING, mProgressBar
                .getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    /**
     * This method will return Toolbar instance of InterpretationContainerFragment
     */
    @Nullable
    protected Toolbar getToolbarOfContainer() {
        if (getParentFragment() != null && getParentFragment() instanceof InterpretationContainerFragment) {
            return ((InterpretationContainerFragment) getParentFragment()).getToolbar();
        }
        return null;
    }

    private void setupToolbar() {
        if (getToolbarOfContainer() != null) {
            getToolbarOfContainer().inflateMenu(R.menu.menu_interpretations_fragment);
            getToolbarOfContainer().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onMenuItemClicked(item);
                }
            });

        }
    }

    public boolean onMenuItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh: {
                syncInterpretations();
                return true;
            }
        }
        return false;
    }

}