package com.example.xyzreader.ui.adapter;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;

import android.app.Activity;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

/**
 * RecyclerView adapter for articles list
 * Created by ogasimli on 15.11.2015.
 */
public final class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleHolder> {

    public interface OnArticleItemClickListener {
        void onArticleSelected(View view, long articleId);

        OnArticleItemClickListener DUMMY = new OnArticleItemClickListener() {
            @Override
            public void onArticleSelected(View view, long articleId) {
            }
        };
    }

    private final Activity mActivity;
    private final LayoutInflater mInflater;

    private OnArticleItemClickListener mListener = OnArticleItemClickListener.DUMMY;
    private Cursor mCursor;

    public ArticlesAdapter(Activity activity, Cursor cursor) {
        mInflater = LayoutInflater.from(activity);
        mActivity = activity;
        mCursor = cursor;
        setHasStableIds(true);
    }

    public ArticlesAdapter setListener(@NonNull OnArticleItemClickListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ArticleHolder(mInflater.inflate(R.layout.list_item_article, parent, false));
    }

    @Override
    public void onBindViewHolder(ArticleHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    final class ArticleHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.article_item_footer_container)
        View footerContainerView;
        @Bind(R.id.thumbnail)
        ImageView thumbnailView;
        @Bind(R.id.article_title)
        TextView titleView;
        @Bind(R.id.article_subtitle)
        TextView subtitleView;

        @BindColor(R.color.colorPrimary)
        int mColorBackground;
        @BindColor(R.color.color_title_inverse)
        int mColorTitle;
        @BindColor(R.color.color_subtitle_inverse)
        int mColorSubtitle;

        private long mLastItemId;

        public ArticleHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onArticleSelected(thumbnailView, getItemId());
                }
            });
        }

        public void bind(int position) {
            mCursor.moveToPosition(position);

            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            subtitleView.setText(
                    mActivity.getString(R.string.article_item_subtitle,
                            DateUtils.getRelativeTimeSpanString(
                                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                    DateUtils.FORMAT_ABBREV_ALL).toString(),
                            mCursor.getString(ArticleLoader.Query.AUTHOR)));

            if (mLastItemId != getItemId()) {
                resetColors();
                mLastItemId = getItemId();
            }

            String imageUrl = mCursor.getString(ArticleLoader.Query.THUMB_URL);
            Glide.with(thumbnailView.getContext())
                    .load(imageUrl)
                    .crossFade()
                    .placeholder(R.color.photo_placeholder)
                    .listener(GlidePalette.with(imageUrl).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(Palette palette) {
                            applyColors(palette.getVibrantSwatch());
                        }
                    }))
                    .into(thumbnailView);
        }

        private void resetColors() {
            footerContainerView.setBackgroundColor(mColorBackground);
            titleView.setTextColor(mColorTitle);
            subtitleView.setTextColor(mColorSubtitle);
        }

        private void applyColors(Palette.Swatch swatch) {
            if (swatch != null) {
                footerContainerView.setBackgroundColor(swatch.getRgb());
                titleView.setTextColor(swatch.getBodyTextColor());
                subtitleView.setTextColor(swatch.getTitleTextColor());
            }
        }
    }
}
