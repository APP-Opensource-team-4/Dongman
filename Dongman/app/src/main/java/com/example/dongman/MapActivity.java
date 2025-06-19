package com.example.dongman;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map); // activity_map.xml 레이아웃 사용

        locationName = getIntent().getStringExtra("location_name");

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // mapFragment가 null이 아님을 가정. (activity_map.xml에 @id/map 잘 정의되어 있어야 함)
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapActivity", "Error: Map fragment not found.");
            Toast.makeText(this, "지도 로드 오류: 맵을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Geocoder 서비스 사용
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // locationName이 유효한지 확인
            if (locationName == null || locationName.trim().isEmpty()) {
                Toast.makeText(this, "유효한 장소 이름이 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                Toast.makeText(this, "장소를 찾을 수 없습니다: " + locationName, Toast.LENGTH_LONG).show();
                finish(); // 장소를 찾지 못하면 MapActivity 종료
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "지도 로딩 오류 (네트워크 문제 또는 Geocoder 서비스 문제)", Toast.LENGTH_LONG).show();
            finish(); // 오류 발생 시 MapActivity 종료
        } catch (IllegalArgumentException e) { // Geocoder.getFromLocationName에 null 전달 시 발생 가능성 대비
            e.printStackTrace();
            Toast.makeText(this, "유효하지 않은 장소 이름입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}