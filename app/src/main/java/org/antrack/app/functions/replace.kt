package org.antrack.app.functions

import android.os.Parcel
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

// Original: https://stackoverflow.com/questions/7364119/how-to-use-spannablestring-with-regex-in-android/7365113#7365113

fun CharSequence.replace(
    regex: String,
    replacement: CharSequence
): CharSequence {
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(this)
    return Replacer(this, matcher, replacement).replace()
}

private class Replacer(
    private val mSource: CharSequence, private val mMatcher: Matcher,
    private val mReplacement: CharSequence
) {
    private var mAppendPosition = 0
    private val mIsSpannable: Boolean = mReplacement is Spannable

    fun replace(): CharSequence {
        val buffer = SpannableStringBuilder()
        while (mMatcher.find()) {
            appendReplacement(buffer)
        }
        return appendTail(buffer)
    }

    private fun appendReplacement(buffer: SpannableStringBuilder) {
        buffer.append(mSource.subSequence(mAppendPosition, mMatcher.start()))
        val replacement = when {
            mIsSpannable -> copyCharSequenceWithSpans(mReplacement)
            else -> mReplacement
        }
        buffer.append(replacement)
        mAppendPosition = mMatcher.end()
    }

    fun appendTail(buffer: SpannableStringBuilder): SpannableStringBuilder {
        buffer.append(mSource.subSequence(mAppendPosition, mSource.length))
        return buffer
    }

    // This is a weird way of copying spans, but I don't know any better way.
    private fun copyCharSequenceWithSpans(string: CharSequence): CharSequence {
        val parcel = Parcel.obtain()
        return try {
            TextUtils.writeToParcel(string, parcel, 0)
            parcel.setDataPosition(0)
            TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}