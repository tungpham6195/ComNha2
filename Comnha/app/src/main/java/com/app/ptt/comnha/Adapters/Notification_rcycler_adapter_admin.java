package com.app.ptt.comnha.Adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ptt.comnha.Classes.AnimationUtils;
import com.app.ptt.comnha.FireBase.Notification;
import com.app.ptt.comnha.R;
import com.app.ptt.comnha.Service.MyService;

import java.util.ArrayList;

/**
 * Created by cuong on 12/22/2016.
 */

public class Notification_rcycler_adapter_admin extends RecyclerView.Adapter<Notification_rcycler_adapter_admin.ViewHolder> {
    ArrayList<Notification> list;
    Activity activity;
    int previuosPosition = 0;
    int type;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_date, txt_time, txt_noidung;
        public LinearLayout ll_notification;
        public ImageButton button;


        public ViewHolder(View view) {
            super(view);
            button=(ImageButton) view.findViewById(R.id.btn__Option);
            ll_notification=(LinearLayout) view.findViewById(R.id.ll_item_notification);
            txt_date=(TextView) view.findViewById(R.id.txt_Ngay);
            txt_time=(TextView) view.findViewById(R.id.txt_Gio);
            txt_noidung=(TextView) view.findViewById(R.id.txt_Noidung);
        }
    }
    public Notification_rcycler_adapter_admin(ArrayList<Notification> list, Activity activity, int a) {
        this.list = list;
        this.activity = activity;
        type=a;
    }
    @Override
    public Notification_rcycler_adapter_admin.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rcyler_notification_admin, parent, false);

        return new Notification_rcycler_adapter_admin.ViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder( final ViewHolder holder,final int position) {
        if (type == 1) {

            if (!list.get(position).getReaded()) {
                holder.txt_date.setTypeface(null, Typeface.BOLD);
                holder.txt_time.setTypeface(null, Typeface.BOLD);
                holder.txt_noidung.setTypeface(null, Typeface.BOLD);
            } else {
                holder.txt_date.setTypeface(null, Typeface.NORMAL);
                holder.txt_time.setTypeface(null, Typeface.NORMAL);
                holder.txt_noidung.setTypeface(null, Typeface.NORMAL);
            }
            holder.txt_date.setText(list.get(position).getDate());
            holder.txt_time.setText(list.get(position).getTime());
            if (list.get(position).getTo().equals("admin")) {
                if (list.get(position).getType() == 1) {
                    String text = list.get(position).getAccount().getUsername() + " đã thêm món " + list.get(position).getFood().getTenmon() + " vào quán " + list.get(position).getLocation().getName();
                    holder.txt_noidung.setText(text);
                }
                if (list.get(position).getType() == 2) {
                    String text = list.get(position).getAccount().getUsername() + " đã thêm quán ăn:  " + list.get(position).getLocation().getName();
                    holder.txt_noidung.setText(text);
                }
                if (list.get(position).getType() == 3) {
                    String text = list.get(position).getAccount().getUsername() + " đã thêm review vào quán " + list.get(position).getLocation().getName();
                    holder.txt_noidung.setText(text);
                }
                if (list.get(position).getType() == 4) {
                    String text =  " đã report ";
                    holder.txt_noidung.setText(text);
                }
            }
        }

        //MyService.setFinish(true);
        if (position > previuosPosition) {
            AnimationUtils.animateItemRcylerV(holder, true);

        } else {
            AnimationUtils.animateItemRcylerV(holder, false);

        }
        //if(list.get(position).getIndex().equals("user")) {
        holder.ll_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!list.get(position).getReaded()) {
                    holder.txt_date.setTypeface(null, Typeface.NORMAL);
                    holder.txt_time.setTypeface(null, Typeface.NORMAL);
                    holder.txt_noidung.setTypeface(null, Typeface.NORMAL);
                    MyService.setPosNotification(position);
                }
            }
        });
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(activity.getApplicationContext(), v);
                if (list.get(position).getType() == 4) {


                    /** Adding menu items to the popumenu */
                    popup.getMenuInflater().inflate(R.menu.opt_menu_item_repo_admin, popup.getMenu());

                    /** Defining menu item click listener for the popup menu */

                }
                else
                {
                    popup.getMenuInflater().inflate(R.menu.opt_menu_item_noti_admin, popup.getMenu());
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_item_notification_xemnoidung:
                                Toast.makeText(activity.getApplicationContext(),"xem noi dung",Toast.LENGTH_SHORT).show();

                                break;
                            case R.id.menu_notification_dendiachi:
                                Toast.makeText(activity.getApplicationContext(),"den dia chi",Toast.LENGTH_SHORT).show();

                                break;
                            case R.id.menu_notification_dongy:
                                Toast.makeText(activity.getApplicationContext(),"dong y report",Toast.LENGTH_SHORT).show();

                                break;
                            case R.id.menu_notification_khongdongy:
                                Toast.makeText(activity.getApplicationContext(),"khong dong y report",Toast.LENGTH_SHORT).show();

                                break;
                        }
                        return true;
                    }
                });

                /** Showing the popup menu */
                popup.show();
            }
        });

        //}
        previuosPosition = position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
