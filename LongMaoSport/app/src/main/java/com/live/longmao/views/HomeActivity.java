package com.live.longmao.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.live.longmao.BaseApp;
import com.live.longmao.LocationManager;
import com.live.longmao.R;
import com.live.longmao.activity.HostLiveActivity;
import com.live.longmao.activity.person.PersonalCenterActivity;
import com.live.longmao.avcontrollers.QavsdkControl;
import com.live.longmao.fragment.person.PersonalCenterFragment;
import com.live.longmao.model.BrokenLineInfo;
import com.live.longmao.model.GradeInfo;
import com.live.longmao.model.MySelfInfo;
import com.live.longmao.model.PersonInfo;
import com.live.longmao.presenters.BrokenLineHelper;
import com.live.longmao.presenters.GradeHelper;
import com.live.longmao.presenters.InitBusinessHelper;
import com.live.longmao.presenters.LoginHelper;
import com.live.longmao.presenters.PersonHelper;
import com.live.longmao.presenters.ProfileInfoHelper;
import com.live.longmao.presenters.viewinface.BrokenLineView;
import com.live.longmao.presenters.viewinface.GradeView;
import com.live.longmao.presenters.viewinface.PersonView;
import com.live.longmao.presenters.viewinface.ProfileView;
import com.live.longmao.util.DisplayUtil;
import com.live.longmao.util.SxbLog;
import com.live.longmao.views.customviews.BaseFragmentActivity;
import com.live.okhttp.OkHttpUtils;
import com.tencent.TIMUserProfile;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * 主界面
 */
public class HomeActivity extends BaseFragmentActivity implements ProfileView, BrokenLineView, PersonView, GradeView {
    private FragmentTabHost mTabHost;
    private LayoutInflater layoutInflater;
    private ProfileInfoHelper infoHelper;
    private LoginHelper mLoginHelper;
    private final Class fragmentArray[] = {FragmentPublish.class, FragmentLiveList.class, PersonalCenterFragment.class};
    private int mImageViewArray[] = {R.drawable.tab_live, R.mipmap.icon_camera, R.drawable.tab_profile};
    private String mTextviewArray[] = {"publish", "live", "profile"};
    private static final String TAG = HomeActivity.class.getSimpleName();
    private boolean isShow = true;
    private BrokenLineHelper brokenLineHelper;
    private RelativeLayout rl_load;
    private PersonHelper personHelper;
    private GradeHelper gradeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLocation();
        personHelper = new PersonHelper(this);
        personHelper.getPerson();
        gradeHelper = new GradeHelper(this);
        gradeHelper.givingGift();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.home_layout);
        SxbLog.i(TAG, "HomeActivity onStart");
        rl_load = (RelativeLayout) findViewById(R.id.rl_load);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        layoutInflater = LayoutInflater.from(this);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.contentPanel);
        int fragmentCount = fragmentArray.length;
        for (int i = 0; i < fragmentCount; i++) {
            //为每一个Tab按钮设置图标、文字和内容
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            mTabHost.getTabWidget().setDividerDrawable(null);
        }
        mTabHost.getTabWidget().getChildTabViewAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                DialogFragment newFragment = InputDialog.newInstance();
