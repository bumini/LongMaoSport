package com.live.longmao.activity.person;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.live.longmao.R;
import com.live.longmao.dlg.ActionSheetDialog;
import com.live.longmao.imageselector.MultiImageSelectorActivity;
import com.live.longmao.imageshow.ViewPagerFragment;
import com.live.longmao.model.ChangeEntayInfo;
import com.live.longmao.model.PersonInfo;
import com.live.longmao.model.PutPhotoInfo;
import com.live.longmao.pickerview.OptionsPickerView;
import com.live.longmao.presenters.ModificationMessageHelper;
import com.live.longmao.presenters.PersonHelper;
import com.live.longmao.presenters.ProfileInfoHelper;
import com.live.longmao.presenters.UpPhotoHelper;
import com.live.longmao.presenters.UploadHelper;
import com.live.longmao.presenters.viewinface.ModificationMessageView;
import com.live.longmao.presenters.viewinface.PersonView;
import com.live.longmao.presenters.viewinface.ProfileView;
import com.live.longmao.presenters.viewinface.UpPhotoView;
import com.live.longmao.util.ChangeHeight;
import com.live.longmao.util.GlideUtil;
import com.live.longmao.views.BaseActivity;
import com.live.longmao.views.EditActivity;
import com.live.longmao.views.customviews.BaseFragmentActivity;
import com.live.okhttp.OkHttpUtils;
import com.tencent.TIMUserProfile;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HPDN on 2017/1/13.
 */
public class PersonEditActivity extends BaseFragmentActivity implements PersonView, UpPhotoView, ModificationMessageView, View.OnClickListener, ProfileView {

    private RelativeLayout rl_phone, rl_sex;

    // 省数据集合
    private ArrayList<String> mListProvince = new ArrayList<String>();
    // 市数据集合
    private ArrayList<ArrayList<String>> mListCiry = new ArrayList<ArrayList<String>>();


    private OptionsPickerView<String> mOpv;
    private JSONObject mJsonObj;
    private TextView tv_edit_ctiy, tv_number, tv_edit_sex, tv_back, person_id, person_phone_id;
    private EditText et_context, person_nickname_et;
    private RelativeLayout rl_edit_hand, activity_base_title_rl;
    private ImageView iv_back;

    private int number = 140;

    private static final int Take_Photo = 1;

    private CircleImageView iv_user_guessing_head;

