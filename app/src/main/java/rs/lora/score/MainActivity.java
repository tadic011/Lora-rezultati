package rs.lora.score;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.graphics.Typeface;import android.text.*;import android.view.*;import android.view.inputmethod.InputMethodManager;import android.widget.*;import java.util.*;

public class MainActivity extends Activity{
    final String[] games={"Plus","Minus","Pop Herc","Dame","Herčevi","Lora","Igra po želji"};
    EditText[] players=new EditText[4]; EditText[][] scores=new EditText[28][4]; TextView[] totals=new TextView[4]; TextView ranking; LinearLayout table; SharedPreferences sp; boolean loading=false;
    int bg=Color.rgb(17,24,39), panel=Color.rgb(31,41,55), card=Color.rgb(55,65,81), text=Color.WHITE, muted=Color.rgb(209,213,219), green=Color.rgb(34,197,94), red=Color.rgb(239,68,68);
    @Override public void onCreate(Bundle b){super.onCreate(b);sp=getSharedPreferences("lora",0);build();load();calc();}
    TextView tv(String s,int size,int style){TextView v=new TextView(this);v.setText(s);v.setTextColor(text);v.setTextSize(size);v.setTypeface(Typeface.DEFAULT,style);v.setPadding(10,8,10,8);return v;}
    EditText input(String hint){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(Color.rgb(156,163,175));e.setTextColor(text);e.setTextSize(16);e.setSingleLine(true);e.setSelectAllOnFocus(true);e.setBackgroundColor(card);e.setPadding(10,6,10,6);return e;}
    void build(){ScrollView sv=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(18,18,18,28);root.setBackgroundColor(bg);sv.addView(root);setContentView(sv);
        TextView title=tv("🂡 Lora Score",28,Typeface.BOLD);root.addView(title);TextView sub=tv("Pobednik je igrač sa najmanjim ukupnim brojem poena.",14,Typeface.NORMAL);sub.setTextColor(muted);root.addView(sub);
        LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);names.setPadding(0,12,0,12);root.addView(names);names.addView(tv("Igrači",18,Typeface.BOLD));
        for(int i=0;i<4;i++){players[i]=input("Igrač "+(i+1)); final int ix=i; players[i].addTextChangedListener(new SimpleWatcher(){public void afterTextChanged(Editable e){if(!loading){save();calc();}}}); names.addView(players[i],new LinearLayout.LayoutParams(-1,-2));}
        LinearLayout buttons=new LinearLayout(this);buttons.setOrientation(LinearLayout.HORIZONTAL);root.addView(buttons);buttons.addView(btn("Nova partija",v->newGame()),new LinearLayout.LayoutParams(0,-2,1));buttons.addView(btn("Obriši sve",v->confirmClear()),new LinearLayout.LayoutParams(0,-2,1));
        table=new LinearLayout(this);table.setOrientation(LinearLayout.VERTICAL);table.setPadding(0,14,0,14);root.addView(table);makeTable();
        TextView totalTitle=tv("Total Score",22,Typeface.BOLD);root.addView(totalTitle);LinearLayout totalBox=new LinearLayout(this);totalBox.setOrientation(LinearLayout.VERTICAL);totalBox.setBackgroundColor(panel);totalBox.setPadding(8,8,8,8);root.addView(totalBox);for(int i=0;i<4;i++){totals[i]=tv("",18,Typeface.BOLD);totalBox.addView(totals[i]);}
        ranking=tv("",18,Typeface.BOLD);ranking.setPadding(10,18,10,18);root.addView(ranking);
    }
    Button btn(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setBackgroundColor(green);b.setOnClickListener(l);return b;}
    void makeTable(){String[] headers={"Igra","1","2","3","4"};LinearLayout hr=row();for(String h:headers)hr.addView(cell(tv(h,14,Typeface.BOLD), h.equals("Igra")?2:1));table.addView(hr);
        for(int p=0;p<4;p++){TextView pt=tv("PARTIJA "+(p+1),17,Typeface.BOLD);pt.setTextColor(green);table.addView(pt);for(int g=0;g<7;g++){int r=p*7+g;LinearLayout line=row();line.addView(cell(tv(games[g],14,Typeface.BOLD),2));for(int c=0;c<4;c++){EditText e=input("0");e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);e.setTextSize(14);final int rr=r,cc=c;e.addTextChangedListener(new SimpleWatcher(){public void afterTextChanged(Editable x){if(!loading){saveScore(rr,cc,x.toString());calc();}}});scores[r][c]=e;line.addView(cell(e,1));}table.addView(line);}}
    }
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setPadding(0,2,0,2);return l;}
    LinearLayout.LayoutParams cell(View v,int weight){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,weight);lp.setMargins(2,2,2,2);v.setLayoutParams(lp);return lp;}
    void load(){loading=true;for(int i=0;i<4;i++)players[i].setText(sp.getString("p"+i,"Igrač "+(i+1)));for(int r=0;r<28;r++)for(int c=0;c<4;c++)scores[r][c].setText(sp.getString("s"+r+"_"+c,""));loading=false;}
    void save(){SharedPreferences.Editor e=sp.edit();for(int i=0;i<4;i++)e.putString("p"+i,players[i].getText().toString());e.apply();}
    void saveScore(int r,int c,String v){sp.edit().putString("s"+r+"_"+c,v).apply();}
    int val(String s){try{return Integer.parseInt(s.trim());}catch(Exception e){return 0;}}
    void calc(){int[] sum=new int[4];for(int r=0;r<28;r++)for(int c=0;c<4;c++)sum[c]+=val(scores[r][c].getText().toString());Integer[] order={0,1,2,3};Arrays.sort(order,(a,b)->Integer.compare(sum[a],sum[b]));for(int i=0;i<4;i++){String name=players[i].getText().toString().trim();if(name.isEmpty())name="Igrač "+(i+1);totals[i].setText(name+": "+sum[i]);totals[i].setTextColor(i==order[0]?green:text);}StringBuilder sb=new StringBuilder("🏆 Rang lista\n");for(int pos=0;pos<4;pos++){int i=order[pos];String medal=pos==0?"🥇 ":pos==1?"🥈 ":pos==2?"🥉 ":"4. ";String name=players[i].getText().toString().trim();if(name.isEmpty())name="Igrač "+(i+1);sb.append(medal).append(name).append(" — ").append(sum[i]).append("\n");}ranking.setText(sb.toString());}
    void newGame(){for(int r=0;r<28;r++)for(int c=0;c<4;c++)scores[r][c].setText("");for(int r=0;r<28;r++)for(int c=0;c<4;c++)sp.edit().remove("s"+r+"_"+c).apply();calc();}
    void confirmClear(){new AlertDialog.Builder(this).setTitle("Obriši sve?").setMessage("Brišu se imena i svi rezultati.").setPositiveButton("Obriši",(d,w)->{sp.edit().clear().apply();load();calc();}).setNegativeButton("Odustani",null).show();}
    abstract class SimpleWatcher implements TextWatcher{public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){}}
}
