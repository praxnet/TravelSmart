package travelsmart.shekharkg.com.travelsmart;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by SKG on 1/24/2015.
 */
public class GetSearchResult extends AsyncTask<String, Void, String> {

  private MainActivity mainActivity;

  public GetSearchResult(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
  }

  @Override
  protected String doInBackground(String... params) {
    Log.d("Url is",params[0]);
    return GetJsonString.GET(params[0]);
  }

  @Override
  protected void onPostExecute(String result) {
    mainActivity.searchOutput(result);
  }

}