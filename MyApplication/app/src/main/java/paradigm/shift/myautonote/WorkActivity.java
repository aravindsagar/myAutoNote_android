package paradigm.shift.myautonote;

import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

import paradigm.shift.myautonote.data_model.LineObject;

import static android.support.v4.view.ViewCompat.setOverScrollMode;
import static android.view.View.OVER_SCROLL_ALWAYS;
import static android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS;

public class WorkActivity extends AppCompatActivity {

    private ArrayList<String> content;

    private int pad1 = 0;
    private int pad2 = 0;
    private int pad3 = 0;

    private ScrollView scrollView;
    private LinearLayout formattedViewer;
    private TextView currentLine;
    private int workingIndex = 0;
    private EditText editor;
    private boolean switchLine = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        String result = getIntent().getStringExtra("note_title");

//        TextView testViewer = (TextView) findViewById(R.id.test_viewer);
//        testViewer.setText(getIntent().getStringExtra("note_content"));
//        setTitle(result);

        formattedViewer = (LinearLayout) findViewById(R.id.formatted_viewer);
        scrollView = (ScrollView) findViewById(R.id.scrollable_viewer);
        scrollView.setOverScrollMode(OVER_SCROLL_ALWAYS);

        String givenData = getIntent().getStringExtra("note_content");
        if(givenData.length() > 0){
            content = new ArrayList<String>(Arrays.asList(givenData.split("<p>")));
            for(int i = 1; i < content.size(); i++){

                TextView newView = new TextView(this);
                newView.setId(i);
                newView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                String parsableText = "";
                if(content.get(i).length() > 4 && content.get(i).substring(content.get(i).length()-4).equals("</p>"))
                    parsableText =content.get(i).substring(0, content.get(i).length()-4);
                else
                    parsableText = content.get(i);

                LineObject lo = new LineObject(i, parsableText, pad1, pad2, pad3, false);
                lo.printLineObject(this, newView);
                setPadding(lo);

                formattedViewer.addView(newView);
                newView.setOnClickListener(onClickLineListener);
            }
        }

        currentLine = new TextView(this);
        currentLine.setId(content.size());
        currentLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.add("");
        workingIndex = content.size()-1;
        LineObject lo = new LineObject(content.size()-1, "", pad1, pad2, pad3, true);
        //currentLine.setBackgroundColor(ContextCompat.getColor(this, R.color.textHighlight));
        //currentLine.setText("new line");
       // currentLine.setTypeface(null, Typeface.ITALIC);

        lo.printLineObject(this, currentLine);
        setPadding(lo);
        currentLine.setOnClickListener(onClickLineListener);
        formattedViewer.addView(currentLine);



        editor = (EditText) findViewById(R.id.edit_box);

        editor.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        scrollView.scrollTo(0, (int)currentLine.getY());
                        //scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 100);

            }
        });


        editor.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(!switchLine){
                    if(s.toString().indexOf("\n") != -1){
                        switchLine = true;
                        currentLine.setBackgroundColor(0x00000000);
                        if(content.get(workingIndex).length() == 0 || editor.getText().toString().length() == 0){
                            formattedViewer.removeView(currentLine);

                            currentLine = new TextView(WorkActivity.this);
                            currentLine.setId(workingIndex);
                            currentLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            currentLine.setText("");
                            LineObject lo = new LineObject(workingIndex, "", pad1, pad2, pad3, false);
                            lo.printLineObject(WorkActivity.this, currentLine);
                            setPadding(lo);
                            currentLine.setOnClickListener(onClickLineListener);
                            formattedViewer.addView(currentLine);
                            currentLine.setText("");
                        }
                        editor.setText("");
                        //TODO insert new line
                        content.add("");
                        workingIndex = content.size()-1;
                        currentLine = new TextView(WorkActivity.this);
                        currentLine.setId(workingIndex);
                        currentLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        LineObject lo = new LineObject(workingIndex, "", pad1, pad2, pad3, true);
                        lo.printLineObject(WorkActivity.this, currentLine);
                        setPadding(lo);
                        currentLine.setOnClickListener(onClickLineListener);
                        formattedViewer.addView(currentLine);
                        switchLine = false;
                    }else{
                        //formattedViewer.removeView(currentLine);

                        //currentLine = new TextView(WorkActivity.this);
                        //currentLine.setId(workingIndex);
                        //currentLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        currentLine.setText(editor.getText().toString());
                        content.set(workingIndex, editor.getText().toString());
                        LineObject lo = new LineObject(workingIndex, editor.getText().toString(), pad1, pad2, pad3, true);
                        lo.printLineObject(WorkActivity.this, currentLine);
                        setPadding(lo);
                        currentLine.setOnClickListener(onClickLineListener);
                        //formattedViewer.addView(currentLine);

                    }
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            if((int)currentLine.getY() > 0)
                                scrollView.smoothScrollTo(0, (int)currentLine.getY());
                            //scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    }, 150);
                }







            }
        });
    }

    public View.OnClickListener onClickLineListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            TextView line = (TextView)v;

            if(line.getId() != currentLine.getId()){
                switchLine = true;

                currentLine.setBackgroundColor(0x00000000);

                if(content.get(content.size()-1).length() == 0 || editor.getText().toString().length() == 0){
                    //formattedViewer.removeView(currentLine);

                    //currentLine = new TextView(WorkActivity.this);
                    //currentLine.setId(content.size()-1);
                    //currentLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    //currentLine.setText("");
                    //LineObject lo = new LineObject(workingIndex, "", pad1, pad2, pad3, false);
                    //lo.printLineObject(WorkActivity.this, currentLine);
                    //setPadding(lo);
                    //currentLine.setOnClickListener(onClickLineListener);
                    //formattedViewer.addView(currentLine);
                    //currentLine.setText("");
                }


                currentLine = line;
                workingIndex = (int)line.getId();
                editor.setText(line.getText());
                content.set(workingIndex, currentLine.getText().toString());
                currentLine.setOnClickListener(onClickLineListener);
                currentLine.setBackgroundColor(ContextCompat.getColor(WorkActivity.this, R.color.textHighlight));
                editor.requestFocus();
                switchLine = false;
            }

        }
    };


    private void setPadding(LineObject lo){
        pad1 = lo.pad1;
        pad2 = lo.pad2;
        pad3 = lo.pad3;
    }





}
