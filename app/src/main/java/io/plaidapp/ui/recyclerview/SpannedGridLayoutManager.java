/*
 * Copyright 2016 Google Inc.
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

package io.plaidapp.ui.recyclerview;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link RecyclerView.LayoutManager} which displays a regular grid (i.e. all cells are the same
 * size) and allows simultaneous row & column spanning.
 */
public class SpannedGridLayoutManager extends RecyclerView.LayoutManager {

    private final GridSpanLookup spanLookup;
    private final int columns;
    private final float cellAspectRatio;

    private int cellHeight;
    private int[] cellBorders;
    private int firstVisiblePosition;
    private int lastVisiblePosition;
    private int firstVisibleRow;
    private int lastVisibleRow;
    private boolean forceClearOffsets;
    private SparseArray<GridCell> cells;
    private List<Integer> firstChildPositionForRow; // key == row, val == first child position
    private int totalRows;
    private final Rect itemDecorationInsets = new Rect();

    public SpannedGridLayoutManager(GridSpanLookup spanLookup, int columns, float cellAspectRatio) {
        this.spanLookup = spanLookup;
        this.columns = columns;
        this.cellAspectRatio = cellAspectRatio;
        setAutoMeasureEnabled(true);
    }

    public interface GridSpanLookup {
        SpanInfo getSpanInfo(int position);
    }

    public static class SpanInfo {
        public int columnSpan;
        public int rowSpan;

        public SpanInfo(int columnSpan, int rowSpan) {
            this.columnSpan = columnSpan;
            this.rowSpan = rowSpan;
        }

        public static final SpanInfo SINGLE_CELL = new SpanInfo(1, 1);
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {

        int columnSpan;
        int rowSpan;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        calculateWindowSize();
        calculateCellPositions(recycler, state);

        if (state.getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            firstVisibleRow = 0;
            resetVisibleItemTracking();
            return;
        }

        // TODO use orientationHelper
        int startTop = getPaddingTop();
        int scrollOffset = 0;
        if (forceClearOffsets) { // see #scrollToPosition
            startTop = -(firstVisibleRow * cellHeight);
            forceClearOffsets = false;
        } else if (getChildCount() != 0) {
            scrollOffset = getDecoratedTop(getChildAt(0));
            startTop = scrollOffset - (firstVisibleRow * cellHeight);
            resetVisibleItemTracking();
        }

        detachAndScrapAttachedViews(recycler);
        int row = firstVisibleRow;
        int availableSpace = getHeight() - scrollOffset;
        int lastItemPosition = state.getItemCount() - 1;
        while (availableSpace > 0 && lastVisiblePosition < lastItemPosition) {
            availableSpace -= layoutRow(row, startTop, recycler, state);
            row = getNextSpannedRow(row);
        }

        layoutDisappearingViews(recycler, state, startTop);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
        reset();
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state){
        if (getChildCount() == 0 || dy == 0) return 0;

        int scrolled;
        int top = getDecoratedTop(getChildAt(0));

        if (dy < 0) { // scrolling content down
            if (firstVisibleRow == 0) { // at top of content
                int scrollRange = -(getPaddingTop() - top);
                scrolled = Math.max(dy, scrollRange);
            } else {
                scrolled = dy;
            }
            if (top - scrolled >= 0) { // new top row came on screen
                int newRow = firstVisibleRow - 1;
                if (newRow >= 0) {
                    int startOffset = top - (firstVisibleRow * cellHeight);
                    layoutRow(newRow, startOffset, recycler, state);
                }
            }
            int firstPositionOfLastRow = getFirstPositionInSpannedRow(lastVisibleRow);
            int lastRowTop = getDecoratedTop(
                    getChildAt(firstPositionOfLastRow - firstVisiblePosition));
            if (lastRowTop - scrolled > getHeight()) { // last spanned row scrolled out
                recycleRow(lastVisibleRow, recycler, state);
            }
        } else { // scrolling content up
            int bottom = getDecoratedBottom(getChildAt(getChildCount() - 1));
            if (lastVisiblePosition == getItemCount() - 1) { // is at end of content
                int scrollRange = Math.max(bottom - getHeight() + getPaddingBottom(), 0);
                scrolled = Math.min(dy, scrollRange);
            } else {
                scrolled = dy;
            }
            if ((bottom - scrolled) < getHeight()) { // new row scrolled in
                int nextRow = lastVisibleRow + 1;
                if (nextRow < getSpannedRowCount()) {
                    int startOffset = top - (firstVisibleRow * cellHeight);
                    layoutRow(nextRow, startOffset, recycler, state);
                }
            }
            int lastPositionInRow = getLastPositionInSpannedRow(firstVisibleRow, state);
            int bottomOfFirstRow =
                    getDecoratedBottom(getChildAt(lastPositionInRow - firstVisiblePosition));
            if (bottomOfFirstRow - scrolled < 0) { // first spanned row scrolled out
                recycleRow(firstVisibleRow, recycler, state);
            }
        }
        offsetChildrenVertical(-scrolled);
        return scrolled;
    }

