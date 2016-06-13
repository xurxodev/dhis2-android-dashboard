/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.presenters;

import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardEmptyFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class DashboardEmptyFragmentPresenterImpl implements DashboardEmptyFragmentPresenter {
    private static final String TAG = DashboardEmptyFragmentPresenterImpl.class.getSimpleName();
    private final DashboardInteractor dashboardInteractor;

    private final SessionPreferences sessionPreferences;
    private final SyncDateWrapper syncDateWrapper;
    private final ApiExceptionHandler apiExceptionHandler;
//  TODO          private final SyncWrapper syncWrapper , add to constructor as well
    private final Logger logger;

    private boolean hasSyncedBefore;
    private DashboardEmptyFragmentView dashboardEmptyFragmentView;
    private boolean isSyncing;

    public DashboardEmptyFragmentPresenterImpl(DashboardInteractor dashboardInteractor,
                                               SessionPreferences sessionPreferences,
                                               SyncDateWrapper syncDateWrapper,
                                               ApiExceptionHandler apiExceptionHandler,
                                               Logger logger) {

        this.dashboardInteractor = dashboardInteractor;
        this.sessionPreferences = sessionPreferences;
        this.syncDateWrapper = syncDateWrapper;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;
        this.hasSyncedBefore = false;
    }

    public void attachView(View view) {
        isNull(view, "DashboardEmptyFragmentView must not be null");
        dashboardEmptyFragmentView = (DashboardEmptyFragmentView) view;

        if (isSyncing) {
            dashboardEmptyFragmentView.showProgressBar();
        } else {
            dashboardEmptyFragmentView.hideProgressBar();
        }
        // check if metadata was synced,
        // if not, syncMetaData it
        if (!isSyncing && !hasSyncedBefore) {
            sync();
        }

        // TODO
        doSomethingToLoadDashboards();
    }

    @Override
    public void detachView() {
        dashboardEmptyFragmentView.hideProgressBar();
        dashboardEmptyFragmentView = null;
    }

    @Override
    public void sync() {
        dashboardEmptyFragmentView.showProgressBar();
        isSyncing = true;
       // TODO Syncing code
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        dashboardEmptyFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardEmptyFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardEmptyFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

}
