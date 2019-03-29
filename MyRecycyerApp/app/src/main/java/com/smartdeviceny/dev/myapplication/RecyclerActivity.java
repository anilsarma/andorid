package com.smartdeviceny.dev.myapplication;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class RecyclerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
     //  setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_rv);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerAdapter adapter = new RecyclerAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        setData(adapter);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.addItemDecoration(new StickHeaderItemDecoration(adapter));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setData(RecyclerAdapter adapter) {
        HeaderDataImpl headerData1 = new HeaderDataImpl(HeaderDataImpl.HEADER_TYPE_1, R.layout.header1_item_recycler);
        HeaderDataImpl headerData2 = new HeaderDataImpl(HeaderDataImpl.HEADER_TYPE_2, R.layout.header2_item_recycler);

        List<CustomerData> items = new ArrayList<>();
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        adapter.setHeaderAndData(items, headerData1);


        items = new ArrayList<>();
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        adapter.setHeaderAndData(items, headerData2);

        items = new ArrayList<>();
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        adapter.setHeaderAndData(items, headerData1);

        items = new ArrayList<>();
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        adapter.setHeaderAndData(items, headerData2);

        items = new ArrayList<>();
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        adapter.setHeaderAndData(items, headerData1);

        items = new ArrayList<>();
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        adapter.setHeaderAndData(items, headerData2);

        items = new ArrayList<>();
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        items.add(new CustomerData());
        adapter.setHeaderAndData(items, headerData1);
    }
}
