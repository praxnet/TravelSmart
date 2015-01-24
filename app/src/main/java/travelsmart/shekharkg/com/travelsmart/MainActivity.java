package travelsmart.shekharkg.com.travelsmart;

import android.location.Location;
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
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by SKG on 1/23/2015.
 */
public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener, GoogleMap.OnMapLoadedCallback {

  private Spinner chooseCategory;
  private GoogleMap map;
  private String[] placeToVisit;
  private String apiKey;

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
    chooseCategory.setOnItemSelectedListener(this);
    map.setMyLocationEnabled(true);
    map.setOnMapLoadedCallback(this);
  }

  private void initView() {
    placeToVisit = getResources().getStringArray(R.array.places_to_visit);
    apiKey = getResources().getString(R.string.api_key_search);
    chooseCategory = (Spinner) findViewById(R.id.chooseCategorySpinner);
    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    if(position == 0)
      return;
    final String type = placeToVisit[position].toLowerCase().replace(" ", "_");
    StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
    Location location = map.getMyLocation();
    googlePlacesUrl.append("location=" + location.getLatitude() + "," + location.getLongitude());
    googlePlacesUrl.append("&radius=" + "5000");
    googlePlacesUrl.append("&types=" + type);
    googlePlacesUrl.append("&sensor=true");
    googlePlacesUrl.append("&key=" + apiKey);
    new GetSearchResult(this).execute(googlePlacesUrl.toString());
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }

  @Override
  public void onMapLoaded() {
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
        String name = results.getJSONObject(i).getString("name");
        String lat = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
        String lng = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
        map.addMarker(new MarkerOptions().position(new LatLng(Float.parseFloat(lat), Float.parseFloat(lng))).title(name));
      }
    }catch (Exception e){
      Log.e("Exception",e.toString());
    }
  }

}
