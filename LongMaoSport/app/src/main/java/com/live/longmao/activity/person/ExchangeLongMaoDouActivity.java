package com.live.longmao.activity.person;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.live.longmao.R;
import com.live.longmao.activity.PersonCoinActivity;
import com.live.longmao.model.BInfo;
import com.live.longmao.model.BeanInfo;
import com.live.longmao.model.CInfo;
import com.live.longmao.model.CoinInfo;
import com.live.longmao.model.CointoBeanInfo;
import com.live.longmao.model.CtoBInfo;
import com.live.longmao.model.MySelfInfo;
import com.live.longmao.presenters.BeanHelper;
import com.live.longmao.presenters.CoinHelper;
import com.live.longmao.presenters.CoinToBeanHelper;
import com.live.longmao.presenters.viewinface.BeanView;
import com.live.longmao.presenters.viewinface.CoinToBeanView;
import com.live.longmao.presenters.viewinface.CoinView;
import com.live.longmao.util.ToastUtil;
import com.live.longmao.views.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/16 0016.
 */
public class ExchangeLongMaoDouActivity extends BaseActivity implements BeanView, CoinView, CoinToBeanView {
    private TextView lmDouTv, lmBiYuTv, exlmDouTv, tv_exchange_bi;
    private EditText kaluliExchangeEt;
    private Button exchangeBtn;
    private BeanHelper beanHelper;
    private CoinHelper coinHelper;
    private CoinToBeanHelper coinToBeanHelper;

    private int intEtNumber;
    private String cintNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setView(R.layout.activity_exchange_longmao_dou);
        setTitle("兑换龙猫豆");
        setRigthImg(R.mipmap.tixian_wenhao);

        initEcView();
    }

    @Override
    protected void setRigthClick(View v) {
        super.setRigthClick(v);
        // TODO: 2017/2/16 0016 点击问号要作甚???
    }

    private void initEcView() {
        beanHelper = new BeanHelper(this);
        beanHelper.getBean(MySelfInfo.getInstance().getId());
        coinHelper = new CoinHelper(this);
        coinHelper.getCoin(MySelfInfo.getInstance().getId());
        coinToBeanHelper = new CoinToBeanHelper(this);

        lmDouTv = (TextView) findViewById(R.id.longmaodou_yu);
        lmBiYuTv = (TextView) findViewById(R.id.bi_yu_tv);
        exlmDouTv = (TextView) findViewById(R.id.exchange_longmaodou_tv);

        tv_exchange_bi = (TextView) findViewById(R.id.tv_exchange_bi);
        tv_exchange_bi.setOnClickListener(this);

        kaluliExchangeEt = (EditText) findViewById(R.id.et_exchange);
        kaluliExchangeEt.setOnClickListener(this);
        kaluliExchangeEt.addTextChangedListener(KLLWatcher);

        exchangeBtn = (Button) findViewById(R.id.exchange_bi_btn);
        exchangeBtn.setOnClickListener(this);


//        String num = kaluliExchangeEt.getText().toString();
//        intEtNumber = (Integer.parseInt(num))*10;
//        cintNum = String.format("%20d", intEtNumber);


    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            //输入要兑换的龙猫币数量
            case R.id.et_exchange:
                break;
            //兑换龙猫币
            case R.id.exchange_bi_btn:
                coinToBeanHelper.getCoinToBean(MySelfInfo.getInstance().getId(), kaluliExchangeEt.getText().toString().trim());
                break;
            //充值
            case R.id.tv_exchange_bi:
                startActivity(new Intent(ExchangeLongMaoDouActivity.this, PersonCoinActivity.class));
                finish();
                break;
        }
    }


    private TextWatcher KLLWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            if (s.length() != 0 && s.toString() != null){
//                Integer.parseInt(s.toString());
//            }else {
//                exlmDouTv.setText(0);
//            }


        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == 0) {
                exlmDouTv.setText("0");
            } else {
                int a = Integer.valueOf((Integer.valueOf(s.toString())) * 10);
                exlmDouTv.setText(String.valueOf(a));
            }
        }
    };


    @Override
    public void onBeanSucc(BeanInfo result) {
        if (null != result && null != result.getBody()) {
            ArrayList<BInfo> body = result.getBody();
            BInfo bInfo = body.get(0);
            lmDouTv.setText(String.valueOf(bInfo.getLmBeanNum()));
        } else {
            lmDouTv.setText("0");
        }
    }

    @Override
    public void onBeanError(String msg) {
        ToastUtil.showToast(this, msg);
    }

    @Override
    public void onCoinSucc(CoinInfo result) {
        if (null != result && null != result.getBody()) {
            List<CInfo> body = result.getBody();
            CInfo cInfo = body.get(0);
            lmBiYuTv.setText(String.valueOf(cInfo.getLmCoinNum()));
        } else {
            lmBiYuTv.setText("0");
        }
    }

    @Override
    public void onError(String msg) {
        ToastUtil.showToast(this, msg);

    }

    @Override
    public void onCoinToBeanSucc(CointoBeanInfo result) {
//        coinHelper.getCoin(MySelfInfo.getInstance().getId());
//        beanHelper.getBean(MySelfInfo.getInstance().getId());
        if (null != result && null != result.getBody()) {
            List<CtoBInfo> body = result.getBody();
            CtoBInfo cInfo = body.get(0);
            lmBiYuTv.setText(String.valueOf(cInfo.getLmCoinNum()));
            kaluliExchangeEt.setText("");
        }
        ToastUtil.showToast(this, "兑换成功");
    }

    @Override
    public void onCtoBError(String msg) {
        ToastUtil.showToast(this, msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        coinHelper.getCoin(MySelfInfo.getInstance().getId());
        beanHelper.getBean(MySelfInfo.getInstance().getId());
    }
}