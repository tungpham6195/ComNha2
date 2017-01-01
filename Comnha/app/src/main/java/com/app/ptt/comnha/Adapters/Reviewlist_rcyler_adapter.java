package com.app.ptt.comnha.Adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.ptt.comnha.Classes.AnimationUtils;
import com.app.ptt.comnha.FireBase.Image;
import com.app.ptt.comnha.FireBase.Post;
import com.app.ptt.comnha.R;
import com.app.ptt.comnha.Service.MyService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by PTT on 10/4/2016.
 */

public class Reviewlist_rcyler_adapter extends RecyclerView.Adapter<Reviewlist_rcyler_adapter.ViewHolder> {
    ArrayList<Post> list;
    Activity activity;
    ArrayList<String> bitmapList;
    public int previuosPosition = 0;
    public static int type;
    Bitmap mBitmap;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_un, txt_postdate, txt_tenmon,
                txt_gia, txt_likenumb, txt_commentnumb, txt_time, txt_tiltereview, txt_diachi, txt_ratingtext,txt_tenquan;
        public RatingBar ratingBar;
        public ImageView img_user;

        public ViewHolder(View view) {
            super(view);
            if(type==2){
                txt_time = (TextView) view.findViewById(R.id.food_txtv_time);
                txt_un = (TextView) view.findViewById(R.id.food_txtv_username);
                txt_postdate = (TextView) view.findViewById(R.id.food_txtv_postdate);
                txt_likenumb = (TextView) view.findViewById(R.id.food_txtv_likenumb);
                txt_commentnumb = (TextView) view.findViewById(R.id.food_txtv_commentnumb);
                txt_tiltereview = (TextView) view.findViewById(R.id.txt_tiltereview);
                ratingBar= (RatingBar) view.findViewById(R.id.ratingbar_item);

                txt_ratingtext=(TextView) view.findViewById(R.id.rating_text);
            }else{
                txt_time = (TextView) view.findViewById(R.id.review_txtv_time);
                txt_un = (TextView) view.findViewById(R.id.review_txtv_username);
                txt_tenmon = (TextView) view.findViewById(R.id.review_txtv_tenmon);
                txt_postdate = (TextView) view.findViewById(R.id.review_txtv_postdate);
                txt_gia = (TextView) view.findViewById(R.id.review_txtv_gia);
                txt_likenumb = (TextView) view.findViewById(R.id.review_txtv_likenumb);
                txt_commentnumb = (TextView) view.findViewById(R.id.review_txtv_commentnumb);
                txt_tenquan = (TextView) view.findViewById(R.id.review_txtv_tenquan);
                txt_diachi = (TextView) view.findViewById(R.id.review_txtv_diachi);
                img_user = (ImageView) view.findViewById(R.id.review_img_quan);
            }


        }
    }
    public Reviewlist_rcyler_adapter(ArrayList<Post> list, Activity activity,int type) {
        this.list = list;
        this.activity = activity;
        bitmapList=new ArrayList<>();
        this.type=type;
        mBitmap=BitmapFactory.decodeResource(activity.getResources(),R.drawable.ic_logo);

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if(type==2){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rcyler_fooddetail, parent, false);
        }else
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rcyler_review_food, parent, false);

        return new ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if(type==2){
            if (list.get(position).getType() == 1) {
                holder.ratingBar.setNumStars(3);
                holder.ratingBar.setIsIndicator(true);
                holder.ratingBar.setStepSize(1);
                int a = (int) list.get(position).getFood().getDanhGia();
                holder.ratingBar.setRating(a);
                if (a == 1) {
                    holder.txt_ratingtext.setText("Dở tệ");
                }
                if (a == 2) {
                    holder.txt_ratingtext.setText("Bình thường");
                }
                if (a == 3) {
                    holder.txt_ratingtext.setText("Ngon tuyệt");
                }
            }
            holder.txt_tiltereview.setText(list.get(position).getTitle());

        }else {
            if (list.get(position).getType() == 1) {
                holder.txt_tenmon.setText(list.get(position).getFood().getTenmon());
                holder.txt_gia.setText(list.get(position).getFood().getGia() + " đ");
            } else {
                holder.txt_tenmon.setText("");
                holder.txt_gia.setText("");
            }
            if (list.get(position).getHinh() != null)
                Picasso.with(activity)
                        .load(list.get(position).getHinh())
                        .into(holder.img_user);
            else
                holder.img_user.setImageBitmap(mBitmap);
            if(type==3) {
                holder.txt_tenquan.setText(list.get(position).getUserName());
                holder.txt_diachi.setText(list.get(position).getTitle());
            }else{
                holder.txt_tenquan.setText(list.get(position).getLocaName());
                holder.txt_diachi.setText(list.get(position).getDiachi());
            }

        }
        holder.txt_postdate.setText(list.get(position).getDate());
        holder.txt_time.setText(list.get(position).getTime());
        holder.txt_un.setText(list.get(position).getUserName());
        holder.txt_likenumb.setText(String.valueOf(list.get(position).getLikeCount()) + " Likes");
        holder.txt_commentnumb.setText(String.valueOf(list.get(position).getCommentCount()) + " Comments");

        //MyService.setFinish(true);
        if (position > previuosPosition) {
            AnimationUtils.animateItemRcylerV(holder, true);

        } else {
            AnimationUtils.animateItemRcylerV(holder, false);

        }

        previuosPosition = position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
