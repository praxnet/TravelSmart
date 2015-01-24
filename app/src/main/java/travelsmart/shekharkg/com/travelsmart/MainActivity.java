package travelsmart.shekharkg.com.travelsmart;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by SKG on 1/23/2015.
 */
public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener, GoogleMap.OnMapLoadedCallback, GoogleMap.OnInfoWindowClickListener {

  private Spinner chooseCategory;
  private GoogleMap map;
  private String[] placeToVisit, audioFileNames;
  private String apiKey;
  private MediaPlayer player;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
    populateData();
  }

  private void populateData() {
    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1,placeToVisit);
    chooseCategory.setAdapter(categoryAdapter);
    map.setMyLocationEnabled(true);
    map.setOnMapLoadedCallback(this);
    map.setOnInfoWindowClickListener(this);
  }

  private void initView() {
    placeToVisit = getResources().getStringArray(R.array.places_to_visit);
    audioFileNames = getResources().getStringArray(R.array.audio_file_names);
    apiKey = getResources().getString(R.string.api_key);
    chooseCategory = (Spinner) findViewById(R.id.chooseCategorySpinner);
    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if(player != null) {
      player.stop();
      player.release();
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    if(position == 0)
      return;

    ConnectivityManager connec = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connec != null &&   (connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) ||
        (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED)) {
      final String type = placeToVisit[position].toLowerCase().replace(" ", "_");
      StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
      Location location = map.getMyLocation();
      googlePlacesUrl.append("location=" + location.getLatitude() + "," + location.getLongitude());
      googlePlacesUrl.append("&radius=" + "5000");
      googlePlacesUrl.append("&types=" + type);
      googlePlacesUrl.append("&sensor=true");
      googlePlacesUrl.append("&key=" + apiKey);
      new GetSearchResult(this).execute(googlePlacesUrl.toString());
    }else{
      Toast.makeText(this,"Check your connection!",Toast.LENGTH_LONG).show();
      chooseCategory.setSelection(0);
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }

  @Override
  public void onMapLoaded() {
    chooseCategory.setOnItemSelectedListener(this);
    Location location = map.getMyLocation();
    if (location != null) {
      LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
      map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
    }
  }

  public void searchOutput(String jsonString) {
    try{
      JSONObject jsonObject = new JSONObject(jsonString);
      JSONArray results = jsonObject.getJSONArray("results");

      if(results.length() == 0){
        Toast.makeText(this,"No result found",Toast.LENGTH_LONG).show();
        return;
      }

      map.clear();
      Location location = map.getMyLocation();
      if (location != null) {
        LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12));
      }
      for(int i=0; i< results.length(); i++){
        String name = results.getJSONObject(i).getString("name") + " (Tap to listen Audio Review)";
        String lat = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
        String lng = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
        Marker marker =  map.addMarker(new MarkerOptions().position(new LatLng(Float.parseFloat(lat), Float.parseFloat(lng))).title(name));
        marker.showInfoWindow();
      }
    }catch (Exception e){
      Log.e("Exception",e.toString());
    }
  }

  @Override
  public void onInfoWindowClick(Marker marker) {
    try {
      if(player != null) {
        player.stop();
        player.release();
      }

      player = new MediaPlayer();
      Random rn = new Random();
      int index = rn.nextInt(6 - 1 + 1) + 1;
      AssetFileDescriptor descriptor = getAssets().openFd(audioFileNames[index-1]);
      player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
      descriptor.close();
      player.prepare();
      player.setVolume(1f, 1f);
      player.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
