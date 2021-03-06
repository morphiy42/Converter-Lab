package com.example.myapplication3.app.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import android.widget.Toast;

import com.example.myapplication3.app.Constants;
import com.example.myapplication3.app.R;
import com.example.myapplication3.app.adapters.OrganizationRecyclerAdapter;
import com.example.myapplication3.app.models.GlobalModel;
import com.example.myapplication3.app.models.Organization;
import com.example.myapplication3.app.service.UpdatingService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sasha on 22.09.2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RecyclerViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
    private GlobalModel mGlobalModel;
    private OnFragmentInteractionListener mListener;
    private BroadcastReceiver mBroadcastReceiver;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        findViews(rootView);

        if (mGlobalModel == null) {
            Bundle bundle = getArguments();
            mGlobalModel = bundle.getParcelable(Constants.TAG_GLOBAL_MODEL);
        }
        if (!mGlobalModel.isDateIsNull()) {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        setModelInRecyclerView(mGlobalModel);

        setHasOptionsMenu(true);

        return rootView;
    }

    private void findViews(View rootView) {
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_FRV);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_RVF);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(R.color.blue,
                R.color.color_of_bank_name,
                R.color.color_green_up,
                R.color.black_semi_transparent);
    }

    public void setModelInRecyclerView(final GlobalModel globalModel) {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter = new OrganizationRecyclerAdapter(globalModel);
        mAdapter.notifyDataSetChanged();

        mRecyclerView.setAdapter(mAdapter);
        ((OrganizationRecyclerAdapter) mAdapter).setOnItemClickListener(new OrganizationRecyclerAdapter.MyClickListener() {

            @Override
            public void onItemClick(int position, View view) {

                switch (view.getId()) {
                    case R.id.iv_link_RI:
                        goToLink(position);
                        break;
                    case R.id.iv_map_RI:
                        mListener.goMapsFragment(globalModel, position);
                        break;
                    case R.id.iv_phone_RI:
                        callToPhone(position);
                        break;
                    case R.id.iv_next_RI:
                        mListener.goDetailFragment(globalModel, position);
                        break;
                }
            }
        });
    }

    private void callToPhone(int position) {

        List<Organization> organizationList = mGlobalModel.getOrganizations();
        Organization organization = organizationList.get(position);
        String phone = organization.getPhone();
        String uri = "tel:" + phone.trim();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    private void goToLink(int position) {

        List<Organization> organizationList = mGlobalModel.getOrganizations();
        Organization organization = organizationList.get(position);
        String link = organization.getLink();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }

    @Override
    public void onRefresh() {

        mSwipeRefreshLayout.setRefreshing(true);
        Intent intent = new Intent(getActivity(), UpdatingService.class);
        getActivity().startService(intent);
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.error_load, Toast.LENGTH_SHORT).show();
                }
            }
        }, 40000);
    }

    public interface OnFragmentInteractionListener {
        public void goDetailFragment(GlobalModel globalModel, int position);
        public void goMapsFragment(GlobalModel globalModel, int position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recycler_view_fragment, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search_menu);
        SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        }
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                setModelInRecyclerView(mGlobalModel);
                hideSoftKeyboard();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              public boolean onQueryTextChange(String text) {
                                                  getRezOfSearch(text);
                                                  return false;
                                              }

                                              public boolean onQueryTextSubmit(String text) {
                                                  return false;
                                              }
                                          }
        );
    }

    private void getRezOfSearch(String searchText) {
        GlobalModel rezOfSearch = new GlobalModel(mGlobalModel.getSourceId(), mGlobalModel.getDate(), mGlobalModel.getOrganizations(),
                mGlobalModel.getOrgTypes(), mGlobalModel.getCurrenciesReal(), mGlobalModel.getRegionsReal(), mGlobalModel.getCitiesReal());

        List<Organization> organizations = new ArrayList<Organization>();
        for (Organization organization : mGlobalModel.getOrganizations()) {
            String city = Constants.getRealName(mGlobalModel.getCitiesReal(), organization.getCityId());
            String region = Constants.getRealName(mGlobalModel.getRegionsReal(), organization.getRegionId());

            if (organization.getTitle().toLowerCase().contains(searchText) || organization.getAddress().toLowerCase().contains(searchText)
                    || city.toLowerCase().contains(searchText) || region.toLowerCase().contains(searchText)) {
                organizations.add(organization);
            }
        }
        rezOfSearch.setOrganizations(organizations);
        setModelInRecyclerView(rezOfSearch);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        regBroadcastReceiver();
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void regBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle bundle = intent.getBundleExtra(Constants.TAG_GLOBAL_MODEL);

                mGlobalModel = bundle.getParcelable(Constants.TAG_GLOBAL_MODEL);
                if (!(mSwipeRefreshLayout == null)) {
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        Toast.makeText(getActivity(), getString(R.string.DB_is_update), Toast.LENGTH_SHORT).show();
                    }

                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);}
                    Log.d("qqq", "mBroadcastReceiver");
                    setModelInRecyclerView(mGlobalModel);

            }
        };
        IntentFilter intentFilter = new IntentFilter(Constants.TAG_BROADCAST_ACTION);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getActivity().getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }
}

