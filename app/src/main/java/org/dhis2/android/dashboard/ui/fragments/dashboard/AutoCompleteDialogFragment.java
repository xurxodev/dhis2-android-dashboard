/*
 * Copyright (c) 2015, University of Oslo
 * All rights reserved.
 *
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

package org.dhis2.android.dashboard.ui.fragments.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.builder.Condition.CombinedCondition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

import org.dhis2.android.dashboard.R;
import org.dhis2.android.dashboard.api.models.DashboardItemContent;
import org.dhis2.android.dashboard.api.models.DashboardItemContent$Table;
import org.dhis2.android.dashboard.api.persistence.loaders.DbLoader;
import org.dhis2.android.dashboard.api.persistence.loaders.Query;
import org.dhis2.android.dashboard.ui.adapters.AutoCompleteDialogAdapter;
import org.dhis2.android.dashboard.ui.adapters.AutoCompleteDialogAdapter.OptionAdapterValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;

import static com.raizlabs.android.dbflow.sql.builder.Condition.column;

public class AutoCompleteDialogFragment extends DialogFragment
        implements PopupMenu.OnMenuItemClickListener, LoaderCallbacks<List<OptionAdapterValue>> {
    private static final String TAG = AutoCompleteDialogFragment.class.getSimpleName();
    private static final int LOADER_ID = 3451234;

    public static final int DIALOG_ID = 234235;

    @InjectView(R.id.filter_options) EditText mFilter;
    @InjectView(R.id.dialog_label) TextView mDialogLabel;
    @InjectView(R.id.simple_listview) ListView mListView;
    @InjectView(R.id.filter_resources) ImageView mFilterResources;

    PopupMenu mResourcesMenu;
    AutoCompleteDialogAdapter mAdapter;
    OnOptionSelectedListener mListener;

    public static AutoCompleteDialogFragment newInstance(OnOptionSelectedListener listener) {
        AutoCompleteDialogFragment fragment
                = new AutoCompleteDialogFragment();
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_auto_complete, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.inject(this, view);

        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFilter.getWindowToken(), 0);

        mAdapter = new AutoCompleteDialogAdapter(
                LayoutInflater.from(getActivity()));
        mListView.setAdapter(mAdapter);
        mDialogLabel.setText(getString(R.string.add_dashboard_item));

        mResourcesMenu = new PopupMenu(getActivity(), mFilterResources);
        mResourcesMenu.inflate(R.menu.menu_filter_resources);
        mResourcesMenu.setOnMenuItemClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryApiResources();
    }

    @OnTextChanged(value = R.id.filter_options,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable s) {
        mAdapter.getFilter().filter(s.toString());
    }

    @OnClick({R.id.close_dialog_button, R.id.filter_resources})
    public void onButtonClick(View v) {
        if (R.id.close_dialog_button == v.getId()) {
            dismiss();
        } else if (R.id.filter_resources == v.getId()) {
            mResourcesMenu.show();
        }
    }

    @OnItemClick(R.id.simple_listview)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            AutoCompleteDialogAdapter.OptionAdapterValue value = mAdapter.getItem(position);
            if (value != null) {
                mListener.onOptionSelected(DIALOG_ID, position, value.id, value.label);
            }
        }

        dismiss();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        item.setChecked(!item.isChecked());
        queryApiResources();
        return false;
    }

    @Override
    public Loader<List<OptionAdapterValue>> onCreateLoader(int id, Bundle args) {
        List<Class<? extends Model>> tablesToTrack = new ArrayList<>();
        tablesToTrack.add(DashboardItemContent.class);
        return new DbLoader<>(getActivity().getApplicationContext(),
                tablesToTrack, new DbQuery(getTypesToInclude()));
    }

    @Override
    public void onLoadFinished(Loader<List<OptionAdapterValue>> loader,
                               List<OptionAdapterValue> data) {
        if (loader != null && loader.getId() == LOADER_ID) {
            mAdapter.swapData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<OptionAdapterValue>> loader) {
        if (loader != null && loader.getId() == LOADER_ID) {
            mAdapter.swapData(null);
        }
    }

    private void queryApiResources() {
        getLoaderManager().restartLoader(LOADER_ID, getArguments(), this);
    }

    private List<String> getTypesToInclude() {
        List<String> typesToInclude = new ArrayList<>();
        if (isItemChecked(R.id.type_charts)) {
            typesToInclude.add(DashboardItemContent.TYPE_CHART);
        }
        if (isItemChecked(R.id.type_event_charts)) {
            typesToInclude.add(DashboardItemContent.TYPE_EVENT_CHART);
        }
        if (isItemChecked(R.id.type_maps)) {
            typesToInclude.add(DashboardItemContent.TYPE_MAP);
        }
        if (isItemChecked(R.id.type_report_tables)) {
            typesToInclude.add(DashboardItemContent.TYPE_REPORT_TABLES);
        }
        if (isItemChecked(R.id.type_event_reports)) {
            typesToInclude.add(DashboardItemContent.TYPE_EVENT_REPORT);
        }
        if (isItemChecked(R.id.type_users)) {
            typesToInclude.add(DashboardItemContent.TYPE_USERS);
        }
        if (isItemChecked(R.id.type_reports)) {
            typesToInclude.add(DashboardItemContent.TYPE_REPORTS);
        }
        if (isItemChecked(R.id.type_resources)) {
            typesToInclude.add(DashboardItemContent.TYPE_RESOURCES);
        }

        return typesToInclude;
    }

    private boolean isItemChecked(int id) {
        return mResourcesMenu.getMenu().findItem(id).isChecked();
    }

    static class DbQuery implements Query<List<OptionAdapterValue>> {
        private List<String> mTypes;

        public DbQuery(List<String> types) {
            mTypes = types;
        }

        @Override
        public List<OptionAdapterValue> query(Context context) {
            if (mTypes.isEmpty()) {
                return new ArrayList<>();
            }

            CombinedCondition generalCondition =
                    CombinedCondition.begin(column(DashboardItemContent$Table.TYPE).isNotNull());
            CombinedCondition columnConditions = null;
            for (String type : mTypes) {
                if (columnConditions == null) {
                    columnConditions = CombinedCondition
                            .begin(column(DashboardItemContent$Table.TYPE).is(type));
                } else {
                    columnConditions = columnConditions
                            .or(column(DashboardItemContent$Table.TYPE).is(type));
                }
            }
            generalCondition.and(columnConditions);

            List<DashboardItemContent> resources = new Select().from(DashboardItemContent.class)
                    .where(generalCondition).queryList();
            Collections.sort(resources, DashboardItemContent.DISPLAY_NAME_COMPARATOR);

            List<OptionAdapterValue> adapterValues = new ArrayList<>();
            for (DashboardItemContent dashboardItemContent : resources) {
                adapterValues.add(new OptionAdapterValue(dashboardItemContent.getUId(),
                        dashboardItemContent.getDisplayName()));
            }

            return adapterValues;
        }
    }

    public interface OnOptionSelectedListener {
        void onOptionSelected(int dialogId, int position, String id, String name);
    }
}