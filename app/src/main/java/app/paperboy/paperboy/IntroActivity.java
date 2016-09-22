package app.paperboy.paperboy;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created By David Jura
 *
 * This class is intended to handle the Intro Activity
 */
public class IntroActivity extends AppCompatActivity {

    private UserData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Intent me = getIntent();
        this.data = new UserData(this);

        /*Since the home page acts as the settings page as well, check to make sure it isn't
            accessed by an intent service
         */
        if (data.getPreference() != -1) {
            if (me.getExtras() == null)
                goHome();
            else if (!me.getExtras().containsKey("settings_click")) {
                goHome();
            }
        }

        //Setup the typeface font
        Typeface mainFont = Typeface.createFromAsset(getAssets(), "fonts/appfont.ttf");

        //Setup all the layout view elements
        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setTypeface(mainFont);

        Button btnModerate = (Button) findViewById(R.id.btnModerate);
        btnModerate.setTypeface(mainFont);
        btnModerate.setOnClickListener(moderateClick);

        Button btnConservative = (Button) findViewById(R.id.btnConservative);
        btnConservative.setTypeface(mainFont);
        btnConservative.setOnClickListener(conservativeClick);

        Button btnLiberal = (Button) findViewById(R.id.btnLiberal);
        btnLiberal.setTypeface(mainFont);
        btnLiberal.setOnClickListener(liberalClick);

        TextView txtIntro = (TextView) findViewById(R.id.txtIntro);
        txtIntro.setTypeface(mainFont);

        TextView txtCopyright = (TextView) findViewById(R.id.txtCopyright);
        txtCopyright.setTypeface(mainFont);
    }

    /**
     * click listener for the conservative option to set preference
     */
    private View.OnClickListener conservativeClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            data.setPreference(UserData.CONSERVATIVE);
            goHome();
        }
    };

    /**
     * Click listener for the liberal option to set preference
     */
    private View.OnClickListener liberalClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            data.setPreference(UserData.LIBERAL);
            goHome();
        }
    };

    /**
     * Click listener for the moderate option to set preference
     */
    private View.OnClickListener moderateClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            data.setPreference(UserData.MODERATE);
            goHome();
        }
    };

    /**
     * Go to the home activity
     */
    private void goHome() {
        Intent i = new Intent(IntroActivity.this, HomeActivity.class);
        startActivity(i);
        finish();
    }
}
