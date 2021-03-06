package kr.co.tacademy.mongsil.mongsil.Activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import kr.co.tacademy.mongsil.mongsil.R;
import kr.co.tacademy.mongsil.mongsil.Fragments.TutorialPaneFragment;

public class AppTutorialActivity extends BaseActivity {
    private static final int MAX_PAGES = 3;

    ViewPager pager;
    ImageView imgWeather, imgNavi, imgWrite;
    ImageView imgTutorialSkip;
    LinearLayout indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_tutorial);

        imgWeather = (ImageView) findViewById(R.id.img_tutorial_weather);
        imgNavi = (ImageView) findViewById(R.id.img_tutorial_navi);
        imgWrite = (ImageView) findViewById(R.id.img_tutorial_write);

        pager = (ViewPager) findViewById(R.id.viewpager_tutorial);
        final tutorialPagerAdapter adapter = new tutorialPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setPageTransformer(true, new CrossFadePageTransformer());
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == 0) {
                    imgWeather.setVisibility(View.VISIBLE);
                    imgNavi.setVisibility(View.GONE);
                    imgWrite.setVisibility(View.GONE);
                    imgWeather.setAlpha(1.0f - Math.abs(positionOffset));
                }
                if(position == 1) {
                    imgWeather.setVisibility(View.GONE);
                    imgNavi.setVisibility(View.VISIBLE);
                    imgWrite.setVisibility(View.GONE);
                    imgNavi.setAlpha(1.0f - Math.abs(positionOffset));
                }
                if(position == 2) {
                    imgWeather.setVisibility(View.GONE);
                    imgNavi.setVisibility(View.GONE);
                    imgWrite.setVisibility(View.VISIBLE);
                    imgWrite.setAlpha(1.0f - Math.abs(positionOffset));
                }
            }

            @Override
            public void onPageSelected(int position) {
                setIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Unused
            }
        });
        buildIndicator();

        imgTutorialSkip = (ImageView) findViewById(R.id.img_tutorial_skip);
        imgTutorialSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTutorial();
            }
        });
    }

    private void buildIndicator(){
        indicator = (LinearLayout) findViewById(R.id.viewPager_count_dots);

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (5 * scale + 0.5f);

        for(int i = 0 ; i < MAX_PAGES ; i++){
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.drawable.indicator_none_dot);
            dot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dot.setAdjustViewBounds(true);
            dot.setPadding(padding, 0, padding, 0);
            indicator.addView(dot);
        }
        setIndicator(0);
    }

    private void setIndicator(int index){
        if(index < MAX_PAGES){
            for(int i = 0 ; i < MAX_PAGES ; i++){
                ImageView dot = (ImageView) indicator.getChildAt(i);
                if(i == index){
                    dot.setImageResource(R.drawable.indicator_dot);
                }else {
                    dot.setImageResource(R.drawable.indicator_none_dot);
                }
            }
        }
    }

    private void endTutorial() {
        Intent intent = new Intent(AppTutorialActivity.this, SignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
            super.onBackPressed();
            Intent intent = new Intent(AppTutorialActivity.this, SplashActivity.class);
            startActivity(intent);
            finish();
        } else {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    private class tutorialPagerAdapter extends FragmentStatePagerAdapter {

        public tutorialPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialPaneFragment.newInstance(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        @Override
        public int getCount() {
            return MAX_PAGES;
        }
    }

    public class CrossFadePageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View page, float position) {
            int pageWidth = page.getWidth();

            // 첫번째
            View cloudCutTwo = page.findViewById(R.id.img_one_cloud02_cut);
            View cloudCutFour = page.findViewById(R.id.img_one_cloud04_cut);
            View cloudCutThree = page.findViewById(R.id.img_one_cloud03_cut);

            // 두번째
            View cloud2CutTwo = page.findViewById(R.id.img_two_cloud02_cut);
            View cloud2CutFour = page.findViewById(R.id.img_two_cloud04_cut);
            View cloud2CutThree = page.findViewById(R.id.img_two_cloud03_cut);

            // 세번째
            View cloud3CutTwo = page.findViewById(R.id.img_three_cloud02_cut);
            View cloud3CutThree = page.findViewById(R.id.img_three_cloud03_cut);

            if(position <= -1.0f || position >= 1.0f) {
            } else if( position == 0.0f ) {
            } else {

                // 첫번째
                if (cloudCutTwo != null) {
                    cloudCutTwo.setTranslationX(-(float)(pageWidth/1.5 * position));
                }
                if (cloudCutFour != null) {
                    cloudCutFour.setTranslationX(-(float)(pageWidth/0.9 * position));
                }
                if (cloudCutThree != null) {
                    cloudCutThree.setTranslationX(-(float)(pageWidth/0.8 * position));
                }

                // 두번째
                if (cloud2CutTwo != null) {
                    cloud2CutTwo.setTranslationX((float)(pageWidth/0.9 * position));

                }
                if (cloud2CutFour != null) {
                    cloud2CutFour.setTranslationX(-(float)(pageWidth/0.8 * position));
                }
                if (cloud2CutThree != null) {
                    cloud2CutThree.setTranslationX(-(float)(pageWidth/1.2 * position));
                }

                // 세번째
                if (cloud3CutTwo != null) {
                    cloud3CutTwo.setTranslationX(-(float)(pageWidth/0.7 * position));
                }
                if (cloud3CutThree != null) {
                    cloud3CutThree.setTranslationX((float)(pageWidth/1.6 * position));
                }

            }
        }
    }
}
