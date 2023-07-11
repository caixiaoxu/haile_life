package com.yunshang.haile_life.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.lsy.framelib.async.LiveDataBus;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.yunshang.haile_life.business.event.BusEvents;
import com.yunshang.haile_life.utils.thrid.WeChatHelper;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, WeChatHelper.APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.getType()) {
            case ConstantsAPI.COMMAND_PAY_BY_WX: //支付回调
                LiveDataBus.post(BusEvents.WXPAY_STATUS, true);
                break;
        }
        finish();
    }
}