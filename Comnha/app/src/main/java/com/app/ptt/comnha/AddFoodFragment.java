package com.app.ptt.comnha;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.app.ptt.comnha.Classes.Times;
import com.app.ptt.comnha.FireBase.Notification;
import com.app.ptt.comnha.SingletonClasses.LoginSession;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ptt.comnha.FireBase.Food;
import com.app.ptt.comnha.FireBase.FoodCategory;
import com.app.ptt.comnha.Service.MyService;
import com.app.ptt.comnha.SingletonClasses.ChooseLoca;
import com.app.ptt.comnha.SingletonClasses.DoPost;
import com.app.ptt.comnha.SingletonClasses.LoginSession;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFoodFragment extends Fragment implements View.OnClickListener{
    private static final String LOG=AddFoodFragment.class.getSimpleName();
    FloatingActionButton fab_themMon;
    RatingBar ratingBar;
    EditText edt_tenMon, edt_giamon;
    DatabaseReference dbRef;
    private ProgressDialog mProgressDialog;
    TextView txt_tenquan, txt_diachi;
    private String locaID;
    PickFoodCategoDialogFragment pickFoodDialog;
    FragmentManager dialogFm;
    boolean isConnected=true;
    Map<String, Object> childUpdates;
    float mRating=2;
    boolean isChange=false;
    IntentFilter mIntentFilter;
    public static final String mBroadcastSendAddress = "mBroadcastSendAddress";
    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(mBroadcastSendAddress)) {
                Log.i(LOG+".onReceive form Service","isConnected= "+ intent.getBooleanExtra("isConnected", false));
                if (intent.getBooleanExtra("isConnected", false)) {
                    isConnected = true;
                } else
                    isConnected = false;
            }
        }
    };
    public AddFoodFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isConnected= MyService.returnIsConnected();
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl(
                getResources().getString(R.string.firebase_path));
        locaID = ChooseLoca.getInstance().getLocation().getLocaID();
        View view = inflater.inflate(R.layout.fragment_addfood, container, false);
        anhxa(view);
        return view;
    }

    private void anhxa(View view) {
        ratingBar=(RatingBar) view.findViewById(R.id.rb_danhGiaMon);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mRating=rating;
                isChange=true;
                if (rating == 1)
                    Toast.makeText(getContext(), "Dở tệ", Toast.LENGTH_SHORT).show();
                if (rating == 2  )
                    Toast.makeText(getContext(), "Bình thường", Toast.LENGTH_SHORT).show();
                if (rating == 3)
                    Toast.makeText(getContext(), "Ngon tuyệt", Toast.LENGTH_SHORT).show();
            }
        });
        dialogFm = getActivity().getSupportFragmentManager();
        fab_themMon = (FloatingActionButton) view.findViewById(R.id.frg_themMon_btn_save);
        edt_giamon = (EditText) view.findViewById(R.id.frg_themMon_edt_giaMon);
        edt_tenMon = (EditText) view.findViewById(R.id.frg_themMon_edt_tenMon);
        txt_diachi = (TextView) view.findViewById(R.id.frg_themMon_txt_diachi);
        txt_tenquan = (TextView) view.findViewById(R.id.frg_themMon_txt_tenquan);
        txt_tenquan.setText(ChooseLoca.getInstance().getLocation().getName());
        txt_diachi.setText(ChooseLoca.getInstance().getLocation().getDiachi());
        fab_themMon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frg_themMon_btn_save:
                if (edt_tenMon.getText().toString().trim().equals("")) {
                    Snackbar.make(v, getResources().getString(R.string.txt_noTenMon), Snackbar.LENGTH_SHORT).show();

                }else if (!isChange) {
                    Snackbar.make(v, "Chưa đánh giá", Snackbar.LENGTH_SHORT).show();
                }

                else if (edt_giamon.getText().toString().trim().equals("")) {
                    Snackbar.make(v, getResources().getString(R.string.txt_nogiaMon), Snackbar.LENGTH_SHORT).show();
                }  else {
                    if(isConnected){
                        if(MyService.getUserAccount()!=null)
                            DoSave();
                        else Toast.makeText(getContext(), "You are offline", Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(getContext(), "You are offline", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void DoSave() {
        mProgressDialog = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.txt_plzwait),
                getResources().getString(R.string.txt_addinmon), true, false);
        final Food newFood = new Food();
        newFood.setGia(Long.valueOf(edt_giamon.getText().toString()));
        newFood.setDanhGia(mRating);
        newFood.setUserID(MyService.getUserAccount().getId());
        newFood.setTenmon(edt_tenMon.getText().toString());
        newFood.setLocaID(locaID);
        if(MyService.getUserAccount().getRole()){
            newFood.setVisible(true);
        }else
        newFood.setVisible(false);
        Log.i("Visible----Visible",newFood.getVisible()+"");
        String key = dbRef.child(getResources().getString(R.string.thucdon_CODE)+locaID).push().getKey();
        Map<String, Object> thucdonValue = newFood.toMap();
        childUpdates = new HashMap<>();
        childUpdates.put(
                getResources().getString(R.string.thucdon_CODE)
                + key, thucdonValue);
        if(!MyService.getUserAccount().getRole()) {
            Notification notification = new Notification();
            String key1 = dbRef.child(getResources().getString(R.string.notification_CODE) + "admin").push().getKey();
            notification.setAccount(MyService.getUserAccount());
            notification.setDate(new Times().getDate());
            notification.setTime(new Times().getTime());
            notification.setFood(newFood);
            notification.setType(1);
            notification.setLocation(ChooseLoca.getInstance().getLocation());
            notification.setReaded(false);
            notification.setTo("admin");
            Map<String, Object> notificationValue = notification.toMap();
            childUpdates.put(getResources().getString(R.string.notification_CODE) + "admin/" + key1, notificationValue);
            dbRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isComplete()) {
                        mProgressDialog.dismiss();
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.txt_addedMon), Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    } else {
                        mProgressDialog.dismiss();
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            mProgressDialog.dismiss();
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.txt_addedMon), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        isConnected= MyService.returnIsConnected();
        if(!isConnected){
            Toast.makeText(getContext(),"Offline mode",Toast.LENGTH_SHORT).show();
        }
        mIntentFilter=new IntentFilter();
        mIntentFilter.addAction(mBroadcastSendAddress);
        getContext().registerReceiver(broadcastReceiver,mIntentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getContext().unregisterReceiver(broadcastReceiver);
        ChooseLoca.getInstance().setLocation(null);


    }

}
