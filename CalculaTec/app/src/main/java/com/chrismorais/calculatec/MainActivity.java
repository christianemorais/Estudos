package com.chrismorais.calculatec;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

	boolean digitandoNumero = false;
	Calculadora calculadora = new Calculadora();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

	public void clicarNumero(View view){
		Button botaoTocado = (Button)view;
		String numero = botaoTocado.getText().toString();

		TextView txt_visor = (TextView)findViewById(R.id.txt_visor);

		if (digitandoNumero) {
			txt_visor.setText(txt_visor.getText().toString() + numero);
		} else {
			txt_visor.setText(numero);

			digitandoNumero = true;
		}
	}

	public void clicarOperacao(View view){
		Button botaoTocado = (Button)view;
		String operacao = botaoTocado.getText().toString();

		TextView txt_visor = (TextView)findViewById(R.id.txt_visor);

		if (operacao.equals(".")) {
			tratarPontoDecimal(txt_visor);
		} else {
			realizarOperacao(operacao, txt_visor);

			digitandoNumero = false;
		}
	}

	private void realizarOperacao(String operacao, TextView txt_visor) {
		calculadora.setOperando(Double.parseDouble(txt_visor.getText().toString()));
		calculadora.realizarOperacao(operacao);

		String resultado = formatarResultado(String.valueOf(calculadora.getOperando()));

		txt_visor.setText(resultado);
	}

	private String formatarResultado(String resultado) {

		if (resultado.endsWith(".0")) {
			resultado = resultado.substring(0, resultado.length() - 2);
		}

		return resultado;
	}

	private void tratarPontoDecimal(TextView txt_visor) {
		if (!txt_visor.getText().toString().contains(".")) {
			txt_visor.setText(txt_visor.getText().toString() + ".");
		}
	}
}
