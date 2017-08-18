package io.condividifacile;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class GroupActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //this is the main activity for the project, must contain the sliding window and the group page
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private ExpandOrCollapse mAnimationManager;
    private ArrayList<Integer> colors;
    private PieChart pieChart;
    //test data
    private String selectedGroup;
    private String email;
    private String name;
    private String uid;
    private Uri photoUrl;
    private ArrayList<String> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        groups = new ArrayList<>();
        pieChart = (PieChart) findViewById(R.id.piechart);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //navigation menu settings
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0);
        final Menu navMenu = navView.getMenu();
        final SubMenu groupsMenu = navMenu.addSubMenu("Groups");
        final TextView nameView = (TextView) header.findViewById(R.id.nameView);
        final TextView emailView = (TextView) header.findViewById(R.id.emailView);
        final ImageView userImage = (ImageView) header.findViewById(R.id.userImageView);


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
            photoUrl = currentUser.getPhotoUrl();
            userImage.setImageURI(photoUrl);
            DatabaseReference groupsRef = database.getReference("users/" + uid + "/groups");
            groupsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String group = singleSnapshot.getKey();
                        groups.add(group);
                        groupsMenu.add(Menu.NONE,groups.indexOf(group),Menu.NONE,group);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //test data
        ArrayList<PieEntry>entries = new ArrayList<>();
        entries.add(new PieEntry(100,"Alimenti"));
        entries.add(new PieEntry(40,"Bollette"));
        entries.add(new PieEntry(25,"Internet"));
        updateChart(entries);

        //From here on is the expandableLayout on the bottom
        final FloatingActionButton expand_btn = (FloatingActionButton) findViewById(R.id.expandbtn);
        final RelativeLayout expandableLayout = (RelativeLayout) findViewById(R.id.expandableLayout);
        final TextView testView = (TextView) findViewById(R.id.textView);
        final boolean[] isExpanded = {false};
        testView.setText("BALANCE: 12€");
        mAnimationManager = new ExpandOrCollapse();

        expand_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isExpanded[0]) {
                    mAnimationManager.expand(expandableLayout, 1000, 450);
                    isExpanded[0] = true;
                    expand_btn.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }else {
                    mAnimationManager.collapse(expandableLayout, 1000, 200);
                    isExpanded[0] = false;
                    expand_btn.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                }
            }
        });

        final FloatingActionButton addExpBtn = (FloatingActionButton) findViewById(R.id.addExp);
        addExpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addExpenseIntent = new Intent(GroupActivity.this,AddExpenseActivity.class);
                //addExpenseIntent.putExtra("group",groupName);
                startActivity(addExpenseIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        RelativeLayout revealLayout = (RelativeLayout) findViewById(R.id.transitionLayout);
        revealLayout.setVisibility(View.INVISIBLE);
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

        } else {
            selectedGroup = groups.get(id);
            ArrayList <Expense> exps = getGroupExpenses(selectedGroup);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //method to get group expenses from DB
    private ArrayList <Expense> getGroupExpenses(String groupName){

        final ArrayList <Expense> expenses = new ArrayList<>();
        DatabaseReference expRef = database.getReference("groups/"+groupName+"/expenses");
        expRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    i++;
                    Expense exp = new Expense();
                    exp.setId(i);
                    long amount = (long) singleSnapshot.child("amount").getValue();
                    exp.setAmount(amount);
                    String buyer = (String) singleSnapshot.child("buyer").getValue();
                    exp.setBuyer(buyer);
                    String category = (String) singleSnapshot.child("category").getValue();
                    exp.setCategory(category);
                    //Missing date, description and photo on DB
                    expenses.add(exp);
                }
                ArrayList <PieEntry> entries = new ArrayList<>();
                for(int j = 0; j < expenses.size(); j++){
                    entries.add(new PieEntry(expenses.get(j).getAmount(),expenses.get(j).getCategory()));
                }
                updateChart(entries);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return expenses;
    }


    public void updateChart(final ArrayList<PieEntry> entries){

        final RelativeLayout revealLayout = (RelativeLayout) findViewById(R.id.transitionLayout);
        revealLayout.setVisibility(View.INVISIBLE);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final float maxRadius = Math.max(dm.heightPixels, dm.widthPixels);


        //setting chart
        pieChart.setUsePercentValues(true);
        Description dscr = new Description();
        dscr.setText("Spese recenti");
        pieChart.setDescription(dscr);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(10);
        pieChart.setTransparentCircleRadius(10);
        pieChart.setHoleColor(Color.TRANSPARENT);

        //chart value selected listener

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            public void onValueSelected(Entry e, Highlight h) {
                //display message on value selected
                if(e==null){
                    return;
                }

                PieEntry pieE = (PieEntry) e;
                int clickedIndex = 0;
                for(int i = 0; i < entries.size(); i++){
                    if(entries.get(i).equals(pieE.getLabel())){
                        clickedIndex = i;
                    }
                }

                final Intent detailsIntent = new Intent(GroupActivity.this, DetailsActivity.class);
                detailsIntent.putExtra("categoria",pieE.getLabel());
                detailsIntent.putExtra("totale",pieE.getValue());
                detailsIntent.putExtra("color",colors.get(clickedIndex));
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



        PieDataSet dataSet = new PieDataSet(entries,"Categorie spese");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        //Customizing legend
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        l.setXEntrySpace(7);
        l.setYEntrySpace(5);

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
        dataSet.setColors(colors);

        //instantiate pieData here
        PieData data = new PieData();
        data.setDataSet(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextColor(Color.GRAY);
        data.setValueTextSize(11f);

        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }

}
