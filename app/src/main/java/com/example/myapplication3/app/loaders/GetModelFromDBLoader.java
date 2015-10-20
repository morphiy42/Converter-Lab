package com.example.myapplication3.app.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import com.example.myapplication3.app.Constants;
import com.example.myapplication3.app.DB.DBWorker;
import com.example.myapplication3.app.models.GlobalModel;

/**
 * Created by sasha on 14.10.2015.
 */
public class GetModelFromDBLoader extends AsyncTaskLoader<GlobalModel> {
    private Context context;

    public GetModelFromDBLoader(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public GlobalModel loadInBackground() {
        Log.d(Constants.TAG_LOG, "work LOADER");

        return DBWorker.getInstance(context).getGlobalModelFromDB();
    }


}