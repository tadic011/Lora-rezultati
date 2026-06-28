package rs.imlek.lorascore;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Typeface;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final String[] games = {"Plus", "Minus", "Pop Herc", "Dame", "Herčevi", "Lora", "Igra po želji"};
    private final EditText[] nameFields = new EditText[4];
    private final EditText[] scoreFields = new EditText[4];
    private final TextView[] totalViews = new TextView[4];
    private LinearLayout root;
    private int round = 0, gameIndex = 0;
    private int[][][] scores = new int[4][7][4];
    private String[] names = {"Igrač 1", "Igrač 2", "Igrač 3", "Igrač 4"};
    private SharedPreferences prefs;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("lora_score", MODE_PRIVATE);
        load();
        showStart();
    }

    private void base() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 36);
        root.setBackgroundColor(0xff121212);
        scroll.addView(root);
        setContentView(scroll);
    }

    private TextView text(String s, int sp, int style) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(0xffffffff);
        v.setTextSize(sp);
        v.setTypeface(Typeface.DEFAULT, style);
        v.setPadding(0, 10, 0, 10);
        return v;
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setPadding(12, 14, 12, 14);
        return b;
    }

    private EditText input(String s) {
        EditText e = new EditText(this);
        e.setText(s);
        e.setTextColor(0xffffffff);
        e.setTextSize(18);
        e.setSingleLine(true);
        e.setSelectAllOnFocus(true);
        e.setPadding(16, 12, 16, 12);
        return e;
    }

    private void showStart() {
        base();
        root.addView(text("♠ Lora Score", 30, Typeface.BOLD));
        root.addView(text("Unesi imena igrača", 18, Typeface.NORMAL));
        for (int i=0;i<4;i++) {
            root.addView(text("Igrač " + (i+1), 14, Typeface.BOLD));
            nameFields[i] = input(names[i]);
            root.addView(nameFields[i]);
        }
        Button start = button("Počni partiju");
        start.setOnClickListener(v -> { for(int i=0;i<4;i++) names[i]=nameFields[i].getText().toString().trim().isEmpty()?"Igrač "+(i+1):nameFields[i].getText().toString().trim(); save(); showGame(); });
        root.addView(start);
        Button reset = button("Obriši sve");
        reset.setOnClickListener(v -> confirmReset());
        root.addView(reset);
    }

    private void showGame() {
        base();
        root.addView(text("Krug " + (round+1) + " / 4", 18, Typeface.BOLD));
        root.addView(text(games[gameIndex], 30, Typeface.BOLD));
        root.addView(text("Unesi poene. Pobednik je igrač sa najmanje poena.", 14, Typeface.NORMAL));
        for (int i=0;i<4;i++) {
            root.addView(text(names[i], 16, Typeface.BOLD));
            scoreFields[i] = input(String.valueOf(scores[round][gameIndex][i]));
            scoreFields[i].setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
            final int idx=i;
            scoreFields[i].setOnFocusChangeListener((v,has)-> { if(!has) readScores(); });
            root.addView(scoreFields[i]);
        }
        Button next = button(isLast() ? "Završi partiju" : "Sledeća igra");
        next.setOnClickListener(v -> { readScores(); save(); if(isLast()) showResult(); else { advance(); showGame(); } });
        root.addView(next);
        Button back = button("Nazad");
        back.setOnClickListener(v -> { readScores(); save(); retreat(); showGame(); });
        root.addView(back);
        addTotals();
    }

    private void addTotals() {
        root.addView(text("Total score", 22, Typeface.BOLD));
        int[] t = totals();
        for(int i=0;i<4;i++) root.addView(text(names[i] + ": " + t[i], 18, Typeface.BOLD));
    }

    private void showResult() {
        base();
        int[] t = totals();
        Integer[] order = {0,1,2,3};
        Arrays.sort(order, (a,b) -> Integer.compare(t[a], t[b]));
        root.addView(text("🏆 Pobednik", 26, Typeface.BOLD));
        root.addView(text(names[order[0]] + " — " + t[order[0]], 30, Typeface.BOLD));
        root.addView(text("Konačan plasman", 20, Typeface.BOLD));
        for(int i=0;i<4;i++) {
            int p=order[i];
            root.addView(text((i+1) + ". " + names[p] + "   " + t[p], 20, Typeface.BOLD));
        }
        Button again = button("Nova partija");
        again.setOnClickListener(v -> { scores = new int[4][7][4]; round=0; gameIndex=0; save(); showGame(); });
        root.addView(again);
        Button namesBtn = button("Promeni imena");
        namesBtn.setOnClickListener(v -> showStart());
        root.addView(namesBtn);
    }

    private void readScores() {
        for(int i=0;i<4;i++) {
            try { scores[round][gameIndex][i] = Integer.parseInt(scoreFields[i].getText().toString().trim()); }
            catch(Exception e) { scores[round][gameIndex][i] = 0; }
        }
    }

    private int[] totals() {
        int[] t = new int[4];
        for(int r=0;r<4;r++) for(int g=0;g<7;g++) for(int p=0;p<4;p++) t[p]+=scores[r][g][p];
        return t;
    }
    private boolean isLast(){ return round==3 && gameIndex==6; }
    private void advance(){ gameIndex++; if(gameIndex==7){ gameIndex=0; round++; } }
    private void retreat(){ if(gameIndex>0) gameIndex--; else if(round>0){ round--; gameIndex=6; } }

    private void confirmReset(){ new AlertDialog.Builder(this).setTitle("Obriši sve?").setMessage("Brišu se svi rezultati.").setPositiveButton("Obriši",(d,w)->{prefs.edit().clear().apply(); scores=new int[4][7][4]; names=new String[]{"Igrač 1","Igrač 2","Igrač 3","Igrač 4"}; round=0; gameIndex=0; showStart();}).setNegativeButton("Odustani",null).show(); }

    private void save(){
        SharedPreferences.Editor e=prefs.edit();
        e.putInt("round",round); e.putInt("game",gameIndex);
        for(int i=0;i<4;i++) e.putString("name"+i,names[i]);
        for(int r=0;r<4;r++) for(int g=0;g<7;g++) for(int p=0;p<4;p++) e.putInt("s"+r+"_"+g+"_"+p, scores[r][g][p]);
        e.apply();
    }
    private void load(){
        round=prefs.getInt("round",0); gameIndex=prefs.getInt("game",0);
        for(int i=0;i<4;i++) names[i]=prefs.getString("name"+i,names[i]);
        for(int r=0;r<4;r++) for(int g=0;g<7;g++) for(int p=0;p<4;p++) scores[r][g][p]=prefs.getInt("s"+r+"_"+g+"_"+p,0);
    }
}
