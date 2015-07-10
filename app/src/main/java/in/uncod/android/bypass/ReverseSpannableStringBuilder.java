package in.uncod.android.bypass;

import android.text.SpannableStringBuilder;

/**
 * Exactly the same as SpannableStringBuilder, but it returns its spans in reverse.
 * <p/>
 * What effect does this have? Well, if you're building up a Spannable recursively (as we
 * are doing in Bypass) then returning the spans in reverse order has the correct effect
 * in some corner cases regarding leading spans.
 * <p/>
 * Example:
 * Suppose we have a BLOCK_QUOTE with a LIST inside of it. Both of them have leading spans, but the LIST
 * span is set first. As a result, the QuoteSpan for the BLOCK_QUOTE is actually indented by the LIST's span!
 * If the order is reversed, then the LIST's margin span is properly indented (and the BlockQuote remains on
 * the side).
 */
public class ReverseSpannableStringBuilder extends SpannableStringBuilder {

    private static void reverse(Object[] arr) {
        if (arr == null) {
            return;
        }

        int i = 0;
        int j = arr.length - 1;
        Object tmp;
        while (j > i) {
            tmp = arr[j];
            arr[j] = arr[i];
            arr[i] = tmp;
            j--;
            i++;
        }
    }

    @Override
    public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind) {
        T[] ret = super.getSpans(queryStart, queryEnd, kind);
        reverse(ret);
        return ret;
    }
}
