package com.whu.gnss.whu01;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;
import org.osmdroid.wms.WMSTileSource;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private MapView mapView = null;
    private Button m_btnRoute;     //路径按钮
    private LocationManager locationManager;           //位置传感器管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //虚拟机调试
        mapView = findViewById(R.id.osmMapView);
        mapView.setMultiTouchControls(true);
        WMSTileSource wmsTileSource = new WMSTileSource("OGC:WMS", new String[]{"http://202.114.122.22:2107/geoserver/whuXX/wms?service=WMS"}, "whuXX:basemap", "1.1.1", "EPSG:900913", "", 256);
        mapView.setTileSource(wmsTileSource);
        mapView.getController().setCenter(new GeoPoint(30.538, 114.3618));
        mapView.setMinZoomLevel(15.0);
        mapView.setMaxZoomLevel(20.0);
        mapView.getController().setZoom(15.0);

//        // 华为手机调试
//        Configuration.getInstance().setUserAgentValue(getPackageName());
//        mapView = findViewById(R.id.osmMapView);
//        mapView.setTileSource(TileSourceFactory.MAPNIK);
//        mapView.setMultiTouchControls(true);
//        mapView.getController().setCenter(new GeoPoint(30.538, 114.3618));
//        mapView.getController().setZoom(15.0);

        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        SimpleLocationOverlay overlay = new SimpleLocationOverlay(((BitmapDrawable) mapView.getContext().getDrawable(R.drawable.location)).getBitmap());
        overlay.setLocation(new GeoPoint(30.538, 114.3618));
        mapView.getOverlays().add(overlay);
        LocationListener locationListener = new LocationListener() {
            // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    overlay.setLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));
                }
            }
        };

        OnNmeaMessageListener mneaListener = new OnNmeaMessageListener() {
            @Override
            public void onNmeaMessage(String message, long timestamp) {
                //日志输出NMEA语句
                Log.i("log2", message);
            }
        };
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locationListener);
        locationManager.addNmeaListener(mneaListener);

        final String strrouteTask = "http://202.114.122.22:2107/geoserver/whuXX";
        m_btnRoute = findViewById(R.id.m_btnRoute);
        m_btnRoute.setOnClickListener(v -> {
            WhuRouteTask whuRouteTask = new WhuRouteTask("http://202.114.122.22:2107/geoserver/whuXX", "whuXX:route", new WhuRouteListner() {
                @Override
                public void onRoute(WhuRoute route) {
                    //添加查询到的路径叠置图层
                    mapView.getOverlays().add(route.getRouteGeometry());
                    List<GeoPoint> points = route.getRouteGeometry().getActualPoints();
                    //路径起点标注
                    Marker startMarker = new Marker(mapView);
                    startMarker.setPosition(points.get(0));
                    startMarker.setTextLabelFontSize(30);
                    startMarker.setTextIcon("S");
                    //路径终点标注
                    mapView.getOverlays().add(startMarker);
                    Marker endMarker = new Marker(mapView);
                    endMarker.setPosition(points.get(points.size()-1));
                    endMarker.setTextLabelFontSize(30);
                    endMarker.setTextIcon("E");
                    mapView.getOverlays().add(endMarker);
                    //强制重绘地图
                    mapView.invalidate();
                }
            });
            //设置路径查询起止点坐标
            whuRouteTask.setStops(new GeoPoint(30.53717,114.3488),new GeoPoint(30.538,114.3718));
            //执行异步路径查询
            whuRouteTask.solveRouteAsync();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

}