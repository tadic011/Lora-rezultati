package rs.imlek.lorascore;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final String[] games = {"Plus", "Minus", "Pop Herc", "Dame", "Herčevi", "Lora", "Igra po želji"};
    private final EditText[] nameFields = new EditText[4];
    private final EditText[] scoreFields = new EditText[4];

    private LinearLayout root;
    private int round = 0, gameIndex = 0;
    private int[][][] scores = new int[4][7][4];
    private String[] names = {"Igrač 1", "Igrač 2", "Igrač 3", "Igrač 4"};
    private SharedPreferences prefs;

    private final int BG = Color.rgb(8, 14, 14);
    private final int CARD = Color.rgb(18, 28, 28);
    private final int GREEN = Color.rgb(104, 214, 112);
    private final int GREEN_DARK = Color.rgb(37, 126, 55);
    private final int TEXT = Color.WHITE;
    private final int MUTED = Color.rgb(190, 200, 196);

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(6, 10, 10));
        getWindow().setNavigationBarColor(Color.rgb(6, 10, 10));
        prefs = getSharedPreferences("lora_score", MODE_PRIVATE);
        load();
        showStart();
    }

    private GradientDrawable bg(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        if (strokeWidth > 0) g.setStroke(strokeWidth, strokeColor);
        return g;
    }

    private void base() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 34, 28, 34);

        scroll.addView(root);
        setContentView(scroll);
    }

    private TextView text(String s, int sp, int style) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(TEXT);
        v.setTextSize(sp);
        v.setTypeface(Typeface.DEFAULT, style);
        v.setPadding(0, 8, 0, 8);
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
        c.setPadding(22, 20, 22, 20);
        c.setBackground(bg(CARD, 28, Color.rgb(45, 75, 55), 2));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 12, 0, 12);
        c.setLayoutParams(lp);
        return c;
    }

    private Button button(String s, boolean primary) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setPadding(14, 18, 14, 18);
        b.setBackground(bg(primary ? GREEN_DARK : Color.rgb(38, 45, 45), 20, primary ? GREEN : Color.rgb(65, 75, 75), 1));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 12, 0, 10);
        b.setLayoutParams(lp);
        return b;
    }

    private EditText input(String s) {
        EditText e = new EditText(this);
        e.setText(s);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(MUTED);
        e.setTextSize(19);
        e.setSingleLine(true);
        e.setSelectAllOnFocus(true);
        e.setPadding(20, 14, 20, 14);
        e.setBackground(bg(Color.rgb(7, 12, 12), 16, GREEN_DARK, 2));
        return e;
    }

    private void addHeader(String subtitle) {
        TextView title = text("♠ Lora Score", 34, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        TextView sub = text(subtitle, 22, Typeface.BOLD);
        sub.setTextColor(GREEN);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 18, 0, 16);
        sub.setLayoutParams(lp);
        root.addView(sub);
    }

    private void showStart() {
        base();
        addHeader("Unesi imena igrača");

        for (int i = 0; i < 4; i++) {
            LinearLayout c = card();
            c.addView(text("👤  Igrač " + (i + 1), 16, Typeface.BOLD));
            nameFields[i] = input(names[i]);
            c.addView(nameFields[i]);
            root.addView(c);
        }

        Button start = button("▶  Počni partiju", true);
        start.setOnClickListener(v -> {
            for (int i = 0; i < 4; i++) {
                String n = nameFields[i].getText().toString().trim();
                names[i] = n.isEmpty() ? "Igrač " + (i + 1) : n;
            }
            save();
            showGame();
        });
        root.addView(start);

        Button reset = button("🗑  Obriši sve", false);
        reset.setOnClickListener(v -> confirmReset());
        root.addView(reset);

        LinearLayout footer = card();
        footer.addView(text("🏆 Lora Score", 22, Typeface.BOLD));
        footer.addView(muted("Pratite bodove i uživajte u partiji!", 16));
        root.addView(footer);
    }

    private void showGame() {
        base();
        addHeader("Krug " + (round + 1) + " / 4");

        LinearLayout gameCard = card();
        TextView gameTitle = text(games[gameIndex], 32, Typeface.BOLD);
        gameTitle.setTextColor(GREEN);
        gameCard.addView(gameTitle);
        gameCard.addView(muted("Unesi poene. Pobednik je igrač sa najmanje poena.", 15));
        root.addView(gameCard);

        for (int i = 0; i < 4; i++) {
            LinearLayout c = card();
            c.addView(text(names[i], 17, Typeface.BOLD));
            scoreFields[i] = input(String.valueOf(scores[round][gameIndex][i]));
            scoreFields[i].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            scoreFields[i].setOnFocusChangeListener((v, has) -> { if (!has) readScores(); });
            c.addView(scoreFields[i]);
            root.addView(c);
        }

        addTotals();

        Button next = button(isLast() ? "🏁  Završi partiju" : "▶  Sledeća igra", true);
        next.setOnClickListener(v -> {
            readScores();
            save();
            if (isLast()) showResult();
            else {
                advance();
                showGame();
            }
        });
        root.addView(next);

        Button back = button("◀  Nazad", false);
        back.setOnClickListener(v -> {
            readScores();
            save();
            retreat();
            showGame();
        });
        root.addView(back);
    }

    private void addTotals() {
        LinearLayout c = card();
        c.addView(text("Total score", 23, Typeface.BOLD));
        int[] t = totals();

        Integer[] order = {0,1,2,3};
        Arrays.sort(order, (a,b) -> Integer.compare(t[a], t[b]));

        for (int rank = 0; rank < 4; rank++) {
            int i = order[rank];
            TextView line = text((rank + 1) + ".  " + names[i] + "    " + t[i], 19, Typeface.BOLD);
            if (rank == 0) line.setTextColor(GREEN);
            c.addView(line);
        }
        root.addView(c);
    }

    private void showResult() {
        base();
        int[] t = totals();
        Integer[] order = {0,1,2,3};
        Arrays.sort(order, (a,b) -> Integer.compare(t[a], t[b]));

        addHeader("🏆 Pobednik");

        LinearLayout winner = card();
        TextView w = text("🥇 " + names[order[0]], 32, Typeface.BOLD);
        w.setTextColor(GREEN);
        winner.addView(w);
        winner.addView(text("Rezultat: " + t[order[0]], 24, Typeface.BOLD));
        root.addView(winner);

        LinearLayout ranking = card();
        ranking.addView(text("Konačan plasman", 22, Typeface.BOLD));
        for (int i = 0; i < 4; i++) {
            int p = order[i];
            String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "4.";
            TextView row = text(medal + "  " + names[p] + "    " + t[p], 20, Typeface.BOLD);
            if (i == 0) row.setTextColor(GREEN);
            ranking.addView(row);
        }
        root.addView(ranking);

        Button again = button("▶  Nova partija", true);
        again.setOnClickListener(v -> {
            scores = new int[4][7][4];
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

    private void readScores() {
        for (int i = 0; i < 4; i++) {
            try {
                scores[round][gameIndex][i] = Integer.parseInt(scoreFields[i].getText().toString().trim());
            } catch (Exception e) {
                scores[round][gameIndex][i] = 0;
            }
        }
    }

    private int[] totals() {
        int[] t = new int[4];
        for (int r = 0; r < 4; r++)
            for (int g = 0; g < 7; g++)
                for (int p = 0; p < 4; p++)
                    t[p] += scores[r][g][p];
        return t;
    }

    private boolean isLast() {
        return round == 3 && gameIndex == 6;
    }

    private void advance() {
        gameIndex++;
        if (gameIndex == 7) {
            gameIndex = 0;
            round++;
        }
    }

    private void retreat() {
        if (gameIndex > 0) gameIndex--;
        else if (round > 0) {
            round--;
            gameIndex = 6;
        }
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Obriši sve?")
                .setMessage("Brišu se svi rezultati.")
                .setPositiveButton("Obriši", (d,w) -> {
                    prefs.edit().clear().apply();
                    scores = new int[4][7][4];
                    names = new String[]{"Igrač 1", "Igrač 2", "Igrač 3", "Igrač 4"};
                    round = 0;
                    gameIndex = 0;
                    showStart();
                })
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void save() {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("round", round);
        e.putInt("game", gameIndex);

        for (int i = 0; i < 4; i++) e.putString("name" + i, names[i]);

        for (int r = 0; r < 4; r++)
            for (int g = 0; g < 7; g++)
                for (int p = 0; p < 4; p++)
                    e.putInt("s" + r + "_" + g + "_" + p, scores[r][g][p]);

        e.apply();
    }

    private void load() {
        round = prefs.getInt("round", 0);
        gameIndex = prefs.getInt("game", 0);

        for (int i = 0; i < 4; i++) names[i] = prefs.getString("name" + i, names[i]);

        for (int r = 0; r < 4; r++)
            for (int g = 0; g < 7; g++)
                for (int p = 0; p < 4; p++)
                    scores[r][g][p] = prefs.getInt("s" + r + "_" + g + "_" + p, 0);
    }
}
