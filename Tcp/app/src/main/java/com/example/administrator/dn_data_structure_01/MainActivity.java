package com.example.administrator.dn_data_structure_01;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.dn_data_structure_01.bean.MessageClient;
import com.example.administrator.dn_data_structure_01.bean.MessageServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    Button btnStartServer;
    Button btnSrartClient;

    Button btnSendToServer;
    Button btnSendToClient;

    TextView tv_server;
    TextView tv_client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnStartServer = findViewById(R.id.btn_start_server);
        btnSrartClient = findViewById(R.id.btn_start_client);
        btnSendToServer = findViewById(R.id.btn_send_server);
        btnSendToClient = findViewById(R.id.btn_send_client);
        tv_server = findViewById(R.id.tv_server);
        tv_client = findViewById(R.id.tv_client);

        btnStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启服务器
               TcpServer.startServer();
            }
        });

        btnSrartClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //连接服务器
                TcpClient.startClient( getIPAddress(getApplicationContext()) , 8080);
            }
        });

        btnSendToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送数据给服务器
                TcpClient.sendTcpMessage("321");
            }
        });

        btnSendToClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送数据给客户端
                TcpServer.sendTcpMessage("321");
            }
        });

        Log.i("tcp" ,"ip地址:" + getIPAddress(this));

        //注册EventBus
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    //收到客户端的消息
    public void MessageClient(MessageClient messageEvent) {
        tv_client.setText("客户端收到:" +messageEvent.getMsg());
    }

    //收到服务器的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MessageServer(MessageServer messageEvent) {
        tv_server.setText("服务器收到:" +messageEvent.getMsg());
    }


    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
