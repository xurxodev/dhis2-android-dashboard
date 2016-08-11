/*
 * Copyright (c) 2016, University of Oslo
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

package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.views.fragments.BaseDialogFragment;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationComment;
import org.hisp.dhis.client.sdk.ui.views.FontButton;

public class InterpretationCommentEditFragment extends BaseDialogFragment {
    private static final String TAG = InterpretationCommentEditFragment.class.getSimpleName();
    private static final String ARG_INTERPRETATION_COMMENT_ID = "arg:interpretationCommentId";

    EditText mCommentEditText;

    TextView mDialogLabel;

    InterpretationComment mInterpretationComment;

    ImageView mCloseDialogButton ;
    FontButton mCancelInterpretationCommentEdit;
    FontButton mUpdateInterpretationComment;

    public static InterpretationCommentEditFragment newInstance(long commentId) {
        Bundle args = new Bundle();
        args.putLong(ARG_INTERPRETATION_COMMENT_ID, commentId);

        InterpretationCommentEditFragment fragment
                = new InterpretationCommentEditFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_interpretation_comment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mDialogLabel = (TextView) view.findViewById(R.id.dialog_label);
        mCommentEditText = (EditText) view.findViewById(R.id.interpretation_comment_edit_text);


        mCloseDialogButton = (ImageView) view.findViewById(R.id.close_dialog_button);
        mCancelInterpretationCommentEdit = (FontButton) view.findViewById(R.id.cancel_interpretation_comment_edit);
        mUpdateInterpretationComment = (FontButton) view.findViewById(R.id.update_interpretation_comment);

        mCloseDialogButton.setOnClickListener(onClickListener);
        mCancelInterpretationCommentEdit.setOnClickListener(onClickListener);
        mUpdateInterpretationComment.setOnClickListener(onClickListener);

        mInterpretationComment = new Select()
                .from(InterpretationComment.class)
                .where(Condition.column(InterpretationComment$Table
                        .ID).is(getArguments().getLong(ARG_INTERPRETATION_COMMENT_ID)))
                .querySingle();

        mDialogLabel.setText(getString(R.string.edit_comment));
        mCommentEditText.setText(mInterpretationComment.getText());
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.update_interpretation_comment: {
                    mInterpretationComment.updateComment(
                            mCommentEditText.getText().toString());

                    if (isDhisServiceBound()) {
                        getDhisService().syncInterpretations();
                    }
                    break;
                }
            }

            // hide keyboard before closing dialog.
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(
                    mCommentEditText.getWindowToken(), 0);
            dismiss();
        }
    };

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }
}