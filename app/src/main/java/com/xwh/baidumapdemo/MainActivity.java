package com.xwh.baidumapdemo;

import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.xwh.baidumapdemo.receiver.BaiduListenerReceiver;
import com.xwh.baidumapdemo.utils.BaiduLocationUtil;
import com.xwh.baidumapdemo.utils.MyLocationListener;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationManager locationmanger;
    private String provider;
    private BaiduListenerReceiver mReceiver;
    private String TAG = "百度地图";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        initBaiduMap();
        initListener();
    }

    private void initListener() {
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(TAG, marker.toString());
                Log.i(TAG, "id:" + marker.getId() + ",title" + marker.getTitle());
                return true;
            }
        });
    }

    /**
     * 开启百度地图监听事件
     */
    private void startBaiduClentReceiver() {
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new BaiduListenerReceiver();
        registerReceiver(mReceiver, iFilter);
    }

    /**
     * 设置百度地图
     */
    private void initBaiduMap() {
        mBaiduMap = mMapView.getMap();
        // 隐藏百度的LOGO
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
        // 不显示地图上比例尺
        mMapView.showScaleControl(true);
        // 不显示地图缩放控件（按钮控制栏）
        mMapView.showZoomControls(false);


        //定位功能
        BaiduLocationUtil util = new BaiduLocationUtil();
        util.initLocationClient(getApplicationContext(), new MyLocationListener(mBaiduMap));
        util.startLocation();
        //开启事件监听广播
        startBaiduClentReceiver();

        for (double i = 0.1; i < 0.3; i += 0.1) {
            LatLng latlng = new LatLng(39.928653 + i, 116.360814 + i);
            OverlayOptions options = new MarkerOptions()
                    .position(latlng)
                    .title(i + "")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_mark_business));
            mBaiduMap.addOverlay(options);
        }
    }

    /**
     * 设置比例尺大小
     * 暂时设置为1公里
     * Level:18.0f -- 3.0f
     * {"50m","100m","200m","500m","1km","2km","5km","10km","20km",
     * "25km","50km","100km","200km","500km","1000km","2000km"}
     */
    private void setMapStatus() {
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(14.0f);
        mBaiduMap.animateMapStatus(u);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
