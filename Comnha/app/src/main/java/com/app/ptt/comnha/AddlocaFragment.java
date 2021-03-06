package com.app.ptt.comnha;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.ptt.comnha.Classes.Times;
import com.app.ptt.comnha.FireBase.MyLocation;
import com.app.ptt.comnha.FireBase.Notification;
import com.app.ptt.comnha.Modules.LocationFinderListener;
import com.app.ptt.comnha.Modules.PlaceAPI;
import com.app.ptt.comnha.Modules.PlaceAttribute;
import com.app.ptt.comnha.Service.MyService;
import com.app.ptt.comnha.Service.MyTool;
import com.app.ptt.comnha.SingletonClasses.ChooseLoca;
import com.app.ptt.comnha.SingletonClasses.LoginSession;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddlocaFragment extends Fragment implements View.OnClickListener, TimePickerDialog.OnTimeSetListener,
        DialogInterface.OnDismissListener, DialogInterface.OnCancelListener, LocationFinderListener {
    FloatingActionButton fab_save;
    public static final String LOG = AddlocaFragment.class.getSimpleName();
    ArrayList<PlaceAttribute> mPlaceAttribute;
    EditText edt_tenquan, edt_sdt,
            edt_giamin, edt_giamax;
    Button btn_timestart, btn_timeend;
    ProgressDialog mProgressDialog;
    DatabaseReference dbRef;
    ImageView img_ic;
    Calendar now;
    String tinh, huyen;
    TimePickerDialog tpd;
    int edtID, pos=-1;
    MyLocation newLocation;
    int hour, min;
    Geocoder gc;
    PlaceAPI placeAPI;
    AutoCompleteTextView autoCompleteText;
    String a = "";
    MyTool myTool;
    boolean isConnected=true;
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
    }

    public AddlocaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_addloca, container, false);
        isConnected= MyService.returnIsConnected();
        now = Calendar.getInstance();
        gc = new Geocoder(getContext(), Locale.getDefault());
        anhXa(view);
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl(getResources().getString(R.string.firebase_path));
        autoCompleteText = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
        autoCompleteText.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));
        autoCompleteText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = position;

            }
        });
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    void anhXa(View view) {
        edt_tenquan = (EditText) view.findViewById(R.id.frg_addloction_edt_tenquan);
        //edt_diachi = (EditText) view.findViewById(R.id.frg_addloction_edt_diachi);
        btn_timeend = (Button) view.findViewById(R.id.frg_addloction_btn_giodong);
        btn_timestart = (Button) view.findViewById(R.id.frg_addloction_btn_giomo);
        edt_sdt = (EditText) view.findViewById(R.id.frg_addloction_edt_sdt);
        edt_giamin = (EditText) view.findViewById(R.id.frg_addloction_edt_giamin);
        edt_giamax = (EditText) view.findViewById(R.id.frg_addloction_edt_giamax);
        fab_save = (FloatingActionButton) view.findViewById(R.id.frg_addloction_btn_save);
        img_ic = (ImageView) view.findViewById(R.id.frg_addloca_ic);
        //edt_quanhuyen = (EditText) view.findViewById(R.id.frg_addloction_edt_quanhuyen);
        //edt_tinhTP = (EditText) view.findViewById(R.id.frg_addloction_edt_tinhtp);
        tpd = TimePickerDialog.newInstance(this, now.get(Calendar.HOUR), now.get(Calendar.MINUTE), now.get(Calendar.SECOND), true);
        tpd.setOnDismissListener(this);
        tpd.setOnCancelListener(this);
        img_ic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        fab_save.setOnClickListener(this);
        btn_timeend.setOnClickListener(this);
        btn_timestart.setOnClickListener(this);
    }


    class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        ArrayList<String> resultList;
        Context mContext;
        int mResource;

        public PlacesAutoCompleteAdapter(Context context, int resource) {
            super(context, resource);
            mContext = context;
            mPlaceAttribute = new ArrayList<>();
            mResource = resource;
            myTool = new MyTool(getContext(), AddlocaFragment.class.getSimpleName());
        }

        @Override
        public int getCount() {
            if (resultList != null) {
                return resultList.size();
            } else return 0;
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return resultList.get(position);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            final Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        mPlaceAttribute = new ArrayList<>();
                        mPlaceAttribute = myTool.returnPlaceAttributeByName(constraint.toString());
                        if (mPlaceAttribute != null) {
                            a = "OK";
                            resultList = new ArrayList<>();
                            for (PlaceAttribute placeAttribute : mPlaceAttribute)
                                resultList.add(placeAttribute.getFullname());
                            filterResults.values = resultList;
                            filterResults.count = resultList.size();
                        } else {
                            a = null;
                            PlaceAttribute a1 =new PlaceAttribute();
                            a1.setFullname(constraint.toString());
                            mPlaceAttribute.add(a1);
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    public String returnFullname() {
        String a = mPlaceAttribute.get(pos).getAddressNum();
        String b = mPlaceAttribute.get(pos).getLocality();
        String c = mPlaceAttribute.get(pos).getDistrict();
        String d = mPlaceAttribute.get(pos).getState();
        String e = "";
        if (a != null)
            e += a;
        if (b != null)
            if (a == null)
                e += b;
            else
                e += ", " + b;

        if (c != null)
            if (a == null && b == null)
                e += c;
            else
                e += ", " + c;
        if (d != null)
            if (a == null && b == null && c == null)
                e += d;
            else
                e += ", " + d;
        return e;
    }

    private void addNewLoca() {
        Log.i(LOG + ".addNewLoca", "Da toi đây");
        newLocation = new MyLocation();
        newLocation.setName(edt_tenquan.getText().toString());
        newLocation.setSdt(edt_sdt.getText().toString());
        newLocation.setTimestart(btn_timestart.getText().toString());
        newLocation.setTimeend(btn_timeend.getText().toString());
        newLocation.setGiamin(Long.valueOf(edt_giamin.getText().toString()));
        newLocation.setGiamax(Long.valueOf(edt_giamax.getText().toString()));
        if(isConnected){
        if(pos!=-1) {
            placeAPI = new PlaceAPI(mPlaceAttribute.get(pos).getFullname(), this);
        }else{
            placeAPI = new PlaceAPI(autoCompleteText.getText().toString(), this);
        }
        } else   Toast.makeText(getContext(), "You are offline", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationFinderStart() {

    }

    @Override
    public void onLocationFinderSuccess(PlaceAttribute placeAttribute) {
        if (placeAttribute != null &&placeAttribute.getState()!=null&&placeAttribute.getDistrict()!=null) {
            final LatLng newLatLng = myTool.returnLatLngByName(placeAttribute.getFullname());
            final PlaceAttribute myPlaceAttribute = placeAttribute;
            newLocation.setDiachi(placeAttribute.getFullname());
            Log.i(LOG + ".onLocationFinder", placeAttribute.getState() + "-" + placeAttribute.getDistrict());
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Địa chỉ: " + placeAttribute.getFullname()).setTitle("Xác nhận")
                    .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mProgressDialog = ProgressDialog.show(getActivity(),
                                    getResources().getString(R.string.txt_plzwait),
                                    getResources().getString(R.string.txt_addinloca), true, true);
                            newLocation.setLat(newLatLng.latitude);
                            newLocation.setLng(newLatLng.longitude);
                            newLocation.setTinhtp(myPlaceAttribute.getState());
                            newLocation.setQuanhuyen(myPlaceAttribute.getDistrict());
                            newLocation.setTime(new Times().getTime());
                            newLocation.setDate(new Times().getDate());
                            if(MyService.getUserAccount().getRole())
                                newLocation.setVisible(true);
                            else
                                newLocation.setVisible(false);
                            newLocation.setUserId(MyService.getUserAccount().getId());
                            Log.i(LOG + ".onLocation", tinh + " va " + huyen);
                            String key = dbRef.child(getResources().getString(R.string.locations_CODE)).push().getKey();
                            Map<String, Object> newLocaValue = newLocation.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(getResources().getString(R.string.locations_CODE)
                                    + key, newLocaValue);
                            dbRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                    } else {
                                        if(!MyService.getUserAccount().getRole()) {
                                            Map<String, Object> childUpdates = new HashMap<>();
                                            Notification notification = new Notification();
                                            String key1 = dbRef.child(getResources().getString(R.string.notification_CODE) + "admin").push().getKey();
                                            notification.setAccount(MyService.getUserAccount());
                                            notification.setDate(new Times().getDate());
                                            notification.setTime(new Times().getTime());
                                            notification.setType(2);
                                            notification.setLocation(newLocation);
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
                                                                getResources().getString(R.string.text_noti_addloca_succes)
                                                                , Toast.LENGTH_SHORT).show();
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
                                                    getResources().getString(R.string.text_noti_addloca_succes)
                                                    , Toast.LENGTH_SHORT).show();
                                            getActivity().finish();
                                        }



                                    }
                                }

                            });


                        }
                    })
                    .setNegativeButton("Thử lại", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            //builder.create();
        } else {
            //mProgressDialog.dismiss();
            Toast.makeText(getActivity(), "Lỗi! Kiểm tra dữ liệu nhập vàp ", Toast.LENGTH_LONG).show();
        }
        pos=-1;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.frg_addloction_btn_save:
                if (edt_tenquan.getText().toString().trim().equals("")) {
                    Snackbar.make(view, getResources().getText(R.string.txt_notenquan), Snackbar.LENGTH_SHORT).show();
                } else if (autoCompleteText.getText().toString().trim().equals("")) {
                    Snackbar.make(view, getResources().getText(R.string.txt_nodiachi), Snackbar.LENGTH_SHORT).show();
                } else if (edt_giamin.getText().toString().trim().equals("")) {
                    Snackbar.make(view, getResources().getText(R.string.txt_nogia), Snackbar.LENGTH_SHORT).show();
                } else if (edt_giamax.getText().toString().trim().equals("")) {
                    Snackbar.make(view, getResources().getText(R.string.txt_nogia), Snackbar.LENGTH_SHORT).show();
//                } else if (edt_sdt.getText().toString().trim().equals("")) {
//                    Snackbar.make(view, getResources().getText(R.string.txt_nosdt), Snackbar.LENGTH_SHORT).show();
//                } else if (edt_timestart.getText().toString().trim().equals("")) {
//                    Snackbar.make(view, getResources().getText(R.string.txt_noopentime), Snackbar.LENGTH_SHORT).show();
//                } else if (edt_timeend.getText().toString().trim().equals("")) {
//                    Snackbar.make(view, getResources().getText(R.string.txt_noopentime), Snackbar.LENGTH_SHORT).show();
                } else if (Long.valueOf(edt_giamax.getText().toString()) <= Long.valueOf(edt_giamin.getText().toString())) {
                    Snackbar.make(view, getResources().getText(R.string.txt_giawarn), Snackbar.LENGTH_SHORT).show();
                } else {
                        if(isConnected ){
                            if(MyService.getUserAccount()!=null )
                                addNewLoca();
                            else
                                Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
                        }

                    else
                            Toast.makeText(getContext(), "You are offline", Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.frg_addloction_btn_giomo:
                edtID = R.id.frg_addloction_btn_giomo;
                tpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
                break;
            case R.id.frg_addloction_btn_giodong:
                edtID = R.id.frg_addloction_btn_giodong;
                tpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
                break;
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        switch (edtID) {
            case R.id.frg_addloction_btn_giomo:
                hour = hourOfDay;
                min = minute;
                break;
            case R.id.frg_addloction_btn_giodong:
                hour = hourOfDay;
                min = minute;
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        hour = -1;
        Log.d("cancel" + String.valueOf(edtID), String.valueOf(hour));
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        Log.d("dismiss" + String.valueOf(edtID), String.valueOf(hour));
        switch (edtID) {
            case R.id.frg_addloction_btn_giomo:
                if (hour > -1) {
                    btn_timestart.setText(hour + "h" + min);
                }
                break;
            case R.id.frg_addloction_btn_giodong:
                if (hour > -1) {
                    btn_timeend.setText(hour + "h" + min);
                }
                break;
        }
    }
}
