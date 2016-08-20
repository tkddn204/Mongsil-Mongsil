package kr.co.tacademy.mongsil.mongsil;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.util.Log.e;

public class MainActivity extends BaseActivity
        implements SearchPOIDialogFragment.OnPOISearchListener,
                GPSManager.LocationCallback,
    MiddleSelectDialogFragment.OnMiddleSelectDialogListener {
    public static final int RESULT_MAP = 1;

    // 툴바 필드
    TextView tbTitle;
    ImageView tbSearch;

    // 날씨 필드
    RelativeLayout weatherContainer;
    ImageView animBackgroundWeather, imgWeatherIcon;
    TextView date, weatherName;

    // 글목록 프레그먼트
    MainSocketPostFragment mainSocketPostFragment;

    // 슬라이딩메뉴
    SlidingMenu slidingMenu;

    // 글쓰기 버튼
    FloatingActionButton btnCapturePost;

    // GPS
    GPSManager gpsManager;

    @Override
    protected void onResume() {
        super.onResume();
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS)
        {//실패
            GooglePlayServicesUtil.getErrorDialog(result, this, 0, new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    finish();
                }
            }).show();
        }
        else
        {
            // 성공
            if (!PropertyManager.getInstance().getUseGPS()) {
                locationProviderCheck();
            }
        }
    }

    private Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 글 삭제하고 난 후의 다이어로그 창
        if (getIntent().getBooleanExtra("post_remove", false)) {
            getSupportFragmentManager().beginTransaction().
                    add(MiddleAloneDialogFragment.newInstance(0), "middle_done").commit();
        }

        // 글 작성 프레그먼트와 슬라이딩메뉴 프레그먼트를 선언
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_post_fragment_container,
                        MainSocketPostFragment.newInstance(PropertyManager.getInstance().getLocation()))
                .commit();
        // 툴바 추가
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        tbTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        tbTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .add(new SearchPOIDialogFragment(), "search_main").commit();
            }
        });
        tbSearch = (ImageView) toolbar.findViewById(R.id.toolbar_search);
        tbSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivityForResult(intent, RESULT_MAP);
            }
        });

        // 슬라이딩 메뉴(프로필메뉴)
        slidingMenu = new SlidingMenu(getApplicationContext());
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(loadSlidingMenu());

        // 날씨
        weatherContainer = (RelativeLayout) findViewById(R.id.main_weather_container);
        animBackgroundWeather = (ImageView) findViewById(R.id.anim_background_weather);
        imgWeatherIcon = (ImageView) findViewById(R.id.img_weather_icon);
        imgWeatherIcon.setAnimation(AnimationApplyInterpolater(
                R.anim.bounce_interpolator, new LinearInterpolator()));

        date = (TextView) findViewById(R.id.text_date);
        date.setText(TimeData.mainDateFormat);
        weatherName = (TextView) findViewById(R.id.text_weathername);

        // 글쓰기 버튼
        btnCapturePost = (FloatingActionButton) findViewById(R.id.btn_capture_post);
        btnCapturePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PostingActivity.class);
                intent.putExtra("area1", tbTitle.getText().toString());
                startActivity(intent);
            }
        });

        if (!getIntent().hasExtra("area1")) {
            tbTitle.setText(PropertyManager.getInstance().getLocation());
            new AsyncLatLonWeatherJSONList().execute(
                    PropertyManager.getInstance().getLatLocation(),
                    PropertyManager.getInstance().getLonLocation());
        } else {
            // 글을 작성하고 난 후의 지역 설정
            new AsyncLatLonWeatherJSONList().execute(
                    LocationData.ChangeToLatLon(getIntent().getStringExtra("area1")));
        }
    }

    // 애니메이션 인터폴레이터 적용
    private Animation AnimationApplyInterpolater(
            int resourceId, final Interpolator interpolator) {
        Animation animation = AnimationUtils.loadAnimation(this, resourceId);
        animation.setInterpolator(interpolator);
        return animation;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_MAP:
                    String location = data.getStringExtra("name");
                    tbTitle.setText(location);
                    new AsyncLatLonWeatherJSONList().execute(LocationData.ChangeToLatLon(location));
                    mainSocketPostFragment = MainSocketPostFragment.newInstance(location);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_post_fragment_container, mainSocketPostFragment)
                            .commit();
                    break;
            }
        }
    }

    // 날씨를 검색해서 지역 정보를 받아옴
    @Override
    public void onPOISearch(POIData POIData) {
        if (POIData != null) {
            String location = POIData.upperAddrName;
            tbTitle.setText(location);
            new AsyncLatLonWeatherJSONList().execute(POIData.noorLat, POIData.noorLon);
            mainSocketPostFragment = MainSocketPostFragment.newInstance(location);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_post_fragment_container, mainSocketPostFragment)
                    .commit();
        }
    }

    ImageView imgProfileBackground;
    CircleImageView imgProfile;
    TextView textMyName, textMyLocation;
    ImageView imgSetting, imgAlarm, imgClose;
    ViewPager viewPager;
    TabLayout tabLayout;

    // 슬라이딩메뉴 뷰
    public View loadSlidingMenu() {
        View menu = getLayoutInflater().inflate(R.layout.layout_profile_menu, null);

        imgProfileBackground =
                (ImageView) menu.findViewById(R.id.img_profile_background);

        imgProfile =
                (CircleImageView) menu.findViewById(R.id.img_profile);
        Log.e("프로필이미지 value : ", " " + PropertyManager.getInstance().getUserProfileImg());
        if (!PropertyManager.getInstance().getUserProfileImg().isEmpty()) {
            Glide.with(MongSilApplication.getMongSilContext())
                    .load(PropertyManager.getInstance().getUserProfileImg())
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource,
                                                    GlideAnimation<? super Bitmap> glideAnimation) {
                            imgProfile.setImageBitmap(resource);
                            imgProfileBackground.setImageBitmap(
                                    BlurBuilder.blur(resource, 5));
                        }
                    });
        } else {
            imgProfile.setImageResource(R.drawable.none_my_profile);
        }
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("UserProfile : ", PropertyManager.getInstance().getUserProfileImg());
                startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
            }
        });
        textMyName = (TextView) menu.findViewById(R.id.text_my_name);
        textMyLocation = (TextView) menu.findViewById(R.id.text_my_location);
        textMyName.setText(PropertyManager.getInstance().getNickname());
        textMyLocation.setText(PropertyManager.getInstance().getLocation());

        imgSetting = (ImageView) menu.findViewById(R.id.img_setting);
        imgSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });
        imgAlarm = (ImageView) menu.findViewById(R.id.img_alarm);
        imgAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AlarmActivity.class));
            }
        });
        imgClose = (ImageView) menu.findViewById(R.id.img_close);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingMenu.toggle();
            }
        });

        viewPager = (ViewPager) menu.findViewById(R.id.viewpager_menu);
        if (viewPager != null) {
            MenuViewPagerAdapter adapter =
                    new MenuViewPagerAdapter(getSupportFragmentManager());
            String[] tabTitle = MongSilApplication.getMongSilContext()
                    .getResources().getStringArray(R.array.menu_tab_title);
            adapter.appendFragment(
                    ProfileMenuTabFragment
                            .newInstance(0, PropertyManager.getInstance().getUserId()), tabTitle[0]);
            adapter.appendFragment(
                    ProfileMenuTabFragment
                            .newInstance(1, PropertyManager.getInstance().getUserId()), tabTitle[1]);
            viewPager.setAdapter(adapter);
        }

        // 탭 레이아웃 설정
        tabLayout = (TabLayout) menu.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        final Typeface normalFont = Typeface.createFromAsset(getAssets(), "fonts/NotoSansKR-Regular.otf");
        final Typeface boldFont = Typeface.createFromAsset(getAssets(), "fonts/NotoSansKR-Bold.otf");
        for (int i = 0; i < tabLayout.getTabCount(); i++) {

            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {

                TextView tabTextView = new TextView(MainActivity.this);
                tab.setCustomView(tabTextView);

                tabTextView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                tabTextView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                tabTextView.setText(tab.getText());

                // First tab is the selected tab, so if i==0 then set BOLD typeface
                if (i == 0) {
                    tabTextView.setTypeface(boldFont);
                } else if (i == 1) {
                    tabTextView.setTypeface(normalFont);
                }

            }
        }
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                TextView text = (TextView) tab.getCustomView();
                if (text != null) {
                    text.setTypeface(boldFont);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView text = (TextView) tab.getCustomView();
                if (text != null) {
                    text.setTypeface(normalFont);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return menu;
    }

    // 메뉴 뷰페이저 어답터
    private static class MenuViewPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<ProfileMenuTabFragment> fragments
                = new ArrayList<ProfileMenuTabFragment>();
        private final ArrayList<String> tabTitle = new ArrayList<String>();

        public MenuViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public void appendFragment(ProfileMenuTabFragment fragment, String title) {
            fragments.add(fragment);
            tabTitle.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitle.get(position);
        }
    }

    // 툴바 메뉴 선택
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                slidingMenu.showMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 백 버튼 눌렀을 때
    @Override
    public void onBackPressed() {
        if (slidingMenu.isMenuShowing()) {
            slidingMenu.toggle();
            return;
        }
        super.onBackPressed();
    }

    // 위도, 경도 날씨 AsyncTask
    public class AsyncLatLonWeatherJSONList extends AsyncTask<String, Integer, WeatherData> {

        @Override
        protected WeatherData doInBackground(String... args) {
            Response response = null;
            try {
                OkHttpClient toServer = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();

                Log.e("좌표", args[0] + " " + args[1]);
                Request request = new Request.Builder()
                        .addHeader("Accept", "application/json")
                        .addHeader("appKey", NetworkDefineConstant.SK_APP_KEY)
                        .url(String.format(
                                NetworkDefineConstant.SK_WEATHER_LAT_LON,
                                args[0], args[1]))
                        .build();

                response = toServer.newCall(request).execute();
                ResponseBody responseBody = response.body();

                boolean flag = response.isSuccessful();
                int responseCode = response.code();
                if (responseCode >= 400) return null;
                if (flag) {
                    return ParseDataParseHandler.getJSONWeatherList(
                            new StringBuilder(responseBody.string()));
                }
            } catch (UnknownHostException une) {
                e("connectionFail", une.toString());
            } catch (UnsupportedEncodingException uee) {
                e("connectionFail", uee.toString());
            } catch (Exception e) {
                e("connectionFail", e.toString());
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(WeatherData result) {
            if (result != null) {
                weatherName.setText(result.name);
                imgWeatherIcon.setImageResource(WeatherData.imgFromWeatherCode(result.code, 0));
                weatherContainer.setBackgroundResource(WeatherData.imgFromWeatherCode(result.code, 1));
                animBackgroundWeather.setImageResource(WeatherData.imgFromWeatherCode(result.code, 2));
                if (animBackgroundWeather.isShown()) {
                    ((AnimationDrawable) animBackgroundWeather.getDrawable()).start();
                }
                if (imgWeatherIcon.isShown()) {
                    ((AnimationDrawable) imgWeatherIcon.getDrawable()).start();
                }
            }
        }
    }

    // 역지오코딩 AsyncTask
    public class AsyncReGeoJSONList extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            Response response = null;
            try {
                OkHttpClient toServer = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();

                Request request = new Request.Builder()
                        .addHeader("Accept", "application/json")
                        .addHeader("appKey", NetworkDefineConstant.SK_APP_KEY)
                        .url(String.format(
                                NetworkDefineConstant.SK_REVERSE_GEOCOING,
                                args[0], args[1]))
                        .build();
                response = toServer.newCall(request).execute();
                ResponseBody responseBody = response.body();

                boolean flag = response.isSuccessful();
                int responseCode = response.code();
                Log.e("응답 코드 : ", responseCode + "");
                if (responseCode >= 400) return null;
                if (flag) {
                    return ParseDataParseHandler.getJSONResGeo(
                            new StringBuilder(responseBody.string()));
                }
            } catch (UnknownHostException une) {
                e("connectionFail", une.toString());
            } catch (UnsupportedEncodingException uee) {
                e("connectionFail", uee.toString());
            } catch (Exception e) {
                e("connectionFail", e.toString());
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("역지오코딩 결과 :", result+"");
            if (result != null) {
                String GPSlocation = LocationData.ChangeToShortName(result);
                Log.e("GPSlocation 결과 :", GPSlocation);
                tbTitle.setText(GPSlocation);
                mainSocketPostFragment = MainSocketPostFragment.newInstance(GPSlocation);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_post_fragment_container, mainSocketPostFragment)
                        .commit();
            }
        }
    }

    // 아래 전부 GPS 기능 관련
    @Override
    protected void onStop() {
        super.onStop();
        if (gpsManager != null) {
            gpsManager.GPSstop();
        }
    }

    @Override
    public void handleNewLocation(Location location) {
        String lat = String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        Log.e("handleNewLocation 실행 : ", lat +" "+ lng);
        new AsyncReGeoJSONList().execute(lat, lng);
        new AsyncLatLonWeatherJSONList().execute(lat, lng);
    }

    LocationManager locationManager;
    private void locationProviderCheck() {
        if(locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        // TODO : 환경설정에서 GPS가 꺼져있을 경우를 구현
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getSupportFragmentManager().beginTransaction()
                    .add(MiddleSelectDialogFragment.newInstance(10),
                            "middle_gps_check").commit();
        } else {
            if (gpsManager == null) {
                gpsManager = new GPSManager(this, this);
                gpsManager.connect();
            }
        }
    }

    @Override
    public void onMiddleSelect(int select) {
        switch (select) {
            case 100 :
                PropertyManager.getInstance().setUseGps(false);
                break;
            case 101 :
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                PropertyManager.getInstance().setUseGps(true);
                break;
        }
    }
}