    private PersonHelper personHelper;
    private UpPhotoHelper upPhotoHelper;
    private ModificationMessageHelper modificationMessageHelper;
    private ProfileInfoHelper mProfileInfoHelper;
    private String userPhoto, name, sex, city, sig;
    private ArrayList<String> urlListTemp;
    private static final int REQUEST_IMAGE = 2;
    private final static int REQ_EDIT_NICKNAME = 0x100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_person_edit);

        personHelper = new PersonHelper(this);
        upPhotoHelper = new UpPhotoHelper(this);
        personHelper.getPerson();

        tv_edit_ctiy = (TextView) findViewById(R.id.tv_edit_ctiy);
        tv_number = (TextView) findViewById(R.id.tv_number);
        et_context = (EditText) findViewById(R.id.et_context);

        et_context.addTextChangedListener(sigWatcher);

        iv_user_guessing_head = (CircleImageView) findViewById(R.id.iv_user_guessing_head);
        activity_base_title_rl = (RelativeLayout) findViewById(R.id.activity_base_title_rl);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ChangeHeight.changeLH(this, activity_base_title_rl);
        }

        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_back.setOnClickListener(this);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);

        initfvb();
        // 初始化Json对象
        initJsonData();
        // 初始化Json数据
        initJsonDatas();

        // 创建选项选择器对象
        mOpv = new OptionsPickerView<String>(this);

        // 设置标题
        mOpv.setTitle("选择城市");

        // 设置三级联动效果
        mOpv.setPicker(mListProvince, mListCiry, true);

        // 设置是否循环滚动
        mOpv.setCyclic(false, false, false);

        // 设置默认选中的三级项目
        mOpv.setSelectOptions(0, 0, 0);

        // 监听确定选择按钮
        mOpv.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                // 返回的分别是三个级别的选中位置
                String tx = mListProvince.get(options1) + " " + mListCiry.get(options1).get(option2) + " ";
                tv_edit_ctiy.setText(tx);
            }
        });

        // 点击弹出选项选择器
        tv_edit_ctiy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpv.show();
            }
        });


    }

    private void initfvb() {
        modificationMessageHelper = new ModificationMessageHelper(this);
        mProfileInfoHelper = new ProfileInfoHelper(this);
        rl_phone = (RelativeLayout) findViewById(R.id.rl_phone);
        rl_sex = (RelativeLayout) findViewById(R.id.rl_sex);
        tv_edit_sex = (TextView) findViewById(R.id.tv_edit_sex);
        rl_edit_hand = (RelativeLayout) findViewById(R.id.rl_edit_hand);
        person_id = (TextView) findViewById(R.id.person_id);
        person_phone_id = (TextView) findViewById(R.id.person_phone_id);
        person_nickname_et = (EditText) findViewById(R.id.person_nickname_et);
        rl_phone.setOnClickListener(this);
        rl_sex.setOnClickListener(this);
        rl_edit_hand.setOnClickListener(this);
    }

    private void startImageSelect() {
        int selectedMode = MultiImageSelectorActivity.MODE_MULTI;

        if (false) {
            selectedMode = MultiImageSelectorActivity.MODE_SINGLE;
        } else {
            selectedMode = MultiImageSelectorActivity.MODE_MULTI;
        }

        boolean showCamera = true;
        //设置最多选择和显示图片的数量
        int maxNum = 1;

        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, showCamera);
        // 最大可选择图片数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, maxNum);
        // 选择模式
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, selectedMode);
        // 默认选择
        if (urlListTemp != null && urlListTemp.size() > 0) {
            intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, urlListTemp);
        }
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                urlListTemp = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                Log.i("urlListTemp", "------->" + urlListTemp);
                Log.i("urlListTemp", "======>" + data);
                if (urlListTemp.get(0) != null) {
                    userPhoto = urlListTemp.get(0);
                    GlideUtil.getInstance().load(this, iv_user_guessing_head, urlListTemp.get(0));
                }
                super.onActivityResult(requestCode, resultCode, data);

            }
        }
    }

    @Override
    public void onClick(View v) {
        // super.onClick(v);
        switch (v.getId()) {
            case R.id.rl_phone:
                startActivity(new Intent(PersonEditActivity.this, PersonEditPhoneActivity.class));
                break;
            case R.id.rl_sex:
                new ActionSheetDialog(this)
                        .builder()
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem("男", ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        sex = "1";
                                        tv_edit_sex.setText("男");
                //                        modificationMessageHelper.getSex("1");
                                    }
                                })
                        .addSheetItem("女", ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        sex = "0";
                                        tv_edit_sex.setText("女");
    //                                    modificationMessageHelper.getSex("0");
                                    }
                                }).show();
                break;

            case R.id.rl_edit_hand:
                startImageSelect();
                break;
            case R.id.iv_back:
                this.finish();
                break;
            case R.id.tv_back:
                if (userPhoto != null) {
                    upPhotoHelper.uploadPhoto(userPhoto + "");
                }

                initString();
               // modificationMessageHelper = new ModificationMessageHelper(this);
                modificationMessageHelper.getAll(city + "", name + "", sex + "", sig + "");
                //   finish();
                break;

        }
    }

    private void initString() {
        city = tv_edit_ctiy.getText().toString().trim();
        name = person_nickname_et.getText().toString().trim();
        sig = et_context.getText().toString().trim();
    }

    /**
     * 从assert文件夹中读取省市区的json文件，然后转化为json对象
     */
    private void initJsonData() {
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = getAssets().open("city.json");
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len, "UTF-8"));
            }
            is.close();
            mJsonObj = new JSONObject(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化Json数据，并释放Json对象
     */
    private void initJsonDatas() {
        try {
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonP = jsonArray.getJSONObject(i);// 获取每个省的Json对象
                String province = jsonP.getString("name");

                ArrayList<String> options2Items_01 = new ArrayList<String>();
                ArrayList<ArrayList<String>> options3Items_01 = new ArrayList<ArrayList<String>>();
                JSONArray jsonCs = jsonP.getJSONArray("city");
                for (int j = 0; j < jsonCs.length(); j++) {
                    JSONObject jsonC = jsonCs.getJSONObject(j);// 获取每个市的Json对象
                    String city = jsonC.getString("name");
                    options2Items_01.add(city);// 添加市数据

                    ArrayList<String> options3Items_01_01 = new ArrayList<String>();
                    JSONArray jsonAs = jsonC.getJSONArray("area");
                    for (int k = 0; k < jsonAs.length(); k++) {
                        options3Items_01_01.add(jsonAs.getString(k));// 添加区数据
                    }
                    options3Items_01.add(options3Items_01_01);
                }
                mListProvince.add(province);// 添加省数据
                mListCiry.add(options2Items_01);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mJsonObj = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
       // personHelper.getPerson();
        MobclickAgent.onPageStart("个人信息编辑");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPageEnd("个人信息编辑");
        super.onPause();
        MobclickAgent.onPause(this);
    }


    TextWatcher sigWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            temp = s;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
//          mTextView.setText(s);//将输入的内容实时显示
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            editStart = et_context.getSelectionStart();
            editEnd = et_context.getSelectionEnd();
            tv_number.setText(String.valueOf(number - temp.length()));
            if (temp.length() >= 140) {
                Toast.makeText(PersonEditActivity.this,
                        "字数超过限制！", Toast.LENGTH_SHORT)
                        .show();
                s.delete(editStart - 1, editEnd);
                int tempSelection = editStart;
                et_context.setText(s);
                et_context.setSelection(tempSelection);
            }
        }
    };


    @Override
    public void onPersonInfoSucc(PersonInfo result) {
        if (result.getBody().getPhotoUrl() != null) {
            GlideUtil.getInstance().load(this, iv_user_guessing_head, OkHttpUtils.Photo_Url + result.getBody().getPhoneId()+"");
        }
        if (result.getBody().getSex() == 1) {
            tv_edit_sex.setText("男");
        } else {
            tv_edit_sex.setText("女");
        }
        sex = result.getBody().getSex() + "";
        person_nickname_et.setText(result.getBody().getNickName() + "");
        person_id.setText(result.getBody().getId() + "");
        person_phone_id.setText(result.getBody().getUserName() + "");
        et_context.setText(result.getBody().getSigned() + "");
        tv_edit_ctiy.setText(result.getBody().getHobbies() + "");
    }

    @Override
    public void onPersonInfoError(String msg) {

    }

    @Override
    public void onUpPhotoSucc(PutPhotoInfo result) {
   //     modificationMessageHelper = new ModificationMessageHelper(this);
        modificationMessageHelper.getPhoto(result.getBody() + "");
    }

    @Override
    public void onUpPhotoError(String msg) {

    }

    @Override
    public void onModificationMessageSucc(ChangeEntayInfo result) {

        mProfileInfoHelper.setMyNickName(name + "");
       // personHelper.getPerson();
    }

    @Override
    public void onModificationMessageError(String msg) {

    }

    @Override
    public void updateProfileInfo(TIMUserProfile profile) {

        modificationMessageHelper.getMyName(profile.getNickName() + "");
        this.finish();
    }

    @Override
    public void updateUserInfo(int requestCode, List<TIMUserProfile> profiles) {

    }
}
