package id.technomotion.ui.recentconversation;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.remote.QiscusApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import id.technomotion.ui.homepagetab.RecentConversationFragmentRecyclerAdapter;
import id.technomotion.ui.login.LoginActivity;
import id.technomotion.ui.privatechatcreation.PrivateChatCreationActivity;
import id.technomotion.R;
import id.technomotion.model.Room;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RecentConversationsActivity extends AppCompatActivity {
    private static final String TAG = "RecentConversationsActi";
    private FloatingActionButton fabCreateNewConversation;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Room> rooms = new ArrayList<>();
    private RecentConversationFragmentRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Qiscus.hasSetupUser()) {
            startActivity(new Intent(RecentConversationsActivity.this, LoginActivity.class));
        } else {
            setContentView(R.layout.activity_recent_conversations);

            getSupportActionBar().setTitle("Recent conversation");

            recyclerView = (RecyclerView) findViewById(R.id.recyclerRecentConversation);
            linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);

            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

            fabCreateNewConversation = (FloatingActionButton) findViewById(R.id.buttonCreateNewConversation);
            fabCreateNewConversation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(RecentConversationsActivity.this, PrivateChatCreationActivity.class);
                    startActivity(intent);
                }
            });

            adapter = new RecentConversationFragmentRecyclerAdapter(rooms);
            recyclerView.setAdapter(adapter);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    reloadRecentConversation();
                }
            });

            reloadRecentConversation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Qiscus.hasSetupUser()) {
            startActivity(new Intent(RecentConversationsActivity.this, LoginActivity.class));
        } else {
            Log.d(TAG, "onResume: ");
            reloadRecentConversation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //gotoContact
        switch (item.getItemId()) {
            case R.id.logout_menu:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        Qiscus.clearUser();
        startActivity(new Intent(RecentConversationsActivity.this, LoginActivity.class));
    }

    public void reloadRecentConversation() {

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        QiscusApi.getInstance().getChatRooms(1, 20, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<QiscusChatRoom>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<QiscusChatRoom> qiscusChatRooms) {
                        Log.d(TAG, "onNext: size" + qiscusChatRooms.size());
                        rooms.clear();
                        for (int i = 0; i < qiscusChatRooms.size(); i++) {
                            QiscusChatRoom currentChatRoom = qiscusChatRooms.get(i);
                            Room room = new Room(currentChatRoom.getId(), qiscusChatRooms.get(i).getName());
                            room.setLatestConversation(currentChatRoom.getLastComment().getMessage());
                            room.setOnlineImage(currentChatRoom.getAvatarUrl());
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            SimpleDateFormat dateFormatToday = new SimpleDateFormat("hh:mm a");
                            Date messageDate = currentChatRoom.getLastComment().getTime();
                            String finalDateFormat = "";
                            if (DateUtils.isToday(messageDate.getTime())) {
                                finalDateFormat = dateFormatToday.format(currentChatRoom.getLastComment().getTime());
                            }
                            else {
                                finalDateFormat = dateFormat.format(currentChatRoom.getLastComment().getTime());
                            }
                            room.setLastMessageTime(finalDateFormat);
                            room.setUnreadCounter(currentChatRoom.getUnreadCount());
                            rooms.add(room);
                            /*if (!rooms.contains(room)) {
                                rooms.add(room);
                            }
                            else {
                                rooms.set(rooms.indexOf(room),room);
                            }*/
                        }
                    }
                });
    }
}
