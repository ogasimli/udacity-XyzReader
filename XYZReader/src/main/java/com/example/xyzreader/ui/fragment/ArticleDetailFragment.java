package com.example.xyzreader.ui.fragment;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.activity.ArticleDetailActivity;
import com.example.xyzreader.ui.activity.ArticleListActivity;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    @Bind(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.detail_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.meta_bar)
    View metaBarView;
    @Bind(R.id.article_title)
    TextView mTitle;
    @Bind(R.id.article_subtitle)
    TextView mSubtitle;
    @Bind(R.id.photo)
    ImageView mPhotoView;
    @Bind(R.id.share_fab)
    FloatingActionButton mShareFab;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        mShareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    AnimatedVectorDrawable fabRotation = (AnimatedVectorDrawable) getActivity().
                            getDrawable(R.drawable.fab_anim_vector_drawable);
                    mShareFab.setImageDrawable(fabRotation);
                    if (fabRotation != null) {
                        fabRotation.start();
                    }
                }
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
                }
            }
        );

            bindViews();

            return mRootView;
        }

        @Override
        public void onViewCreated (View view, Bundle savedInstanceState){
            super.onViewCreated(view, savedInstanceState);
            initToolbar();
            initCollapsingToolbar();
        }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        mSubtitle.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setVisibility(View.VISIBLE);
            mTitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            mSubtitle.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            Log.d("Photo URL: ", photoUrl);
            Glide.with(mPhotoView.getContext())
                    .load(photoUrl)
                    .placeholder(R.color.colorLightPrimary)
                    .listener(GlidePalette.with(photoUrl).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(Palette palette) {
                            Palette.Swatch swatch = palette.getVibrantSwatch();
                            applyColors(swatch);
                        }
                    }))
                    .into(mPhotoView);
        } else {
            mRootView.setVisibility(View.GONE);
            mTitle.setText("N/A");
            mSubtitle.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    private void applyColors(Palette.Swatch swatch) {
        if (swatch != null) {
            metaBarView.setBackgroundColor(swatch.getRgb());
            mTitle.setTextColor(swatch.getBodyTextColor());
            mSubtitle.setTextColor(swatch.getTitleTextColor());
        }
    }

    /*Initialize CollapsingToolbarLayout*/
    @SuppressWarnings("ConstantConditions")
    private void initCollapsingToolbar() {
        if (mCollapsingToolbarLayout != null) {
            mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
            mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.TRANSPARENT);
            mCollapsingToolbarLayout.setTitle("");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /*Initialize Toolbar*/
    private void initToolbar() {
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }
}
