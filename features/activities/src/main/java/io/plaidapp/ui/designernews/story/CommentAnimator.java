package io.plaidapp.ui.designernews.story;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import io.plaidapp.ui.designernews.story.DesignerNewsStory.CommentItemHolderInfo;
import io.plaidapp.ui.recyclerview.SlideInItemAnimator;

class CommentAnimator extends SlideInItemAnimator {

    CommentAnimator(long addRemoveDuration) {
        super();
        setAddDuration(addRemoveDuration);
        setRemoveDuration(addRemoveDuration);
    }

    static final int EXPAND_COMMENT = 1;
    static final int COLLAPSE_COMMENT = 2;

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(@NonNull RecyclerView.State state,
            @NonNull RecyclerView.ViewHolder viewHolder,
            int changeFlags,
            @NonNull List<Object> payloads) {
        CommentItemHolderInfo info = (CommentItemHolderInfo)
                super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
        info.doExpand = payloads.contains(EXPAND_COMMENT);
        info.doCollapse = payloads.contains(COLLAPSE_COMMENT);
        return info;
    }


    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
            @NonNull RecyclerView.ViewHolder newHolder,
            @NonNull ItemHolderInfo preInfo,
            @NonNull ItemHolderInfo postInfo) {
        if (newHolder instanceof CommentViewHolder && preInfo instanceof CommentItemHolderInfo) {
            final CommentViewHolder holder = (CommentViewHolder) newHolder;
            final CommentItemHolderInfo
                    info = (CommentItemHolderInfo) preInfo;
            holder.animate(info, this);
        }
        return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
    }


    @Override
    public ItemHolderInfo obtainHolderInfo() {
        return new CommentItemHolderInfo();
    }

}