package com.magic.chatai;

import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class DialogProgress extends DialogFragment {
    public DialogProgress(){} //空のコンストラクタ（DialogFragmentのお約束）

    //インスタンス作成
    public static DialogProgress newInstance() {
        return new DialogProgress();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_mypage);
        dialog.setCancelable(false);
        dialog.setTitle("更新中");
        return dialog;
    }

    public void show(FragmentManager fragmentManager, String test) {
    }
}
