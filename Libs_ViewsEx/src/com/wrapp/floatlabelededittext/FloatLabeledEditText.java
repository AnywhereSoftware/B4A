
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package com.wrapp.floatlabelededittext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;


public class FloatLabeledEditText extends FrameLayout {

    private static final int DEFAULT_PADDING_LEFT= 2;
    public OnFocusChangeListener MyFocusListener;
    public TextView mHintTextView;
    public EditText mEditText;
    private Context mContext;

        public FloatLabeledEditText(Context context) {
        super(context);
        mContext = context;
    }

    public FloatLabeledEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setAttributes(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FloatLabeledEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setAttributes(attrs);
    }

    private void setAttributes(AttributeSet attrs) {
        mHintTextView = new TextView(mContext);


        final int padding =  0;
        final int defaultPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PADDING_LEFT, getResources().getDisplayMetrics());
        final int paddingLeft = defaultPadding;
        final int paddingTop =  0;
        final int paddingRight = 0;
        final int paddingBottom =  0;
        Drawable background = null;

        if (padding != 0) {
            mHintTextView.setPadding(padding, padding, padding, padding);
        } else {
            mHintTextView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }

        if (background != null) {
            setHintBackground(background);
        }

        mHintTextView.setTextAppearance(mContext,  android.R.style.TextAppearance_Small);

        //Start hidden
        mHintTextView.setVisibility(INVISIBLE);

        addView(mHintTextView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @SuppressLint("NewApi")
    private void setHintBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mHintTextView.setBackground(background);
        } else {
            mHintTextView.setBackgroundDrawable(background);
        }
    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            if (mEditText != null) {
                throw new IllegalArgumentException("Can only have one Edittext subview");
            }

            final LayoutParams lp = new LayoutParams(params);
            lp.gravity = Gravity.BOTTOM;
            lp.topMargin = (int) (mHintTextView.getTextSize() + mHintTextView.getPaddingBottom() + mHintTextView.getPaddingTop());
            params = lp;

            setEditText((EditText) child);
        }

        super.addView(child, index, params);
    }

    private void setEditText(EditText editText) {
        mEditText = editText;

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                setShowHint(!TextUtils.isEmpty(s));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean gotFocus) {
                onFocusChanged(gotFocus);
            }
        });

        mHintTextView.setText(mEditText.getHint());

        if(!TextUtils.isEmpty(mEditText.getText())){
            mHintTextView.setVisibility(VISIBLE);
        }
    }

    private void onFocusChanged(boolean gotFocus) {
        if (gotFocus && mHintTextView.getVisibility() == VISIBLE) {
            ObjectAnimator.ofFloat(mHintTextView, "alpha", 0.33f, 1f).start();
        } else if (mHintTextView.getVisibility() == VISIBLE) {
            ObjectAnimator.ofFloat(mHintTextView, "alpha", 1f, 0.33f).start();
        }
        if (MyFocusListener != null)
        	MyFocusListener.onFocusChange(null, gotFocus);
    }

    private void setShowHint(final boolean show) {
        AnimatorSet animation = null;
        if ((mHintTextView.getVisibility() == VISIBLE) && !show) {
            animation = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(mHintTextView, "translationY", 0, mHintTextView.getHeight() / 8);
            ObjectAnimator fade = ObjectAnimator.ofFloat(mHintTextView, "alpha", 1, 0);
            animation.playTogether(move, fade);
        } else if ((mHintTextView.getVisibility() != VISIBLE) && show) {
            animation = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(mHintTextView, "translationY", mHintTextView.getHeight() / 8, 0);
            ObjectAnimator fade;
            if (mEditText.isFocused()) {
                fade = ObjectAnimator.ofFloat(mHintTextView, "alpha", 0, 1);
            } else {
                fade = ObjectAnimator.ofFloat(mHintTextView, "alpha", 0, 0.33f);
            }
            animation.playTogether(move, fade);
        }

        if (animation != null) {
            animation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mHintTextView.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mHintTextView.setVisibility(show ? VISIBLE : INVISIBLE);
                }
            });
            animation.start();
        }
    }

    public EditText getEditText() {
        return mEditText;
    }

    public void setHint(String hint) {
        mEditText.setHint(hint);
        mHintTextView.setText(hint);
    }

    public CharSequence getHint() {
        return mHintTextView.getHint();
    }

}
