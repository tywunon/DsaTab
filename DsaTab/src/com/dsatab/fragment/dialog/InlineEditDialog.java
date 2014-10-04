package com.dsatab.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;

import com.dsatab.R;
import com.dsatab.data.ArmorAttribute;
import com.dsatab.data.Attribute;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.Hero.CombatStyle;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.util.Debug;
import com.dsatab.util.NumberPickerUtils;
import com.dsatab.util.Util;

public class InlineEditDialog extends DialogFragment implements DialogInterface.OnClickListener,
		OnCheckedChangeListener {

	public static final String TAG = "InlineEditDialog";

	private Value value;

	private NumberPicker numberPicker;
	private NumberPickerUtils numberPickerUtils;

	private CompoundButton combatStyleBtn;
	private CheckBox beCalculation;

	public static void show(Fragment parent, Value value, int requestCode) {
		InlineEditDialog dialog = new InlineEditDialog();

		Bundle args = new Bundle();
		// TODO value should be set as argument
		dialog.value = value;
		dialog.setArguments(args);
		dialog.setTargetFragment(parent, requestCode);
		dialog.show(parent.getFragmentManager(), TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Bundle args = getArguments();
		// TODO value should be set as argument
		// Value value = (Value) args.get(KEY_VALUE);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		ViewGroup popupcontent = (ViewGroup) LayoutInflater.from(builder.getContext()).inflate(R.layout.popup_edit,
				null, false);
		builder.setView(popupcontent);

		numberPicker = (NumberPicker) popupcontent.findViewById(R.id.popup_edit_text);
		numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		numberPickerUtils = new NumberPickerUtils(numberPicker);

		combatStyleBtn = (CompoundButton) popupcontent.findViewById(R.id.popup_edit_combat_style);
		combatStyleBtn.setOnCheckedChangeListener(this);

		beCalculation = (CheckBox) popupcontent.findViewById(R.id.popup_edit_be_calculation);
		beCalculation.setOnCheckedChangeListener(this);

		builder.setPositiveButton("Ok", this);
		builder.setNegativeButton("Reset", this);

		AlertDialog dialog = builder.create();

		if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null && value != null) {
			dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(value.getReferenceValue() != null);
		}

		dialog.setCanceledOnTouchOutside(true);

		setValue(dialog, value);

		return dialog;
	}

	protected Value getValue() {
		return value;
	}

	protected void setValue(AlertDialog dialog, Value value) {
		this.value = value;

		if (value != null) {

			Integer currentValue = value.getValue();
			numberPickerUtils.setMinValue(value.getMinimum());
			numberPickerUtils.setMaxValue(value.getMaximum());

			if (currentValue != null) {
				numberPickerUtils.setValue(currentValue);
			} else {
				Debug.error("Setting value was null:" + value);
				numberPickerUtils.setValue(0);
			}
			numberPicker.setEnabled(true);
			numberPicker.setWrapSelectorWheel(false);

			int visible = View.GONE;
			if (value instanceof Attribute) {
				Attribute attr = (Attribute) value;
				if (attr.getType() == AttributeType.Behinderung) {
					visible = View.VISIBLE;
					combatStyleBtn.setChecked(attr.getHero().getCombatStyle() == CombatStyle.Offensive);
					beCalculation.setChecked(attr.getHero().isBeCalculation());
					numberPicker.setEnabled(!beCalculation.isChecked());
				}
			}

			combatStyleBtn.setVisibility(visible);
			beCalculation.setVisibility(visible);

			if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
				dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(value.getReferenceValue() != null);
			}
			dialog.setTitle(value.getName());
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == combatStyleBtn) {

			if (value instanceof Attribute) {
				Attribute attr = (Attribute) value;
				if (isChecked)
					attr.getHero().setCombatStyle(CombatStyle.Offensive);
				else
					attr.getHero().setCombatStyle(CombatStyle.Defensive);
			}
		} else if (buttonView == beCalculation) {
			numberPicker.setEnabled(!isChecked);

		}

	}

	private void accept() {
		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;
			if (attr.getType() == AttributeType.Behinderung) {
				attr.getHero().setBeCalculation(beCalculation.isChecked());

				// if we autocalculate the value to not overwrite it with
				// current value of editText afterwards
				if (beCalculation.isChecked()) {
					dismiss();
					Util.hideKeyboard(beCalculation);
					return;
				}
			}
		}

		try {

			int currentValue = numberPickerUtils.getValue();

			if (value instanceof ArmorAttribute) {
				ArmorAttribute armorAttribute = (ArmorAttribute) value;
				armorAttribute.setValue(currentValue, true);

			} else if (value instanceof CombatDistanceTalent) {
				int baseValue = ((CombatDistanceTalent) value).getBaseValue();
				value.setValue(currentValue - baseValue);
			} else {
				value.setValue(currentValue);
			}
		} catch (NumberFormatException e) {
			Debug.error(e);
		}
		Util.hideKeyboard(beCalculation);
		dismiss();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content .DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_POSITIVE:
			accept();
			break;

		case AlertDialog.BUTTON_NEGATIVE:
			value.reset();
			dismiss();
			break;
		}

	}

}