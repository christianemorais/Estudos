package com.chrismorais.conversor;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity {

	int alturaEmCentimentros = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				double altura = progress / 100.0;

				alturaEmCentimentros = progress;

				TextView textView = (TextView)findViewById(R.id.textView2);

				String valorFormatado = DecimalFormat.getNumberInstance().format(altura);
				valorFormatado += " m";

				textView.setText(valorFormatado);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
	}

	public void converter(View view) {

		double alturaEmPes = (alturaEmCentimentros / 30.48);

		TextView altura_convertida = (TextView)findViewById(R.id.altura_convertida);

		String valorFormatado = DecimalFormat.getNumberInstance().format(alturaEmPes);
		valorFormatado += " pé(s)";

		altura_convertida.setText(valorFormatado);
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
}
