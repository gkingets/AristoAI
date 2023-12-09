package com.magic.chatai;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class x_DialogStyle extends DialogFragment {
    View view;

    // インターフェースの定義
    public interface DialogClosedListener {
        void onDialogClosed(String selectedStyle);
    }

    private DialogClosedListener dialogClosedListener;

    public void setDialogClosedListener(DialogClosedListener listener) {
        this.dialogClosedListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        // Shared Preferencesからスタイル情報を取得
        x_UtilsSharedPref xUtilsSharedPref = new x_UtilsSharedPref(requireActivity());
        String selectedStyle = xUtilsSharedPref.getWritingStyle();

        // ダイアログが閉じられたときにリスナーに通知
        if (dialogClosedListener != null) {
            dialogClosedListener.onDialogClosed(selectedStyle);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.x_dialog_style, null);

        builder.setView(view)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        setStyleRadioButton();


        return builder.create(); // returnを後で入れる
    }


    private void setStyleRadioButton() {
        RadioGroup radioGroup = view.findViewById(R.id.x_style_radio_button_group);

        // Shared Preferencesからスタイル情報を読み込む
        x_UtilsSharedPref xUtilsSharedPref = new x_UtilsSharedPref(requireActivity());
        String selectedStyle = xUtilsSharedPref.getWritingStyle();

        // スタイル情報に対応するラジオボタンを選択
        RadioButton radioButton;

        switch (selectedStyle) {
            case "business":
                radioButton = view.findViewById(R.id.x_style_radio_button_2);
                break;
            case "academic":
                radioButton = view.findViewById(R.id.x_style_radio_button_3);
                break;
            case "casual":
                radioButton = view.findViewById(R.id.x_style_radio_button_4);
                break;
            default:
                radioButton = view.findViewById(R.id.x_style_radio_button_1); // デフォルトのスタイルを設定
                break;
        }

        radioButton.setChecked(true);

        // ラジオボタンの選択状態が変更された場合の処理
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = view.findViewById(checkedId);

            String newStyle = "";

            switch (checkedId) {
                case R.id.x_style_radio_button_2:
                    newStyle = "business";
                    break;
                case R.id.x_style_radio_button_3:
                    newStyle = "academic";
                    break;
                case R.id.x_style_radio_button_4:
                    newStyle = "casual";
                    break;
                default:
                    newStyle = "standard";
                    break;
            }

            // 選択された新しいスタイル情報をShared Preferencesに保存
            xUtilsSharedPref.putWritingStyle(newStyle);
        });
    }


}
