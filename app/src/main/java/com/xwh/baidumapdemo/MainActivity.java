package com.xwh.baidumapdemo;

import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.xwh.baidumapdemo.receiver.BaiduListenerReceiver;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationManager locationmanger;
    private String provider;
    private BaiduListenerReceiver mReceiver;
    private String TAG = "百度地图";
    private BDLocationListener myListener = new MyLocationListenner();
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private Double lastX = 0.0;
    private MyLocationData locData;
    private boolean isFirstLoc = true;
    private SensorManager mSensorManager;


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
                Log.i(TAG, "id:" + marker.getId() + ",title" + marker.getTitle());
                return true;
            }
        });
        mBaiduMap.setOnMyLocationClickListener(new BaiduMap.OnMyLocationClickListener() {
            @Override
            public boolean onMyLocationClick() {
                Log.i(TAG, "定位图标监听事件");
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
        // 显示地图上比例尺
        mMapView.showScaleControl(true);
        // 显示地图缩放控件（按钮控制栏）
        mMapView.showZoomControls(true);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        BitmapDescriptor mCurrentMarker = null;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker));

        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));

        MapStatus.Builder builder1 = new MapStatus.Builder();
        builder1.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        LocationClient mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();




     /*   //定位功能
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
        }*/
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
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection)
                    .latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
//            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}
