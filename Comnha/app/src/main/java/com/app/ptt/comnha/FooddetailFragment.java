package com.app.ptt.comnha;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ptt.comnha.Adapters.Photos_rcyler_adapter;
import com.app.ptt.comnha.Adapters.Reviewlist_rcyler_adapter;
import com.app.ptt.comnha.Adapters.Thucdon_rcyler_adapter;
import com.app.ptt.comnha.Classes.CalcuAVGRate;
import com.app.ptt.comnha.Classes.RecyclerItemClickListener;
import com.app.ptt.comnha.FireBase.Food;
import com.app.ptt.comnha.FireBase.Image;
import com.app.ptt.comnha.FireBase.MyLocation;
import com.app.ptt.comnha.FireBase.Post;
import com.app.ptt.comnha.Modules.ConnectionDetector;
import com.app.ptt.comnha.Modules.Storage;
import com.app.ptt.comnha.Service.MyService;
import com.app.ptt.comnha.SingletonClasses.ChooseFood;
import com.app.ptt.comnha.SingletonClasses.ChooseLoca;
import com.app.ptt.comnha.SingletonClasses.ChoosePost;
import com.app.ptt.comnha.SingletonClasses.DoPost;
import com.app.ptt.comnha.SingletonClasses.LoginSession;
import com.app.ptt.comnha.SingletonClasses.OpenAlbum;
import com.app.ptt.comnha.SingletonClasses.ReportLocal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FooddetailFragment extends Fragment {
    private String locaID;
    private static final String LOG = FooddetailFragment.class.getSimpleName();
    DatabaseReference dbRef;
    RecyclerView mRecyclerView, menuRecyclerView, albumRecyclerView;
    RecyclerView.Adapter mAdapter, menuAdapter, albumAdapter;
    RecyclerView.LayoutManager mlayoutManager, albumLayoutManager;
    TextView txt_name, txt_diachi, txt_ratingText;
    RatingBar ratingBar;
    ArrayList<Post> postlist;
    ValueEventListener locationValueEventListener;
    ChildEventListener postChildEventListener, imageChildEventListener;
    ActionBar actionBar;
    Toolbar toolbar;
    StorageReference storeRef;
    MyLocation location;
    ArrayList<Food> foodList;
    ArrayList<Image> files;
    TextView txtalbum;
    public static final String mBroadcastSendAddress = "mBroadcastSendAddress";
    boolean isConnected = false;
    IntentFilter mIntentFilter;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastSendAddress)) {
                Log.i(LOG + ".onReceive form Service", "isConnected= " + intent.getBooleanExtra("isConnected", false));
                if (intent.getBooleanExtra("isConnected", false)) {
                    if (!isConnected)
                        getData();
                    isConnected = true;
                } else
                    isConnected = false;
            }
        }
    };

    public FooddetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        isConnected = MyService.returnIsConnected();
        if (!isConnected) {
            Toast.makeText(getContext(), "Offline mode", Toast.LENGTH_SHORT).show();
        }
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastSendAddress);
        getContext().registerReceiver(broadcastReceiver, mIntentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getContext().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fooddetail, container, false);
        isConnected = MyService.returnIsConnected();
        locaID= ChooseFood.getInstance().getLocation().getLocaID();
        storeRef = FirebaseStorage.getInstance().getReferenceFromUrl(getResources().getString(R.string.firebaseStorage_path));
        if (locaID != null) {
            andxa(view);
            {
                getData();
                return view;
            }
        }


        return null;
    }

    public void getData() {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl(getResources().getString(R.string.firebase_path));

        postChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                post.setPostID(dataSnapshot.getKey());



                if(post.getType()==1 && post.getFood().getMonID().equals(ChooseFood.getInstance().getFood().getMonID())) {
                    Log.i("Mon id:"+post.getFood().getMonID(),"Mon:"+ChooseFood.getInstance().getFood().getMonID());

                    postlist.add(post);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        dbRef.child(
                getResources().getString(R.string.posts_CODE)).orderByChild("locaID").equalTo(locaID)
                .addChildEventListener(postChildEventListener);

    }

    private void andxa(View view) {
        txtalbum = (TextView) view.findViewById(R.id.food_detail_txtAlbum);

        toolbar = (Toolbar) view.findViewById(R.id.food_detail_toolbar);
        txt_diachi = (TextView) view.findViewById(R.id.frg_lcdetail_txt_diachi);
        txt_name = (TextView) view.findViewById(R.id.frg_lcdetail_txt_tenquan);
        ratingBar=(RatingBar) view.findViewById(R.id.food_detail_ratingbar);
        ratingBar.setIsIndicator(true);
        txt_ratingText=(TextView) view.findViewById(R.id.food_detail_ratingtext);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Location Detail");
        setHasOptionsMenu(true);
        postlist = new ArrayList<Post>();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.food_detail_rcyler_review);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mlayoutManager = linearLayoutManager;
        mRecyclerView.setLayoutManager(mlayoutManager);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        mAdapter = new Reviewlist_rcyler_adapter(postlist, getActivity(),2);
        mRecyclerView.setAdapter(mAdapter);


        foodList = new ArrayList<>();

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity().getApplicationContext()
                , new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isConnected) {
//                    if (LoginSession.getInstance().getUserID() == null) {
//                        Toast.makeText(getActivity(), getString(R.string.txt_needlogin),
//                                Toast.LENGTH_SHORT).show();
//                    } else {
                    Intent intent = new Intent(getActivity(), Adapter2Activity.class);
                    intent.putExtra(getString(R.string.fragment_CODE), getString(R.string.frg_viewpost_CODE));
                    ChoosePost.getInstance().setPostID(postlist.get(position).getPostID());
                   // ChoosePost.getInstance().setTinh(tinh);
                   // ChoosePost.getInstance().setHuyen(huyen);
                    startActivity(intent);
//                    }
                } else Toast.makeText(getContext(), "You are offline", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.option_menu_locadetail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.menu_locadetail_themmon:
                if (isConnected) {
                    if (LoginSession.getInstance().getUserID() == null) {
                    } else {
                        Intent intent = new Intent(getActivity(), Adapter2Activity.class);
                        intent.putExtra(getResources().getString(R.string.fragment_CODE),
                                getResources().getString(R.string.frg_themmon_CODE));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (location != null) {
                            ChooseLoca.getInstance().setLocation(location);
                            startActivity(intent);
                        } else {
                            ConnectionDetector.showLoadingAlert(getContext());
                        }
                    }
                    return true;
                } else Toast.makeText(getContext(), "You are offline", Toast.LENGTH_SHORT).show();
            case R.id.menu_locadetail_report:
                if (isConnected) {
                    ReportLocal.getInstance().setMyLocation(location);
                    ReportStoreDialogFragment reportStoreDialog = new ReportStoreDialogFragment();
                    reportStoreDialog.show(getActivity().getSupportFragmentManager(), "fragment_reportStore");
                    return true;
                } else Toast.makeText(getContext(), "You are offline", Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();

    }
}
