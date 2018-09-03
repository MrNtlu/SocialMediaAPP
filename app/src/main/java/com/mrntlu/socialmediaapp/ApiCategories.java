package com.mrntlu.socialmediaapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;


public class ApiCategories extends Fragment {

    //TODO SEARCH BY IMAGE https://wall.alphacoders.com/api2.0/get.php?auth=481cfa6f70112be63d18faaf10a597dd&method=search&term=boku+no+hero
    //todo https://wall.alphacoders.com/api.php#collapse_category_count
    //todo https://wall.alphacoders.com/api2.0/get.php?auth=481cfa6f70112be63d18faaf10a597dd&method=category_list
    //TODO https://android-arsenal.com/details/1/2850

    View v;
    XRecyclerView listView;
    ProgressBar listviewLoadProgress;
    CategoriesAdapter categoriesAdapter;
    Activity activity;
    int category;
    int page;

    private RequestQueue mQueue;
    private final String API_TOKEN="481cfa6f70112be63d18faaf10a597dd";

    ArrayList<Uri> thumbLinks=new ArrayList<Uri>();
    ArrayList<Uri> imageLinks=new ArrayList<Uri>();
    ArrayList<Integer> imageID=new ArrayList<Integer>();

    public ApiCategories() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public ApiCategories(Activity activity, int category) {
        this.activity = activity;
        this.category = category;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_api_categories, container, false);
        listView = (XRecyclerView) v.findViewById(R.id.recyvclerView);
        listviewLoadProgress=(ProgressBar)v.findViewById(R.id.listviewLoadProgress);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        listviewLoadProgress.setVisibility(View.VISIBLE);
        page=1;
        categoriesAdapter = new CategoriesAdapter(getActivity(),thumbLinks,imageLinks,listviewLoadProgress,imageID);

        final GridLayoutManager gridLayoutManager=new GridLayoutManager(activity,2);
        listView.setLayoutManager(gridLayoutManager);
        listView.setAdapter(categoriesAdapter);
        mQueue = Volley.newRequestQueue(activity);
        jsonParser(category,1);
        listView.setPullRefreshEnabled(false);
        listView.setLimitNumberToCallLoadMore(15);
        listView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                listView.refreshComplete();
            }

            @Override
            public void onLoadMore() {
                page++;
                jsonParser(category,page);
                listView.loadMoreComplete();
                if (page>=15){
                    listView.setLoadingMoreEnabled(false);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void jsonParser(int categories,int page){
        String url="https://wall.alphacoders.com/api2.0/get.php?auth="+API_TOKEN+"&method=category&id="+categories+"&page="+page+"&sort=rating";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                JSONArray jsonArray = response.getJSONArray("wallpapers");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    thumbLinks.add(Uri.parse(jsonArray.getJSONObject(i).getString("url_thumb")));
                                    imageLinks.add(Uri.parse(jsonArray.getJSONObject(i).getString("url_image")));
                                    imageID.add(jsonArray.getJSONObject(i).getInt("id"));
                                }
                                categoriesAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }
}