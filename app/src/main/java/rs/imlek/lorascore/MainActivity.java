package rs.imlek.lorascore;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final String[] games = {"Plus", "Minus", "Pop Herc", "Dame", "Herčevi", "Lora", "Igra po želji"};
    private final EditText[] nameFields = new EditText[4];
    private final EditText[] scoreFields = new EditText[4];

    private LinearLayout root, totalCard;
    private TextView ruleMessage;
    private int round = 0, gameIndex = 0;
    private int[][][] scores = new int[4][7][4];
    private boolean[][] visited = new boolean[4][7];
    private String[] names = {"Igrač 1", "Igrač 2", "Igrač 3", "Igrač 4"};
    private SharedPreferences prefs;

    private final int BG = Color.rgb(8,14,14);
    private final int CARD = Color.rgb(18,28,28);
    private final int GREEN = Color.rgb(104,214,112);
    private final int GREEN_DARK = Color.rgb(37,126,55);
    private final int RED = Color.rgb(238,82,82);
    private final int TEXT = Color.WHITE;
    private final int MUTED = Color.rgb(190,200,196);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(6,10,10));
        getWindow().setNavigationBarColor(Color.rgb(6,10,10));
        prefs = getSharedPreferences("lora_score", MODE_PRIVATE);
        load();
        showStart();
    }

    private int dp(int v){ return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable bg(int color, int radiusDp, int strokeColor, int strokeWidthDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        if (strokeWidthDp > 0) g.setStroke(dp(strokeWidthDp), strokeColor);
        return g;
    }

    private void base() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(16), dp(14), dp(18));
        scroll.addView(root);
        setContentView(scroll);
    }

    private TextView text(String s, int sp, int style) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(TEXT);
        v.setTextSize(sp);
        v.setTypeface(Typeface.DEFAULT, style);
        v.setPadding(0, dp(3), 0, dp(3));
        return v;
    }

    private TextView muted(String s, int sp) {
        TextView v = text(s, sp, Typeface.NORMAL);
        v.setTextColor(MUTED);
        return v;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12), dp(10), dp(12), dp(10));
        c.setBackground(bg(CARD, 15, Color.rgb(45,75,55), 1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(5), 0, dp(5));
        c.setLayoutParams(lp);
        return c;
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        return r;
    }

    private LinearLayout gridRow() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        return r;
    }

    private Button button(String s, boolean primary) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setPadding(dp(12), dp(12), dp(12), dp(12));
        b.setBackground(bg(primary ? Color.rgb(35,139,61) : Color.rgb(38,45,45), 15, primary ? GREEN : Color.rgb(65,75,75), 1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(6), 0, dp(5));
        b.setLayoutParams(lp);
        return b;
    }

    private EditText input(String s) {
        EditText e = new EditText(this);
        e.setText(s);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(MUTED);
        e.setTextSize(17);
        e.setSingleLine(true);
        e.setSelectAllOnFocus(true);
        e.setPadding(dp(10), dp(8), dp(10), dp(8));
        e.setBackground(bg(Color.rgb(7,12,12), 10, GREEN_DARK, 1));
        return e;
    }

    private ImageView iconView(int resId) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(resId);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setPadding(dp(2), dp(2), dp(2), dp(2));
        iv.setBackground(bg(Color.rgb(7,18,14), 12, GREEN_DARK, 1));
        iv.setAdjustViewBounds(true);
        return iv;
    }

    private void addHeader(String subtitle) {
        LinearLayout header = row();
        ImageView logo = iconView(R.drawable.ic_launcher_lora);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        logoLp.setMargins(0, 0, dp(10), 0);
        header.addView(logo, logoLp);
        TextView title = text("Lora Score", 28, Typeface.BOLD);
        header.addView(title);
        root.addView(header);

        TextView sub = text(subtitle, 20, Typeface.BOLD);
        sub.setTextColor(GREEN);
        root.addView(sub);
    }

    private int gameIconRes() {
        switch (gameIndex) {
            case 0: return R.drawable.ic_game_plus;
            case 1: return R.drawable.ic_game_minus;
            case 2: return R.drawable.ic_game_popherc;
            case 3: return R.drawable.ic_game_dame;
            case 4: return R.drawable.ic_game_hercevi;
            case 5: return R.drawable.ic_game_lora;
            case 6: return R.drawable.ic_game_zelja;
            default: return R.drawable.ic_game_lora;
        }
    }

    private String ruleText() {
        if (gameIndex == 0) return "Samo negativni brojevi. Zbir ne sme biti manji od -8.";
        if (gameIndex >= 1 && gameIndex <= 3) return "Samo pozitivni brojevi. Zbir ne sme biti veći od 8.";
        if (gameIndex == 4) return "Herčevi: pozitivno do zbira 8 ili jedan igrač -8, ostali 0.";
        return "Slobodan unos pozitivnih i negativnih poena.";
    }

    private void clearScoresForCurrentGame() {
        for (int i=0; i<4; i++) scores[round][gameIndex][i] = 0;
    }

    private void showStart() {
        base();
        addHeader("Unesi imena igrača");

        for (int rr=0; rr<2; rr++) {
            LinearLayout gr = gridRow();
            root.addView(gr);
            for (int c=0; c<2; c++) {
                int i = rr*2+c;
                LinearLayout box = card();
                box.addView(text("Igrač " + (i+1), 14, Typeface.BOLD));
                nameFields[i] = input(names[i]);
                box.addView(nameFields[i]);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
                lp.setMargins(c==0?0:dp(4), dp(3), c==0?dp(4):0, dp(3));
                gr.addView(box, lp);
            }
        }

        Button start = button("▶  Počni partiju", true);
        start.setOnClickListener(v -> {
            for (int i=0; i<4; i++) {
                String n = nameFields[i].getText().toString().trim();
                names[i] = n.isEmpty() ? "Igrač " + (i+1) : n;
            }
            save();
            showGame();
        });
        root.addView(start);

        Button reset = button("🗑  Obriši sve", false);
        reset.setOnClickListener(v -> confirmReset());
        root.addView(reset);
    }

    private void showGame() {
        base();
        addHeader("Krug " + (round+1) + " / 4");

        LinearLayout gameCard = card();
        LinearLayout top = row();
        ImageView icon = iconView(gameIconRes());
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(72), dp(72));
        iconLp.setMargins(0,0,dp(12),0);
        top.addView(icon, iconLp);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        TextView gameTitle = text(games[gameIndex], 29, Typeface.BOLD);
        gameTitle.setTextColor(GREEN);
        titleBox.addView(gameTitle);
        titleBox.addView(muted("Igra " + (gameIndex+1) + " / 7", 13));
        titleBox.addView(muted(ruleText(), 12));
        top.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));
        gameCard.addView(top);

        ruleMessage = muted("", 13);
        gameCard.addView(ruleMessage);
        root.addView(gameCard);

        for (int rr=0; rr<2; rr++) {
            LinearLayout gr = gridRow();
            root.addView(gr);
            for (int c=0; c<2; c++) {
                int i = rr*2+c;
                LinearLayout box = card();
                box.addView(text(names[i], 14, Typeface.BOLD));
                scoreFields[i] = input(String.valueOf(scores[round][gameIndex][i]));
                scoreFields[i].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                final int playerIndex = i;
                scoreFields[i].addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        scores[round][gameIndex][playerIndex] = parseScore(s.toString());
                        visited[round][gameIndex] = true;
                        refreshTotals();
                        refreshRuleMessage();
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });
                box.addView(scoreFields[i]);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
                lp.setMargins(c==0?0:dp(4), dp(3), c==0?dp(4):0, dp(3));
                gr.addView(box, lp);
            }
        }

        totalCard = card();
        root.addView(totalCard);
        refreshTotals();
        refreshRuleMessage();

        Button preview = button("ZA NEVERNE TOME", false);
        preview.setOnClickListener(v -> {
            readScores();
            visited[round][gameIndex] = true;
            save();
            showPreview();
        });
        root.addView(preview);

        Button next = button(isLast() ? "🏁  Završi partiju" : "▶  Sledeća igra", true);
        next.setOnClickListener(v -> {
            readScores();
            visited[round][gameIndex] = true;
            String error = validationError();
            if (!error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                refreshRuleMessage();
                return;
            }
            save();
            if (isLast()) showResult();
            else {
                advance();
                if (!visited[round][gameIndex]) clearScoresForCurrentGame();
                save();
                showGame();
            }
        });
        root.addView(next);

        Button back = button("◀  Nazad", false);
        back.setOnClickListener(v -> {
            readScores();
            visited[round][gameIndex] = true;
            save();
            retreat();
            showGame();
        });
        root.addView(back);
    }

    private int currentGameSum() {
        int s = 0;
        for (int i=0; i<4; i++) s += scores[round][gameIndex][i];
        return s;
    }

    private String validationError() {
        int sum = currentGameSum();

        if (gameIndex == 0) {
            for (int i=0; i<4; i++) {
                if (scores[round][gameIndex][i] > 0) return "Plus prihvata samo negativne brojeve.";
            }
            if (sum < -8) return "Zbir u igri Plus ne sme biti manji od -8. Trenutno: " + sum;
        }

        if (gameIndex >= 1 && gameIndex <= 3) {
            for (int i=0; i<4; i++) {
                if (scores[round][gameIndex][i] < 0) return games[gameIndex] + " prihvata samo pozitivne brojeve.";
            }
            if (sum > 8) return "Zbir u igri " + games[gameIndex] + " ne sme biti veći od 8. Trenutno: " + sum;
        }

        if (gameIndex == 4) {
            int negativeCount = 0;
            int minusEightCount = 0;
            int positiveCount = 0;
            int zeroCount = 0;

            for (int i=0; i<4; i++) {
                int value = scores[round][gameIndex][i];

                if (value < 0) {
                    negativeCount++;
                    if (value == -8) minusEightCount++;
                } else if (value > 0) {
                    positiveCount++;
                } else {
                    zeroCount++;
                }
            }

            boolean onePlayerMinusEight = minusEightCount == 1 && negativeCount == 1 && positiveCount == 0 && zeroCount == 3;
            boolean positiveMode = negativeCount == 0 && sum <= 8;

            if (!onePlayerMinusEight && !positiveMode) {
                return "Herčevi: dozvoljeno je pozitivno do zbira 8 ili tačno jedan igrač -8, ostali 0. Trenutno: " + sum;
            }
        }

        return "";
    }

    private void refreshRuleMessage() {
        if (ruleMessage == null) return;
        String error = validationError();
        if (error.isEmpty()) {
            ruleMessage.setText("Zbir trenutne igre: " + currentGameSum() + "  ✓");
            ruleMessage.setTextColor(GREEN);
        } else {
            ruleMessage.setText(error);
            ruleMessage.setTextColor(RED);
        }
    }

    private int parseScore(String value) {
        try {
            String v = value.trim();
            if (v.isEmpty() || v.equals("-") || v.equals("+")) return 0;
            return Integer.parseInt(v);
        } catch(Exception e) {
            return 0;
        }
    }

    private void readScores() {
        for (int i=0; i<4; i++) if (scoreFields[i] != null) scores[round][gameIndex][i] = parseScore(scoreFields[i].getText().toString());
        visited[round][gameIndex] = true;
    }

    private void refreshTotals() {
        if (totalCard == null) return;
        totalCard.removeAllViews();
        TextView title = text("Total score", 21, Typeface.BOLD);
        title.setTextColor(GREEN);
        totalCard.addView(title);

        int[] t = totals();
        Integer[] order = {0,1,2,3};
        Arrays.sort(order, (a,b) -> {
            int cmp = Integer.compare(t[a], t[b]);
            return cmp != 0 ? cmp : Integer.compare(a,b);
        });

        for (int rank=0; rank<4; rank++) {
            int i = order[rank];
            TextView line = text((rank+1) + ".  " + names[i] + "    " + t[i], 17, Typeface.BOLD);
            if (rank == 0) line.setTextColor(GREEN);
            totalCard.addView(line);
        }
    }

    private void showPreview() {
        base();
        addHeader("ZA NEVERNE TOME");

        LinearLayout intro = card();
        intro.addView(text("Pregled poena tokom partije", 20, Typeface.BOLD));
        intro.addView(muted("Tabelarni prikaz svih unetih rezultata po igri i igraču.", 13));
        root.addView(intro);

        LinearLayout table = card();

        LinearLayout header = row();
        TextView first = previewCell("Igra", true, GREEN);
        header.addView(first, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.35f));

        for (int p = 0; p < 4; p++) {
            TextView h = previewCell(shortName(names[p]), true, GREEN);
            header.addView(h, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }
        table.addView(header);

        for (int r = 0; r < 4; r++) {
            for (int g = 0; g < 7; g++) {
                if (!visited[r][g] && !(r == round && g == gameIndex)) continue;

                LinearLayout tr = row();
                String label = (r + 1) + ". " + games[g];
                tr.addView(previewCell(label, false, TEXT), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.35f));

                for (int p = 0; p < 4; p++) {
                    tr.addView(previewCell(String.valueOf(scores[r][g][p]), false, TEXT), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                }
                table.addView(tr);
            }
        }

        LinearLayout total = row();
        total.addView(previewCell("TOTAL", true, GREEN), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.35f));

        int[] t = totals();
        for (int p = 0; p < 4; p++) {
            total.addView(previewCell(String.valueOf(t[p]), true, GREEN), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }
        table.addView(total);

        root.addView(table);

        Button backToGame = button("◀  Nazad na igru", true);
        backToGame.setOnClickListener(v -> showGame());
        root.addView(backToGame);

        Button startScreen = button("👥  Imena igrača", false);
        startScreen.setOnClickListener(v -> showStart());
        root.addView(startScreen);
    }

    private TextView previewCell(String value, boolean bold, int color) {
        TextView v = text(value, 13, bold ? Typeface.BOLD : Typeface.NORMAL);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(3), dp(5), dp(3), dp(5));
        v.setBackground(bg(Color.rgb(7, 12, 12), 6, Color.rgb(45, 75, 55), 1));
        return v;
    }

    private String shortName(String value) {
        if (value == null) return "";
        String v = value.trim();
        if (v.length() <= 7) return v;
        return v.substring(0, 7);
    }

    private void showResult() {
        readScores();
        String error = validationError();
        if (!error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            showGame();
            return;
        }
        save();

        base();
        int[] t = totals();
        Integer[] order = {0,1,2,3};
        Arrays.sort(order, (a,b) -> {
            int cmp = Integer.compare(t[a], t[b]);
            return cmp != 0 ? cmp : Integer.compare(a,b);
        });

        addHeader("🏆 Pobednik");

        LinearLayout winner = card();
        TextView w = text("🥇 " + names[order[0]], 32, Typeface.BOLD);
        w.setTextColor(GREEN);
        winner.addView(w);
        winner.addView(text("Rezultat: " + t[order[0]], 24, Typeface.BOLD));
        root.addView(winner);

        LinearLayout ranking = card();
        ranking.addView(text("Konačan plasman", 22, Typeface.BOLD));
        for (int i=0; i<4; i++) {
            int p = order[i];
            String medal = i==0 ? "🥇" : i==1 ? "🥈" : i==2 ? "🥉" : "4.";
            TextView row = text(medal + "  " + names[p] + "    " + t[p], 20, Typeface.BOLD);
            if (i==0) row.setTextColor(GREEN);
            ranking.addView(row);
        }
        root.addView(ranking);

        Button again = button("▶  Nova partija", true);
        again.setOnClickListener(v -> {
            scores = new int[4][7][4];
            visited = new boolean[4][7];
            round = 0;
            gameIndex = 0;
            save();
            showGame();
        });
        root.addView(again);

        Button namesBtn = button("👥  Promeni imena", false);
        namesBtn.setOnClickListener(v -> showStart());
        root.addView(namesBtn);
    }

    private int[] totals() {
        int[] t = new int[4];
        for (int r=0; r<4; r++) for (int g=0; g<7; g++) {
            if (!visited[r][g] && !(r == round && g == gameIndex)) continue;
            for (int p=0; p<4; p++) t[p] += scores[r][g][p];
        }
        return t;
    }

    private boolean isLast(){ return round == 3 && gameIndex == 6; }

    private void advance(){
        gameIndex++;
        if (gameIndex == 7) {
            gameIndex = 0;
            round++;
        }
    }

    private void retreat(){
        if (gameIndex > 0) gameIndex--;
        else if (round > 0) {
            round--;
            gameIndex = 6;
        }
    }

    private void confirmReset(){
        new AlertDialog.Builder(this)
                .setTitle("Obriši sve?")
                .setMessage("Brišu se svi rezultati.")
                .setPositiveButton("Obriši", (d,w) -> {
                    prefs.edit().clear().apply();
                    scores = new int[4][7][4];
                    visited = new boolean[4][7];
                    names = new String[]{"Igrač 1", "Igrač 2", "Igrač 3", "Igrač 4"};
                    round = 0;
                    gameIndex = 0;
                    showStart();
                })
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void save(){
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("round", round);
        e.putInt("game", gameIndex);
        for (int i=0; i<4; i++) e.putString("name"+i, names[i]);
        for (int r=0; r<4; r++) for (int g=0; g<7; g++) {
            e.putBoolean("v"+r+"_"+g, visited[r][g]);
            for (int p=0; p<4; p++) e.putInt("s"+r+"_"+g+"_"+p, scores[r][g][p]);
        }
        e.apply();
    }

    private void load(){
        round = prefs.getInt("round", 0);
        gameIndex = prefs.getInt("game", 0);
        if (round < 0 || round > 3) round = 0;
        if (gameIndex < 0 || gameIndex > 6) gameIndex = 0;
        for (int i=0; i<4; i++) names[i] = prefs.getString("name"+i, names[i]);
        for (int r=0; r<4; r++) for (int g=0; g<7; g++) {
            visited[r][g] = prefs.getBoolean("v"+r+"_"+g, false);
            for (int p=0; p<4; p++) scores[r][g][p] = prefs.getInt("s"+r+"_"+g+"_"+p, 0);
        }
    }
}
