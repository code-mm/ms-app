package org.ms.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.android.phone.scancode.export.ScanCallback;
import com.alipay.android.phone.scancode.export.ScanRequest;
import com.alipay.android.phone.scancode.export.ScanService;
import com.alipay.mobile.android.security.upgrade.util.UpgradeConstants;
import com.alipay.mobile.antui.basic.AUButton;
import com.alipay.mobile.antui.basic.AUToast;
import com.alipay.mobile.antui.utils.PublicResources;
import com.alipay.mobile.framework.LauncherApplicationAgent;
import com.alipay.mobile.framework.app.ui.BaseActivity;
import com.j256.ormlite.stmt.query.In;
import com.mpaas.framework.adapter.api.MPFramework;
import com.mpaas.hotpatch.adapter.api.MPHotpatch;
import com.mpaas.mpaasadapter.api.upgrade.MPUpgrade;
import com.ut.device.UTDevice;

import org.ms.module.supper.client.Modules;
import org.ms.module.supper.inter.module.Module;
import org.ms.module.utils.ToastUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button buttonToast;
    private Button aButtonToast;
    private Button aButtonScan;
    private Button buttonTag;
    private Button buttonRelease;
    private Button buttonHotFix;
    private Button buttonRSocket;
    private Button buttonRSocketJS;
    private TextView textViewRSocketRes;



    private Handler handler = new Handler(){


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final View decorView = getWindow().getDecorView();
        final int uiOption = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOption);


        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(uiOption);
                }
            }
        });


        setContentView(R.layout.activity_main);
        buttonToast = (Button) findViewById(R.id.buttonToast);
        buttonToast.setOnClickListener(this);
        aButtonToast = (Button) findViewById(R.id.aButtonToast);
        aButtonToast.setOnClickListener(this);
        aButtonScan = (Button) findViewById(R.id.aButtonScan);
        aButtonScan.setOnClickListener(this);
        buttonTag = (Button) findViewById(R.id.buttonTag);
        buttonTag.setOnClickListener(this);
        buttonRelease = (Button) findViewById(R.id.buttonRelease);
        buttonRelease.setOnClickListener(this);
        buttonHotFix = (Button) findViewById(R.id.buttonHotFix);
        buttonHotFix.setOnClickListener(this);
        buttonRSocket = (Button) findViewById(R.id.buttonRSocket);
        buttonRSocket.setOnClickListener(this);
        buttonRSocketJS = (Button) findViewById(R.id.buttonRSocketJS);
        buttonRSocketJS.setOnClickListener(this);

        textViewRSocketRes= (TextView) findViewById(R.id.textViewRSocketRes);



    }


    @Override
    public void onClick(View v) {


        switch (v.getId()) {

            case R.id.buttonToast:

                Modules.getUtilsModule().getToastUtils().show("Hello");

                break;

            case R.id.aButtonScan:
                scan();

                break;
            case R.id.aButtonToast:
                AUToast.makeToast(MainActivity.this,
                        PublicResources.Toast_OK, "Hello World ", Toast.LENGTH_SHORT).show();
                break;


            case R.id.buttonTag:

                Modules.getUtilsModule().getToastUtils().show(UTDevice.getUtdid(Modules.getDataModule().getApplication()));

                break;

            case R.id.buttonRelease:


                release();


                break;

            case R.id.buttonHotFix:

                hotFix();

                break;

            case R.id.buttonRSocket:

                testRSocket();

                break;

            case R.id.buttonRSocketJS:

                rsocketJs();

                break;



        }
    }

    private void rsocketJs() {


        startActivity(new Intent(this,Web.class));

    }


    private static final String TAG = "MainActivity";

    private void testRSocket() {


        Modules.getUtilsModule().getToastUtils().show("测试Rsocket");


        TcpClientTransport tcpClientTransport = TcpClientTransport.create(InetSocketAddress.createUnresolved("192.168.0.108", 9000));


        Mono<RSocket> socket = RSocketFactory.connect()
                .setupPayload(DefaultPayload.create("login", "{username:hello,password:world}"))
                .transport(tcpClientTransport)
                .start();

        socket.blockOptional()
                .ifPresent(rSocket -> {
                    rSocket.requestResponse(DefaultPayload.create("login", "{username:hello,password:world}"))
                            .doOnNext(new Consumer<Payload>() {
                                @Override
                                public void accept(Payload payload) {
                                    Modules.getUtilsModule().getToastUtils().show(Thread.currentThread().getName());
                                    String metadataUtf8 = payload.getMetadataUtf8();
                                    Modules.getUtilsModule().getThreadPoolUtils().runOnMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textViewRSocketRes.setText(metadataUtf8);
                                        }
                                    });
                                }
                            })
                            .block();
                    rSocket.dispose();
                });


    }

    private void hotFix() {

        MPHotpatch.init();

    }


    public void release() {

        final MPUpgrade mMPUpgrade = new MPUpgrade();


        Modules.getUtilsModule().getThreadPoolUtils().runSubThread(new Runnable() {
            @Override
            public void run() {


                // 同步方法，子线程中调用
                int result = mMPUpgrade.fastCheckHasNewVersion();
                if (result == UpgradeConstants.HAS_NEW_VERSION) {

                    // 有新版本
                    Modules.getUtilsModule().getToastUtils().show("有新版本");
                } else if (result == UpgradeConstants.HAS_NO_NEW_VERSION) {
                    // 没有新版本
                    Modules.getUtilsModule().getToastUtils().show("没有新版本");
                } else if (result == UpgradeConstants.HAS_SOME_ERROR) {
                    // 错误
                    Modules.getUtilsModule().getToastUtils().show("错误");
                }
            }
        });
    }


    public void scan() {

        ScanService service = LauncherApplicationAgent
                .getInstance().getMicroApplicationContext()
                .findServiceByInterface(ScanService.class.getName());
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setScanType(ScanRequest.ScanType.QRCODE);
        // 设置扫码界面 title
        scanRequest.setTitleText("标准扫码");
        // 设置扫码窗口下提示文字
        scanRequest.setViewText("提示文字");
        // 设置打开手电筒提示文字，仅 10.1.60 及以上基线支持
        scanRequest.setOpenTorchText("打开手电筒");
        // 设置关闭手电筒提示文字，仅 10.1.60 及以上基线支持
        scanRequest.setCloseTorchText("关闭手电筒");
        service.scan(this, scanRequest, new ScanCallback() {
            @Override
            public void onScanResult(boolean isProcessed, final Intent result) {
                if (!isProcessed) {
                    // 扫码界面点击物理返回键或左上角返回键
                    return;
                }
                // 注意：本回调是在子线程中执行
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null || result.getData() == null) {

                            AUToast.makeToast(MainActivity.this,
                                    PublicResources.Toast_Exception, "扫描失败 ", Toast.LENGTH_SHORT).show();

                            // 扫码失败
                            return;
                        }
                        // 扫码成功
                        String url = result.getData().toString();
                        AUToast.makeToast(MainActivity.this,
                                PublicResources.Toast_OK, "" + url, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

    }

}
