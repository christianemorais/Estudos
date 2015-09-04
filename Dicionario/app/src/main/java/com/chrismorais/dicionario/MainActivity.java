package com.chrismorais.dicionario;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends ActionBarActivity {

	String responseJSON;
	TextView txt_definicao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txt_definicao = (TextView)findViewById(R.id.txt_definicao);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	class ThreadConsulta extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			JSONObject jsonObject1;

			try {
				jsonObject1 = new JSONObject(responseJSON);
				JSONObject jsonObject2 = jsonObject1.getJSONObject("query");
				JSONObject jsonObject3 = jsonObject2.getJSONObject("pages");

				String pagina = (String)jsonObject3.keys().next();

				JSONObject jsonObject4 = jsonObject3.getJSONObject(pagina);
				String definicao = jsonObject4.getString("extract");

				txt_definicao.setText(definicao);
			} catch (JSONException e) {


			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			StringBuilder stringBuilder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			String endereco = "http://pt.wiktionary.org/w/api" +
					".php?format=json&action=query&prop=extracts&explaintext=&titles=";

			EditText txt_palavra = (EditText)findViewById(R.id.txt_palavra);

			endereco += txt_palavra.getText().toString();

			try {
				HttpGet httpGet = new HttpGet(endereco);
				HttpResponse httpResponse = client.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();

				InputStream conteudo = httpEntity.getContent();

				BufferedReader reader = new BufferedReader(new InputStreamReader(conteudo));
				String linha;

				while ((linha = reader.readLine()) != null) {
					stringBuilder.append(linha);
				}

				responseJSON = stringBuilder.toString();

			} catch (Exception e) {
				Toast.makeText(MainActivity.this, "Erro ao conectar: " + e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}

			return null;
		}
	}

	public void consultar(View view) {
		new ThreadConsulta().execute("");
	}
}


