package com.chrismorais.android.sunshine.app;

import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.AbsListView;
import android.widget.Checkable;

public class ItemChoiceManager {
	private final String LOG_TAG = MainActivity.class.getSimpleName();
	private final String SELECTED_ITEMS_KEY = "SIK";
	private int mChoiceMode;

	private RecyclerView.Adapter mAdapter;
	private RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			if (mAdapter != null && mAdapter.hasStableIds())
				confirmCheckedPositionsById(mAdapter.getItemCount());
		}
	};

	public ItemChoiceManager(RecyclerView.Adapter adapter) {
		mAdapter = adapter;
	}

	private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;

	SparseBooleanArray mCheckStates = new SparseBooleanArray();

	LongSparseArray<Integer> mCheckedIdStates = new LongSparseArray<>();

	public void onClick(RecyclerView.ViewHolder vh) {
		if (mChoiceMode == AbsListView.CHOICE_MODE_NONE)
			return;

		int checkedItemCount = mCheckStates.size();
		int position = vh.getAdapterPosition();

		if (position == RecyclerView.NO_POSITION) {
			Log.d(LOG_TAG, "Unable to Set Item State");
			return;
		}

		switch (mChoiceMode) {
			case AbsListView.CHOICE_MODE_NONE:
				break;
			case AbsListView.CHOICE_MODE_SINGLE: {
				boolean checked = mCheckStates.get(position, false);
				if (!checked) {
					for (int i = 0; i < checkedItemCount; i++) {
						mAdapter.notifyItemChanged(mCheckStates.keyAt(i));
					}
					mCheckStates.clear();
					mCheckStates.put(position, true);
					mCheckedIdStates.clear();
					mCheckedIdStates.put(mAdapter.getItemId(position), position);
				}
				// We directly call onBindViewHolder here because notifying that an item has
				// changed on an item that has the focus causes it to lose focus, which makes
				// keyboard navigation a bit annoying
				mAdapter.onBindViewHolder(vh, position);
				break;
			}
			case AbsListView.CHOICE_MODE_MULTIPLE: {
				boolean checked = mCheckStates.get(position, false);
				mCheckStates.put(position, !checked);
				// We directly call onBindViewHolder here because notifying that an item has
				// changed on an item that has the focus causes it to lose focus, which makes
				// keyboard navigation a bit annoying
				mAdapter.onBindViewHolder(vh, position);
				break;
			}
			case AbsListView.CHOICE_MODE_MULTIPLE_MODAL: {
				throw new RuntimeException("Multiple Modal not implemented in ItemChoiceManager.");
			}
		}
	}

	public void setChoiceMode(int choiceMode) {
		if (mChoiceMode != choiceMode) {
			mChoiceMode = choiceMode;
			clearSelections();
		}
	}

	public boolean isItemChecked(int position) {
		return mCheckStates.get(position);
	}

	void clearSelections() {
		mCheckStates.clear();
		mCheckedIdStates.clear();
	}

	void confirmCheckedPositionsById(int oldItemCount) {
		// Clear out the positional check states, we'll rebuild it below from IDs.
		mCheckStates.clear();

		for (int checkedIndex = 0; checkedIndex < mCheckedIdStates.size(); checkedIndex++) {
			final long id = mCheckedIdStates.keyAt(checkedIndex);
			final int lastPos = mCheckedIdStates.valueAt(checkedIndex);

			final long lastPosId = mAdapter.getItemId(lastPos);
			if (id != lastPosId) {
				// Look around to see if the ID is nearby. If not, uncheck it.
				final int start = Math.max(0, lastPos - CHECK_POSITION_SEARCH_DISTANCE);
				final int end = Math.min(lastPos + CHECK_POSITION_SEARCH_DISTANCE, oldItemCount);
				boolean found = false;
				for (int searchPos = start; searchPos < end; searchPos++) {
					final long searchId = mAdapter.getItemId(searchPos);
					if (id == searchId) {
						found = true;
						mCheckStates.put(searchPos, true);
						mCheckedIdStates.setValueAt(checkedIndex, searchPos);
						break;
					}
				}

				if (!found) {
					mCheckedIdStates.delete(id);
					checkedIndex--;
				}
			} else {
				mCheckStates.put(lastPos, true);
			}
		}
	}

	public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
		boolean checked = isItemChecked(position);
		if (vh.itemView instanceof Checkable) {
			((Checkable) vh.itemView).setChecked(checked);
		}
		ViewCompat.setActivated(vh.itemView, checked);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		byte[] states = savedInstanceState.getByteArray(SELECTED_ITEMS_KEY);
		if ( null != states ) {
			Parcel inParcel = Parcel.obtain();
			inParcel.unmarshall(states, 0, states.length);
			inParcel.setDataPosition(0);
			mCheckStates = inParcel.readSparseBooleanArray();
			final int numStates = inParcel.readInt();
			mCheckedIdStates.clear();
			for (int i=0; i<numStates; i++) {
				final long key = inParcel.readLong();
				final int value = inParcel.readInt();
				mCheckedIdStates.put(key, value);
			}
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		Parcel outParcel = Parcel.obtain();
		outParcel.writeSparseBooleanArray(mCheckStates);
		final int numStates = mCheckedIdStates.size();
		outParcel.writeInt(numStates);
		for (int i=0; i<numStates; i++) {
			outParcel.writeLong(mCheckedIdStates.keyAt(i));
			outParcel.writeInt(mCheckedIdStates.valueAt(i));
		}
		byte[] states = outParcel.marshall();
		outState.putByteArray(SELECTED_ITEMS_KEY, states);
		outParcel.recycle();
	}

	public int getSelectedItemPosition() {
		if ( mCheckStates.size() == 0 ) {
			return RecyclerView.NO_POSITION;
		} else {
			return mCheckStates.keyAt(0);
		}
	}
}