package com.example.booklisting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {
    String keyword;
    BookAdapter adapter;
    RecyclerView bookList;
    View progressBar;
    TextView emptyView;
    int startIndex;
    LoaderManager loaderManager;
    LinearLayoutManager layoutManager;
    ArrayList<Book> books = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);


        //Get the keyword that user entered
        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!(adapter == null)) {
                    adapter.clear();
                }
                books.clear();
                startIndex = 0;
                emptyView = findViewById(R.id.empty_list_item);
                emptyView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                TextView searchView = findViewById(R.id.text_input);
                bookList = findViewById(R.id.book_list);
                loaderManager = getSupportLoaderManager();
                layoutManager = new LinearLayoutManager(MainActivity.this);
                bookList.setLayoutManager(layoutManager);


                keyword = searchView.getText().toString().trim();
                if (keyword.length() == 0) {
                    keyword = " ";
                }


                ConnectivityManager cm =
                        (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();


                if (isConnected) {
                    loaderManager.restartLoader(1, null, MainActivity.this);


                } else {

                    emptyView.setText(getResources().getString(R.string.no_internet_connection));
                }

                EndlessOnScrollListener scrollListener = new EndlessOnScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore() {
                        startIndex = startIndex + 40;
                        loadMore();
                    }
                };
                bookList.addOnScrollListener(scrollListener);
            }
        });
    }


    @NonNull
    @Override
    public Loader<List<Book>> onCreateLoader(int id, @Nullable Bundle args) {
        return new BookLoader(MainActivity.this, keyword, startIndex, books);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Book>> loader, List<Book> list) {


        emptyView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        this.books = (ArrayList<Book>) list;


        adapter = new BookAdapter(this, R.layout.list_item, (ArrayList<Book>) list);


        bookList.setAdapter(adapter);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);

        adapter.notifyItemRangeInserted(layoutManager.getItemCount(), books.size() - 1);
        layoutManager.scrollToPosition(startIndex);



        if (list.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(R.string.no_books_found);
        }
    }

    public void loadMore() {
        Log.i("MA loadMore: ", "Start Index = " + startIndex);
        loaderManager.restartLoader(1, null, MainActivity.this);
    }


    @Override
    public void onLoaderReset(@NonNull Loader<List<Book>> loader) {
        adapter.clear();
    }


}

