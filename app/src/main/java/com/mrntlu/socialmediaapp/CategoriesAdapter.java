package com.mrntlu.socialmediaapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder>{

    private Activity activity;
    Dialog customDialog;
    String category;
    ProgressBar progressBar;

    ArrayList<Uri> thumbLinks=new ArrayList<Uri>();
    ArrayList<Uri> imageLinks=new ArrayList<Uri>();
    ArrayList<Integer> imageID=new ArrayList<Integer>();

    public CategoriesAdapter(Activity activity, ArrayList<Uri> thumbLinks, ArrayList<Uri> imageLinks,ProgressBar progressBar,ArrayList<Integer> imageID) {
        this.activity = activity;
        this.thumbLinks = thumbLinks;
        this.imageLinks = imageLinks;
        this.progressBar=progressBar;
        this.imageID=imageID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(activity).inflate(R.layout.categories_custom,parent,false);
        ViewHolder viewHolder=new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        RequestOptions requestOptions=new RequestOptions();
        //requestOptions.placeholder(R.drawable.loading_process).centerInside();
        requestOptions.error(R.drawable.ic_sync_problem_black_24dp).centerInside();

        holder.uploadedImageProgressBar.setVisibility(View.VISIBLE);
        Glide.with(activity).setDefaultRequestOptions(requestOptions).load(thumbLinks.get(position)).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.uploadedImageProgressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.uploadedImageProgressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(holder.uploadedImage);
        progressBar.setVisibility(View.GONE);
        holder.uploadedImageProgressBar.setVisibility(View.GONE);

        holder.cardviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog=new Dialog(view.getContext(),android.R.style.Theme_Black_NoTitleBar);
                showPopup(view,imageLinks.get(position).toString(),imageID.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return thumbLinks.size();
    }

    void saveImage(Drawable drawable, String imageName){
        try {
            Bitmap image=((BitmapDrawable) drawable).getBitmap();
            Toasty.info(activity, "Started to Save", Toast.LENGTH_SHORT).show();

            File path= Environment.getExternalStorageDirectory();

            File dir=new File(path+"/Download/");
            dir.mkdir();

            File file=new File(dir,imageName);

            OutputStream out=new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG,100,out);
            out.flush();
            out.close();
            Toasty.success(activity, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch (NullPointerException e){
            Toasty.error(activity,"Wait for image to load!",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
            Toasty.error(activity, "Error! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void shareDrawable(Context context, String imageURL, String fileName) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
            i.putExtra(Intent.EXTRA_TEXT, imageURL);
            activity.startActivity(Intent.createChooser(i, "Share URL"));
        }
        catch (Exception e) {
            Toasty.error(context, "Error! "+e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    void showPopup(View v, final String imageURL, final int id){
        ImageButton backButton,downloadButton,shareButton;

        customDialog.setContentView(R.layout.image_dialog);
        backButton=(ImageButton)customDialog.findViewById(R.id.backButton);
        ImageView uploadedImage=(ImageView)customDialog.findViewById(R.id.uploadedImage);
        downloadButton=(ImageButton)customDialog.findViewById(R.id.downloadButton);
        shareButton=(ImageButton)customDialog.findViewById(R.id.shareButton);

        final ProgressBar imageLoadProgress=(ProgressBar)customDialog.findViewById(R.id.imageLoadProgress);
        final ImageView finalUploaded=uploadedImage;

        imageLoadProgress.setVisibility(View.VISIBLE);
        Glide.with(v.getContext()).load(imageURL).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                imageLoadProgress.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                imageLoadProgress.setVisibility(View.GONE);
                return false;
            }
        }).into(uploadedImage);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage(finalUploaded.getDrawable(),id+".jpg");
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDrawable(activity,imageURL,"filename");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.dismiss();
            }
        });
        customDialog.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView uploadedImage;
        CardView cardviewLayout;
        ProgressBar uploadedImageProgressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            uploadedImage=(ImageView)itemView.findViewById(R.id.uploadedImage);
            cardviewLayout=(CardView)itemView.findViewById(R.id.cardview_layout);
            uploadedImageProgressBar=(ProgressBar)itemView.findViewById(R.id.uploadedImageProgressBar);
        }
    }
}
