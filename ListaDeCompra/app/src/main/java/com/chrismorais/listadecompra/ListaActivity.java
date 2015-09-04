package com.chrismorais.listadecompra;

import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class ListaActivity extends ActionBarActivity {

	private Dao dao;
	private long id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista);

		dao = new Dao(this);
		dao.abrir();

		obterItens();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_lista, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		dao.fechar();

		super.onDestroy();
	}

	public void adicionarItem(View view) {
		EditText txt_item = (EditText)findViewById(R.id.txt_item);
		EditText txt_qtde = (EditText)findViewById(R.id.txt_qtde);

		double qtde = 0;
		String descricao = txt_item.getText().toString();

		if (!txt_qtde.getText().toString().equals("")) {
			qtde = Double.parseDouble(txt_qtde.getText().toString());
		}

		if (!descricao.equals("") && qtde != 0.0) {
			if (id != 0) {
				alterarItem(qtde, descricao);
			} else {
				inserirItem(qtde, descricao);
			}
		} else {
			Toast.makeText(this, "Os campos Item e Qtde são obrigatórios.",
					Toast.LENGTH_LONG).show();
		}
	}

	private void alterarItem(double qtde, String descricao) {
		boolean retorno = dao.alterarItem(id, descricao, qtde);

		if (retorno) {
			atualizarLista();
		} else {
			Toast.makeText(this, "Ocorreu um erro na alteração",
					Toast.LENGTH_SHORT).show();
		}

		limparCampos();

		Button btn_alterar = (Button)findViewById(R.id.btn_adicionar);
		btn_alterar.setText("ADICIONAR");
		id = 0;
	}

	private void inserirItem(double qtde, String descricao) {
		boolean retorno = dao.inserirItem(descricao, qtde);

		if (retorno) {
			atualizarLista();

			limparCampos();
		} else {
			Toast.makeText(this, "Ocorreu um erro na inclusão, tente novamente",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void limparCampos() {
		EditText txt_item = (EditText)findViewById(R.id.txt_item);
		EditText txt_qtde = (EditText)findViewById(R.id.txt_qtde);

		txt_item.setText("");
		txt_qtde.setText("");

		txt_item.requestFocus();
	}

	private void obterItens() {
		preencherListaItens(dao.listarItens());
	}

	private void preencherListaItens(Cursor cursor) {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.linha,
				cursor,
				new String[] {"descricao", "qtd"},
				new int[] {R.id.text1, R.id.text2}, 0);
		ListView lst_view = (ListView)findViewById(R.id.lst_itens);

		lst_view.setAdapter(adapter);
		lst_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id2) {
				boolean retorno = dao.excluirItem(id2);

				if (retorno) {
					atualizarLista();
				}
			}
		});

		lst_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			                               long id2) {
				Cursor cursor = (Cursor)parent.getItemAtPosition(position);
				id = cursor.getLong(cursor.getColumnIndex("_id"));

				EditText txt_item = (EditText)findViewById(R.id.txt_item);
				EditText txt_qtde = (EditText)findViewById(R.id.txt_qtde);

				txt_item.setText(cursor.getString(cursor.getColumnIndex("descricao")));
				txt_qtde.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndex("qtd"))));

				Button btn_alterar = (Button)findViewById(R.id.btn_adicionar);
				btn_alterar.setText("ALTERAR");

				return true;
			}
		});
	}

	private void atualizarLista(){
		ListView lst_view = (ListView)findViewById(R.id.lst_itens);
		SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter)lst_view.getAdapter();

		cursorAdapter.changeCursor(dao.listarItens());
	}
}
