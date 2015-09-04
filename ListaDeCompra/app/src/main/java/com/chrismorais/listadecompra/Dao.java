package com.chrismorais.listadecompra;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class Dao {

	private final ListaOpenHelper openHelper;
	private SQLiteDatabase banco;

	public Dao (Context context) {
		openHelper = new ListaOpenHelper(context);
	}

	public void abrir() {
		if (banco == null) {
			banco = openHelper.getWritableDatabase();
		}
	}

	public  void fechar() {
		if (banco != null) {
			banco.close();
		}
	}

	public Cursor listarItens() {
		return banco.query("itens", null, null, null, null, null, "descricao");
	}

	public boolean inserirItem(String descricao, double qtde) {
		ContentValues content = getContentValues(descricao, qtde);

		return banco.insert("itens", null, content) > 0;
	}

	public boolean excluirItem (long id) {
		return banco.delete("itens", "_id = " + id, null) > 0;

	}

	public boolean alterarItem(long id, String descricao, double qtde) {
		ContentValues content = getContentValues(descricao, qtde);

		long retorno = banco.update("itens", content, "_id = " + id, null);

		return retorno >= 0;

	}

	private ContentValues getContentValues(String descricao, double qtde) {
		ContentValues content = new ContentValues();

		content.put("descricao", descricao);
		content.put("qtd", qtde);

		return content;
	}
}