    @Override
    public void scrollToPosition(int position) {
        if (position >= getItemCount()) position = getItemCount() - 1;

        firstVisibleRow = getRowIndex(position);
        resetVisibleItemTracking();
        forceClearOffsets = true;
        removeAllViews();
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(
            RecyclerView recyclerView, RecyclerView.State state, int position) {
        if (position >= getItemCount()) position = getItemCount() - 1;

        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                final int rowOffset = getRowIndex(targetPosition) - firstVisibleRow;
                return new PointF(0, rowOffset * cellHeight);
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        // TODO update this to incrementally calculate
        return getSpannedRowCount() * cellHeight;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return getHeight();
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        if (getChildCount() == 0) return 0;
        return (firstVisibleRow * cellHeight) + Math.abs(getDecoratedTop(getChildAt(0)));
    }

    @Override
    public View findViewByPosition(int position) {
        if (position < firstVisiblePosition || position > lastVisiblePosition) return null;
        return getChildAt(position - firstVisiblePosition);
    }

    public int getFirstVisibleItemPosition() {
        return firstVisiblePosition;
    }

    private static class GridCell {
        final int row;
        final int rowSpan;
        final int column;
        final int columnSpan;

        GridCell(int row, int rowSpan, int column, int columnSpan) {
            this.row = row;
            this.rowSpan = rowSpan;
            this.column = column;
            this.columnSpan = columnSpan;
        }
    }

    /**
     * This is the main layout algorithm, iterates over all items and places them into [column, row]
     * cell positions. Stores this layout info for use later on. Also records the adapter position
     * that each row starts at.
     * <p>
     * Note that if a row is spanned, then the row start position is recorded as the first cell of
     * the row that the spanned cell starts in. This is to ensure that we have sufficient contiguous
     * views to layout/draw a spanned row.
     */
    private void calculateCellPositions(RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int itemCount = state.getItemCount();
        cells = new SparseArray<>(itemCount);
        firstChildPositionForRow = new ArrayList<>();
        int row = 0;
        int column = 0;
        recordSpannedRowStartPosition(row, column);
        int[] rowHWM = new int[columns]; // row high water mark (per column)
        for (int position = 0; position < itemCount; position++) {

            SpanInfo spanInfo;
            int adapterPosition = recycler.convertPreLayoutPositionToPostLayout(position);
            if (adapterPosition !=  RecyclerView.NO_POSITION) {
                spanInfo = spanLookup.getSpanInfo(adapterPosition);
            } else {
                // item removed from adapter, retrieve its previous span info
                // as we can't get from the lookup (adapter)
                spanInfo = getSpanInfoFromAttachedView(position);
            }

            if (spanInfo.columnSpan > columns) {
                spanInfo.columnSpan = columns; // or should we throw?
            }

            // check horizontal space at current position else start a new row
            // note that this may leave gaps in the grid; we don't backtrack to try and fit
            // subsequent cells into gaps. We place the responsibility on the adapter to provide
            // continuous data i.e. that would not span column boundaries to avoid gaps.
            if (column + spanInfo.columnSpan > columns) {
                row++;
                recordSpannedRowStartPosition(row, position);
                column = 0;
            }

            // check if this cell is already filled (by previous spanning cell)
            while (rowHWM[column] > row) {
                column++;
                if (column + spanInfo.columnSpan > columns) {
                    row++;
                    recordSpannedRowStartPosition(row, position);
                    column = 0;
                }
            }

            // by this point, cell should fit at [column, row]
            cells.put(position, new GridCell(row, spanInfo.rowSpan, column, spanInfo.columnSpan));

            // update the high water mark book-keeping
            for (int columnsSpanned = 0; columnsSpanned < spanInfo.columnSpan; columnsSpanned++) {
                rowHWM[column + columnsSpanned] = row + spanInfo.rowSpan;
            }

            // if we're spanning rows then record the 'first child position' as the first item
            // *in the row the spanned item starts*. i.e. the position might not actually sit
            // within the row but it is the earliest position we need to render in order to fill
            // the requested row.
            if (spanInfo.rowSpan > 1) {
                int rowStartPosition = getFirstPositionInSpannedRow(row);
                for (int rowsSpanned = 1; rowsSpanned < spanInfo.rowSpan; rowsSpanned++) {
                    int spannedRow = row + rowsSpanned;
                    recordSpannedRowStartPosition(spannedRow, rowStartPosition);
                }
            }

            // increment the current position
            column += spanInfo.columnSpan;
        }
        totalRows = rowHWM[0];
        for (int i = 1; i < rowHWM.length; i++) {
            if (rowHWM[i] > totalRows) {
                totalRows = rowHWM[i];
            }
        }
    }

    private SpanInfo getSpanInfoFromAttachedView(int position) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (position == getPosition(child)) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                return new SpanInfo(lp.columnSpan, lp.rowSpan);
            }
        }
        // errrrr?
        return SpanInfo.SINGLE_CELL;
    }

    private void recordSpannedRowStartPosition(final int rowIndex, final int position) {
        if (getSpannedRowCount() < (rowIndex + 1)) {
            firstChildPositionForRow.add(position);
        }
    }

    private int getRowIndex(final int position) {
        return position < cells.size() ? cells.get(position).row : -1;
    }

    private int getSpannedRowCount() {
        return firstChildPositionForRow.size();
    }

    private int getNextSpannedRow(int rowIndex) {
        int firstPositionInRow = getFirstPositionInSpannedRow(rowIndex);
        int nextRow = rowIndex + 1;
        while (nextRow < getSpannedRowCount()
                && getFirstPositionInSpannedRow(nextRow) == firstPositionInRow) {
            nextRow++;
        }
        return nextRow;
    }

    private int getFirstPositionInSpannedRow(int rowIndex) {
        return firstChildPositionForRow.get(rowIndex);
    }

    private int getLastPositionInSpannedRow(final int rowIndex, RecyclerView.State state) {
        int nextRow = getNextSpannedRow(rowIndex);
        return (nextRow != getSpannedRowCount()) ? // check if reached boundary
                getFirstPositionInSpannedRow(nextRow) - 1
                : state.getItemCount() - 1;
    }

    /**
     * Lay out a given 'row'. We might actually add more that one row if the requested row contains
     * a row-spanning cell. Returns the pixel height of the rows laid out.
     * <p>
     * To simplify logic & book-keeping, views are attached in adapter order, that is child 0 will
     * always be the earliest position displayed etc.
     */
    private int layoutRow(
            int rowIndex, int startTop, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int firstPositionInRow = getFirstPositionInSpannedRow(rowIndex);
        int lastPositionInRow = getLastPositionInSpannedRow(rowIndex, state);
        boolean containsRemovedItems = false;

        int insertPosition = (rowIndex < firstVisibleRow) ? 0 : getChildCount();
        for (int position = firstPositionInRow;
             position <= lastPositionInRow;
             position++, insertPosition++) {

            View view = recycler.getViewForPosition(position);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            containsRemovedItems |= lp.isItemRemoved();
            GridCell cell = cells.get(position);
            addView(view, insertPosition);

            // TODO use orientation helper
            int wSpec = getChildMeasureSpec(
                    cellBorders[cell.column + cell.columnSpan] - cellBorders[cell.column],
                    View.MeasureSpec.EXACTLY, 0, lp.width, false);
            int hSpec = getChildMeasureSpec(cell.rowSpan * cellHeight,
                    View.MeasureSpec.EXACTLY, 0, lp.height, true);
            measureChildWithDecorationsAndMargin(view, wSpec, hSpec);

            int left = cellBorders[cell.column] + lp.leftMargin;
            int top = startTop + (cell.row * cellHeight) + lp.topMargin;
            int right = left + getDecoratedMeasuredWidth(view);
            int bottom = top + getDecoratedMeasuredHeight(view);
            layoutDecorated(view, left, top, right, bottom);
            lp.columnSpan = cell.columnSpan;
            lp.rowSpan = cell.rowSpan;
        }

        if (firstPositionInRow < firstVisiblePosition) {
            firstVisiblePosition = firstPositionInRow;
            firstVisibleRow = getRowIndex(firstVisiblePosition);
        }
        if (lastPositionInRow > lastVisiblePosition) {
            lastVisiblePosition = lastPositionInRow;
            lastVisibleRow = getRowIndex(lastVisiblePosition);
        }
        if (containsRemovedItems) return 0; // don't consume space for rows with disappearing items

        GridCell first = cells.get(firstPositionInRow);
        GridCell last = cells.get(lastPositionInRow);
        return (last.row + last.rowSpan - first.row) * cellHeight;
    }

    /**
     * Remove and recycle all items in this 'row'. If the row includes a row-spanning cell then all
     * cells in the spanned rows will be removed.
     */
    private void recycleRow(
            int rowIndex, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int firstPositionInRow = getFirstPositionInSpannedRow(rowIndex);
        int lastPositionInRow = getLastPositionInSpannedRow(rowIndex, state);
        int toRemove = lastPositionInRow;
        while (toRemove >= firstPositionInRow) {
            int index = toRemove - firstVisiblePosition;
            removeAndRecycleViewAt(index, recycler);
            toRemove--;
        }
        if (rowIndex == firstVisibleRow) {
            firstVisiblePosition = lastPositionInRow + 1;
            firstVisibleRow = getRowIndex(firstVisiblePosition);
        }
        if (rowIndex == lastVisibleRow) {
            lastVisiblePosition = firstPositionInRow - 1;
            lastVisibleRow = getRowIndex(lastVisiblePosition);
        }
    }

    private void layoutDisappearingViews(
            RecyclerView.Recycler recycler, RecyclerView.State state, int startTop) {
        // TODO
    }

    private void calculateWindowSize() {
        // TODO use OrientationHelper#getTotalSpace
        int cellWidth =
                (int) Math.floor((getWidth() - getPaddingLeft() - getPaddingRight()) / columns);
        cellHeight = (int) Math.floor(cellWidth * (1f / cellAspectRatio));
        calculateCellBorders();
    }

    private void reset() {
        cells = null;
        firstChildPositionForRow = null;
        firstVisiblePosition = 0;
        firstVisibleRow = 0;
        lastVisiblePosition = 0;
        lastVisibleRow = 0;
        cellHeight = 0;
        forceClearOffsets = false;
    }

    private void resetVisibleItemTracking() {
        // maintain the firstVisibleRow but reset other state vars
        // TODO make orientation agnostic
        int minimumVisibleRow = getMinimumFirstVisibleRow();
        if (firstVisibleRow > minimumVisibleRow) firstVisibleRow = minimumVisibleRow;
        firstVisiblePosition = getFirstPositionInSpannedRow(firstVisibleRow);
        lastVisibleRow = firstVisibleRow;
        lastVisiblePosition = firstVisiblePosition;
    }

    private int getMinimumFirstVisibleRow() {
        int maxDisplayedRows = (int) Math.ceil((float) getHeight() / cellHeight) + 1;
        if (totalRows < maxDisplayedRows) return 0;
        int minFirstRow = totalRows - maxDisplayedRows;
        // adjust to spanned rows
        return getRowIndex(getFirstPositionInSpannedRow(minFirstRow));
    }

    /* Adapted from GridLayoutManager */

    private void calculateCellBorders() {
        cellBorders = new int[columns + 1];
        int totalSpace = getWidth() - getPaddingLeft() - getPaddingRight();
        int consumedPixels = getPaddingLeft();
        cellBorders[0] = consumedPixels;
        int sizePerSpan = totalSpace / columns;
        int sizePerSpanRemainder = totalSpace % columns;
        int additionalSize = 0;
        for (int i = 1; i <= columns; i++) {
            int itemSize = sizePerSpan;
            additionalSize += sizePerSpanRemainder;
            if (additionalSize > 0 && (columns - additionalSize) < sizePerSpanRemainder) {
                itemSize += 1;
                additionalSize -= columns;
            }
            consumedPixels += itemSize;
            cellBorders[i] = consumedPixels;
        }
    }

    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec) {
        calculateItemDecorationsForChild(child, itemDecorationInsets);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        widthSpec = updateSpecWithExtra(widthSpec, lp.leftMargin + itemDecorationInsets.left,
                lp.rightMargin + itemDecorationInsets.right);
        heightSpec = updateSpecWithExtra(heightSpec, lp.topMargin + itemDecorationInsets.top,
                lp.bottomMargin + itemDecorationInsets.bottom);
        child.measure(widthSpec, heightSpec);
    }

    private int updateSpecWithExtra(int spec, int startInset, int endInset) {
        if (startInset == 0 && endInset == 0) {
            return spec;
        }
        int mode = View.MeasureSpec.getMode(spec);
        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(
                    View.MeasureSpec.getSize(spec) - startInset - endInset, mode);
        }
        return spec;
    }

}