//                newFragment.show(ft, "dialog");

                if (isShow) {
                    rl_load.setVisibility(View.VISIBLE);
                    brokenLineHelper.isBrokenLine(MySelfInfo.getInstance().getId());
                    personHelper.getPerson();
                } else {
                    mTabHost.setCurrentTab(1);
                    isShow = true;
                }
            }
        });
        // 检测是否需要获取头像
        if (TextUtils.isEmpty(MySelfInfo.getInstance().getAvatar())) {
            infoHelper = new ProfileInfoHelper(this);
            infoHelper.getMyProfile();
        }
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("publish") || tabId.equals("profile")) {
                    isShow = false;
                }
            }
        });
        mTabHost.setCurrentTab(1);
        isShow = true;
        //直播工具类
        brokenLineHelper = new BrokenLineHelper(this);
    }

    @Override
    protected void onStart() {
        SxbLog.i(TAG, "HomeActivity onStart");
        super.onStart();
        if (QavsdkControl.getInstance().getAVContext() == null) {//retry
            InitBusinessHelper.initApp(getApplicationContext());
            SxbLog.i(TAG, "HomeActivity retry login");
            mLoginHelper = new LoginHelper(this);
            mLoginHelper.imLogin(MySelfInfo.getInstance().getId(), MySelfInfo.getInstance().getUserSig());
        }
    }

    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.tab_content, null);
        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        icon.setImageResource(mImageViewArray[index]);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        Log.e(TAG, DisplayUtil.getSysWidth(this) / 6 + "");
        Log.e(TAG + "px2dip", DisplayUtil.px2dip(this, DisplayUtil.getSysWidth(this) / 6) + "");
        Log.e(TAG + "dip2px", DisplayUtil.dip2px(this, DisplayUtil.getSysWidth(this) / 6) + "");
        Log.e(TAG + "px2sp", DisplayUtil.px2sp(this, DisplayUtil.getSysWidth(this) / 6) + "");
        Log.e(TAG + "sp2px", DisplayUtil.sp2px(this, DisplayUtil.getSysWidth(this) / 6) + "");
        if (index == 0) {
            lp.setMargins(DisplayUtil.getSysWidth(this) / 6, DisplayUtil.dip2px(this, 23), 0, 0);
            icon.setLayoutParams(lp);
        }
        if (index == 2) {
            lp.setMargins(DisplayUtil.getSysWidth(this) / 12, DisplayUtil.dip2px(this, 23), 0, 0);
            icon.setLayoutParams(lp);
        }
        return view;
    }

    @Override
    protected void onDestroy() {
        if (mLoginHelper != null)
            mLoginHelper.onDestory();
        SxbLog.i(TAG, "HomeActivity onDestroy");
        QavsdkControl.getInstance().stopContext();
        super.onDestroy();
    }

    //存用户个人信息资料  在直播里面调用
    @Override
    public void updateProfileInfo(TIMUserProfile profile) {
//        SxbLog.i(TAG, "updateProfileInfo");
//        if (null != profile) {
//                    MySelfInfo.getInstance().setAvatar(profile.getFaceUrl());
//            if (!TextUtils.isEmpty(profile.getNickName())) {
//                MySelfInfo.getInstance().setNickName(profile.getNickName());
//            } else {
//                MySelfInfo.getInstance().setNickName(profile.getIdentifier());
//            }
//        }
    }

    @Override
    public void updateUserInfo(int reqid, List<TIMUserProfile> profiles) {
    }

    @Override
    public void onBrokenLineSucc(BrokenLineInfo result) {
        rl_load.setVisibility(View.GONE);
        Intent intent = new Intent(HomeActivity.this, HostLiveActivity.class);
        if (result.getId().equals("-1")) {
            intent.putExtra("isReconnection", "2");//1代表重连，2代表默认
        } else {
            BaseApp.getInstance().setLId(result.getId());
            intent.putExtra("isReconnection", "1");//1代表重连，2代表默认
        }
        startActivity(intent);
        overridePendingTransition(R.anim.activity_open, R.anim.activity_stay);
    }

    @Override
    public void onBrokenLineError(String msg) {
        rl_load.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void initLocation() {
        LocationManager.getInstance().addOnLocationListener(new LocationManager.OnLocationListener() {
            @Override
            public void onReceiveLocation(double longitude, double latitude, String cityCode, String city, String province, String country, String district, final String address) {
                BaseApp.getInstance().setLongitude(longitude);
                BaseApp.getInstance().setLatitude(latitude);
                Log.i("定位", String.valueOf(longitude));
                Log.i("定位", String.valueOf(latitude));
                BaseApp.getInstance().setCity(city);
                Log.d(TAG, "定位成功：" + longitude + "\t" + latitude + "地址:" + address);
            }

            @Override
            public void onFailLocation(int errorCode, String errorInfo) {
                Log.d(TAG, "定位失败，错误码：" + errorCode + "\t错误信息:" + errorInfo);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("主界面");
        personHelper.getPerson();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("主界面");
        MobclickAgent.onPause(this);
    }


    @Override
    public void onPersonInfoSucc(PersonInfo result) {
        if (result != null) {
            MySelfInfo.getInstance().setNickName(result.getBody().getNickName());
            MySelfInfo.getInstance().setAvatar(OkHttpUtils.Photo_Url + result.getBody().getPhotoUrl());
            MySelfInfo.getInstance().setNickName(result.getBody().getNickName() + "");
        }
    }

    @Override
    public void onPersonInfoError(String msg) {

    }

    @Override
    public void onGradeSucc(GradeInfo gradeInfo) {
        MySelfInfo.getInstance().setGrade(gradeInfo.getBody().getUserLevelDTO().getLevel());
    }

    @Override
    public void onGradeError(String msg) {

    }
}
