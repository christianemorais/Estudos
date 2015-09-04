package com.chrismorais.listadecompra;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ListaOpenHelper  extends SQLiteOpenHelper {

	public ListaOpenHelper(Context context) {
		super(context, "Lista.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE itens (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"descricao TEXT, qtd REAL)";

		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
