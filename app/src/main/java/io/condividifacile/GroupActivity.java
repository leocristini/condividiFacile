package io.condividifacile;

import android.animation.Animator;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //this is the main activity for the project, must contain the sliding window and the group page
    public static final int RSS_DOWNLOAD_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private ExpandOrCollapse mAnimationManager;
    private ArrayList<Integer> colors;
    private PieChart pieChart;
    private String selectedGroup;
    private ArrayList <Expense> expenses;
    private View header;
    private String email;
    private String name;
    private String uid;
    private String photoUrl;
    private ArrayList<String> groups;
    private ArrayList<Pair<String,String>> members;
    private ArrayList<Pair<String, Double>> userBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        groups = new ArrayList<>();
        members = new ArrayList<>();
        pieChart = (PieChart) findViewById(R.id.piechart);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //navigation menu settings
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        header = navView.getHeaderView(0);
        final Menu navMenu = navView.getMenu();
        final TextView nameView = (TextView) header.findViewById(R.id.nameView);
        final TextView emailView = (TextView) header.findViewById(R.id.emailView);


        mAuth = FirebaseAuth.getInstance();

        //Getting user data and groups
        database = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            name = currentUser.getDisplayName();
            nameView.setText(name);
            email = currentUser.getEmail();
            emailView.setText(email);
            uid = currentUser.getUid();
            photoUrl = currentUser.getPhotoUrl().toString();
            if(photoUrl != null) {
                PendingIntent pendingResult = createPendingResult(
                        RSS_DOWNLOAD_REQUEST_CODE, new Intent(), 0);
                Intent intent = new Intent(getApplicationContext(), DownloadIntentService.class);
                intent.putExtra(DownloadIntentService.URL_EXTRA, photoUrl);
                intent.putExtra(DownloadIntentService.PENDING_RESULT_EXTRA, pendingResult);
                startService(intent);
            }
            DatabaseReference groupsRef = database.getReference("users/" + uid + "/groups");
            groupsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String group = singleSnapshot.getKey();
                        groups.add(group);
                        navMenu.add(R.id.groups_menu,groups.indexOf(group),Menu.NONE,group);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //From here on is the expandableLayout on the bottom
        final FloatingActionButton expand_btn = (FloatingActionButton) findViewById(R.id.expandbtn);
        final RelativeLayout expandableLayout = (RelativeLayout) findViewById(R.id.expandableLayout);
        final boolean[] isExpanded = {false};
        mAnimationManager = new ExpandOrCollapse();

        expand_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isExpanded[0]) {
                    mAnimationManager.expand(expandableLayout, 500, 450);
                    isExpanded[0] = true;
                    expand_btn.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                    if(selectedGroup != null){
                        detailsBalance();
                    }
                }else {
                    mAnimationManager.collapse(expandableLayout, 500, 200);
                    isExpanded[0] = false;
                    expand_btn.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    if(selectedGroup != null){
                        shortBalance();
                    }
                }
            }
        });

        expandableLayout.setOnTouchListener(new View.OnTouchListener() {

            float X,Y;
            private static final int SLIDE_THRESHOLD = 50;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){

                    case MotionEvent.ACTION_DOWN:
                        X = event.getRawX();
                        Y = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        X = X + event.getX();
                        Y = Y + event.getY();
                        break;

                    case MotionEvent.ACTION_UP:

                        if(Math.abs(Y) > Math.abs(X)){
                            if(Math.abs(Y) > SLIDE_THRESHOLD){
                                if(Y > 0){
                                    //Slide down
                                    if(isExpanded[0]){
                                        mAnimationManager.collapse(expandableLayout, 500, 200);
                                        isExpanded[0] = false;
                                        expand_btn.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                                        if(selectedGroup != null){
                                            shortBalance();
                                        }
                                    }
                                }else{
                                    if (!isExpanded[0]) {
                                        mAnimationManager.expand(expandableLayout, 500, 450);
                                        isExpanded[0] = true;
                                        expand_btn.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                                        if(selectedGroup != null){
                                            detailsBalance();
                                        }
                                    }
                                }
                            }
                        }

                        break;
                }
                return true;
            }
        });

        final FloatingActionButton addExpBtn = (FloatingActionButton) findViewById(R.id.addExp);
        addExpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addExpenseIntent = new Intent(GroupActivity.this,AddExpenseActivity.class);
                addExpenseIntent.putExtra("selectedGroup",selectedGroup);
                startActivity(addExpenseIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        RelativeLayout revealLayout = (RelativeLayout) findViewById(R.id.transitionLayout);
        revealLayout.setVisibility(View.INVISIBLE);
        try {
            getGroupExpenses(selectedGroup);
            getUserBalance(currentUser.getUid(),selectedGroup);
        }catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.add_group){

            Intent i = new Intent(GroupActivity.this, AddGroupActivity.class);
            startActivity(i);

        } else {
            selectedGroup = groups.get(id);
            getGroupExpenses(selectedGroup);
            getUserBalance(uid,selectedGroup);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //method to get user balance inside a group from DB
    private void getUserBalance(String userId, String groupName){

        DatabaseReference balanceRef = database.getReference("users/"+userId+"/groups/"+groupName);
        balanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userBalance = new ArrayList<>();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String member = singleSnapshot.getKey();
                    double balance = Double.parseDouble(singleSnapshot.getValue().toString());
                    Pair<String, Double> memberBalance = new Pair<String, Double>(member,balance);
                    userBalance.add(memberBalance);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        shortBalance();
    }

    //method to hide user balance details
    private void shortBalance(){

        double balanceSum = 0;
        for(int i = 0; i < userBalance.size(); i++){
            balanceSum = balanceSum + userBalance.get(i).second;
        }
        balanceSum = Math.round(balanceSum*100)/100;
        final RelativeLayout expandableLayout = (RelativeLayout) findViewById(R.id.expandableLayout);
        TableLayout table = (TableLayout) expandableLayout.findViewById(R.id.balanceTable);
        table.setVisibility(View.INVISIBLE);
        TextView totalBalance = (TextView) findViewById(R.id.totalBalance);
        totalBalance.setVisibility(View.VISIBLE);
        totalBalance.setTypeface(null, Typeface.BOLD);
        totalBalance.setText("Total balance: "+balanceSum);

    }

    //method to show user balance details
    private void detailsBalance(){

        final RelativeLayout expandableLayout = (RelativeLayout) findViewById(R.id.expandableLayout);
        TextView totalBalance = (TextView) findViewById(R.id.totalBalance);
        totalBalance.setVisibility(View.INVISIBLE);
        final TableLayout balanceTable = (TableLayout) expandableLayout.findViewById(R.id.balanceTable);
        balanceTable.removeAllViews();
        balanceTable.setVisibility(View.VISIBLE);

        for(int i = 0; i < userBalance.size(); i++){
            final TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.balance_row_item, null);
            TextView member = (TextView) row.findViewById(R.id.member);
            member.setText(userBalance.get(i).first);
            TextView balance = (TextView) row.findViewById(R.id.balance);
            balance.setText(userBalance.get(i).second.toString());


            final Button settleBtn = (Button) row.findViewById(R.id.settleBtn);
            settleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String member = ((TextView) row.findViewById(R.id.member)).getText().toString();
                    DatabaseReference userRef = database.getReference("users");
                    userRef.child(currentUser.getUid()).child("groups").child(selectedGroup).child(member).setValue(0);
                    String memberId = null;
                    for(int k = 0; k < members.size(); k++){
                        if(member.equalsIgnoreCase(members.get(k).first)){
                            memberId = members.get(k).second;
                        }
                    }
                    userRef.child(memberId).child("groups").child(selectedGroup).child(currentUser.getDisplayName()).setValue(0);
                    detailsBalance();
                }
            });

            balanceTable.addView(row);
            registerForContextMenu(row);
        }

    }

    //method to get group expenses from DB
    private void getGroupExpenses(final String groupName){

        expenses = new ArrayList<>();
        DatabaseReference expRef = database.getReference("groups");
        expRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int categoriesCount = 0;
                ArrayList <String> categories = new ArrayList<String>();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    //if (singleSnapshot.child("name").getValue().equals(groupName)){
                     if(singleSnapshot.getKey().equals(groupName)){
                        for(DataSnapshot category : singleSnapshot.child("categories").getChildren()){
                            categories.add(category.getKey());
                        }
                        for(DataSnapshot expense : singleSnapshot.child("expenses").getChildren()){
                            Expense exp = new Expense();
                            long amount = (long) expense.child("amount").getValue();
                            exp.setAmount(amount);
                            String buyer = (String) expense.child("buyer").getValue();
                            exp.setBuyer(buyer);
                            String category = (String) expense.child("category").getValue();
                            exp.setCategory(category);
                            String date = (String) expense.child("date").getValue();
                            exp.setDate(date);
                            ArrayList<HashMap<String,Double>> divisionList = (ArrayList<HashMap<String,Double>>) expense.child("division").getValue();
                            ArrayList <Pair<String,Double>> formattedDivision = new ArrayList<Pair<String, Double>>();
                            if(divisionList != null) {
                                Log.d("swag", expense.child("division").getValue().toString());
                                exp.setDivision(divisionList);
                            }

                            //Missing date, description and photo on DB
                            expenses.add(exp);
                        }
                         for(DataSnapshot member :  singleSnapshot.child("members").getChildren()){
                             members.add(new Pair<String, String>(member.getKey(),member.getValue().toString()));
                         }
                    }


                }
                ArrayList <PieEntry> entries = new ArrayList<>(categoriesCount);
                for(int j = 0; j < categories.size(); j++){
                    float sum = 0;
                    for (int i = 0; i < expenses.size(); i++){
                        if(categories.get(j).equals(expenses.get(i).getCategory())){
                            sum = sum+expenses.get(i).getAmount();
                        }
                    }
                    PieEntry e = new PieEntry(sum,categories.get(j));
                    entries.add(e);
                }
                updateChart(entries);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void updateChart(final ArrayList<PieEntry> entries) {

        final RelativeLayout revealLayout = (RelativeLayout) findViewById(R.id.transitionLayout);
        revealLayout.setVisibility(View.INVISIBLE);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final float maxRadius = Math.max(dm.heightPixels, dm.widthPixels);

        if (entries.size() != 0) {
            //setting chart
            pieChart.setUsePercentValues(true);
            Description dscr = new Description();
            dscr.setText("Spese recenti");
            pieChart.setDescription(dscr);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(10);
            pieChart.setTransparentCircleRadius(10);
            pieChart.setHoleColor(Color.TRANSPARENT);

            //Setting colors to chart
            colors = new ArrayList<>();

            for (int c : ColorTemplate.VORDIPLOM_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.JOYFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.COLORFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.LIBERTY_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());

            //chart value selected listener
            pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

                public void onValueSelected(Entry e, Highlight h) {
                    //display message on value selected
                    if (e == null) {
                        return;
                    }

                    PieEntry pieE = (PieEntry) e;
                    int clickedIndex = 0;
                    for (int i = 0; i < entries.size(); i++) {
                        if (entries.get(i).getLabel().equals(pieE.getLabel())) {
                            clickedIndex = i;
                        }
                    }

                    final Intent detailsIntent = new Intent(GroupActivity.this, DetailsActivity.class);
                    detailsIntent.putExtra("categoria", pieE.getLabel());
                    detailsIntent.putExtra("color", colors.get(clickedIndex));
                    detailsIntent.putExtra("expenses", expenses);

                    //animation
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        revealLayout.setBackgroundColor(colors.get(clickedIndex));
                        revealLayout.setVisibility(View.VISIBLE);
                        Animator circularReveal = ViewAnimationUtils.createCircularReveal(revealLayout, (int) h.getXPx(), (int) h.getYPx(), 0, maxRadius);

                        circularReveal.setDuration(600);

                        circularReveal.start();

                        circularReveal.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                startActivity(detailsIntent);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }


                }

                @Override
                public void onNothingSelected() {

                }
            });


            PieDataSet dataSet = new PieDataSet(entries, "Categorie spese");
            dataSet.setSliceSpace(3);
            dataSet.setSelectionShift(5);
            dataSet.setColors(colors);

            //Customizing legend
            Legend l = pieChart.getLegend();
            l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
            l.setXEntrySpace(7);
            l.setYEntrySpace(5);


            //instantiate pieData here
            PieData data = new PieData();
            data.setDataSet(dataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(11f);

            pieChart.setData(data);
            pieChart.highlightValues(null);
            pieChart.invalidate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RSS_DOWNLOAD_REQUEST_CODE) {
            switch (resultCode) {
                case DownloadIntentService.INVALID_URL_CODE:
                    Log.e("Error","Invalid URL");
                    break;
                case DownloadIntentService.ERROR_CODE:
                    Log.e("Error","Error downloading data");
                    break;
                case DownloadIntentService.RESULT_CODE:
                    ImageView userImage = (ImageView) header.findViewById(R.id.userImageView);
                    Bitmap bm = data.getParcelableExtra("url");
                    Bitmap circleBitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);

                    BitmapShader shader = new BitmapShader (bm,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Paint paint = new Paint();
                    paint.setShader(shader);
                    paint.setAntiAlias(true);
                    Canvas c = new Canvas(circleBitmap);
                    c.drawCircle(bm.getWidth()/2, bm.getHeight()/2, bm.getWidth()/2, paint);
                    userImage.setImageBitmap(circleBitmap);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
