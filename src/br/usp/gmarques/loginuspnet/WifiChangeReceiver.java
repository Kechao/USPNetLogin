package br.usp.gmarques.loginuspnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xml.sax.InputSource;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import br.usp.gmarques.loginuspnet.http.HttpUtils;

public class WifiChangeReceiver extends BroadcastReceiver {

	Context context = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		this.context = context;
		
		Log.v("LoginUSPNet", "Action: " + action);

		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo info = (NetworkInfo) intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (info.getDetailedState() == DetailedState.CONNECTED) {
				Log.v("LoginUSPNet", "Conectado");

				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (wifiInfo.getSSID().toUpperCase().contains("USP")) {
					Log.d("LoginUSPNet", "Rede USPNet detectada.");
					new loginThread().execute("USP");
				} else if (wifiInfo.getSSID().toUpperCase().contains("ICMC")) {
					Log.d("LoginUSPNet", "Rede ICMC detectada.");
					new loginThread().execute("ICMC");					
				}
			}

		}
	}

	private void sendRequest(String httpsURL, List<BasicNameValuePair> nvps)
			throws ClientProtocolException, IOException {

		HttpClient client = HttpUtils.getNewHttpClient();

		HttpPost httppost = new HttpPost(httpsURL);

		// Bloco de autenticacao
		UrlEncodedFormEntity p_entity;

		p_entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
		httppost.setEntity(p_entity);
		// Enviando a requisicao e recebendo a resposta
		HttpResponse response = client.execute(httppost);
		HttpEntity responseEntity = response.getEntity();

		// Tratando a resposta
		InputSource inputSource = new InputSource(responseEntity.getContent());
		BufferedReader in = new BufferedReader(new InputStreamReader(
				inputSource.getByteStream()));

		@SuppressWarnings("unused")
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			// Recebe a pagina de retorno. Pode ser usado para verificar se
			// obteve sucesso na autenticaco
			// Log.d("LoginUSPNet:", " " + inputLine);
		}
	}

	private class loginThread extends AsyncTask<String, Void, Void> {

		protected Void doInBackground(String... id) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			final List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
			
			if(id[0].toUpperCase().equals("USP")){

				final String httpsURL = "https://gwsc.semfio.usp.br:8001";

				nvps.add(new BasicNameValuePair("redirurl",	"https://www.google.com"));
				nvps.add(new BasicNameValuePair("auth_user", preferences.getString(context.getString(R.string.pref_username),"")));
				nvps.add(new BasicNameValuePair("auth_pass", preferences.getString(context.getString(R.string.pref_password),"")));
				nvps.add(new BasicNameValuePair("accept", "Continue"));

				try {
					sendRequest(httpsURL, nvps);
				} catch (ClientProtocolException e) {
					Log.e("LoginUSPNet", "ClientProtocolException while connecting to " + id[0] + " Message: "+ e.getMessage());
					Log.e("LoginUSPNet", " " + Log.getStackTraceString(e));
				} catch (IOException e) {
					Log.e("LoginUSPNet", "IOException while connecting to " + id[0] + " Message: "+ e.getMessage());
					Log.e("LoginUSPNet", " " + Log.getStackTraceString(e));
				}

			} else if(id[0].toUpperCase().equals("ICMC")){
				final String httpsURL = "https://1.1.1.1/login.html?redirect=https://www.google.com";

				nvps.add(new BasicNameValuePair("buttonClicked", "4"));
				nvps.add(new BasicNameValuePair("err_flag", "0"));
				nvps.add(new BasicNameValuePair("err_msg", ""));
				nvps.add(new BasicNameValuePair("info_flag", "0"));
				nvps.add(new BasicNameValuePair("info_msg", ""));
				nvps.add(new BasicNameValuePair("redirect_url",	"https://www.google.com"));
				nvps.add(new BasicNameValuePair("username", preferences.getString(context.getString(R.string.pref_username),"")));
				nvps.add(new BasicNameValuePair("password", preferences.getString(context.getString(R.string.pref_password),"")));

				try {
					sendRequest(httpsURL, nvps);
				} catch (ClientProtocolException e) {
					Log.e("LoginUSPNet", "ClientProtocolException while connecting to " + id[0] + " Message: "+ e.getMessage());
					Log.e("LoginUSPNet", " " + Log.getStackTraceString(e));
				} catch (IOException e) {
					Log.e("LoginUSPNet", "IOException while connecting to " + id[0] + " Message: "+ e.getMessage());
					Log.e("LoginUSPNet", " " + Log.getStackTraceString(e));
				}
				
			}
			
			return null;
		}
	}

}