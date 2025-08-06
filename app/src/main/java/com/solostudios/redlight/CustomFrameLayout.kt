package com.solostudios.redlight

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class CustomFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    override fun performClick(): Boolean {
        // Mark this view as having been clicked (satisfies accessibility)
        super.performClick()
        return true
    }

}
