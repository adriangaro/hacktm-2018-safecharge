package com.tmc.safecharge.layouts

import android.annotation.TargetApi
import android.content.Context
import android.os.Build.VERSION_CODES
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet

/** A RelativeLayout that will always be square -- same width and height,
 * where the height is based off the width.  */
class SquareConstraintLayout : ConstraintLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Set a square layout.
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}