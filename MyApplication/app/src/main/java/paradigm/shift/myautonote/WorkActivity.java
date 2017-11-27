package paradigm.shift.myautonote;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import paradigm.shift.myautonote.data_model.LineObject;

public class WorkActivity extends AppCompatActivity{

    private String[] content;
    private ArrayList<LineObject> lineData;

    private int pad1 = 0;
    private int pad2 = 0;
    private int pad3 = 0;

    private ScrollView scrollView;
    private LinearLayout formattedViewer;
    private TextView currentLine;
    private int workingIndex = 0;
    private EditText editor;
    private int editorOffset = 0;
    private boolean switchLine = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        //TODO- does not work
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setTitle(getIntent().getStringExtra("note_title"));

        formattedViewer = (LinearLayout) findViewById(R.id.formatted_viewer);
        scrollView = (ScrollView) findViewById(R.id.scrollable_viewer);


        String givenData = getIntent().getStringExtra("note_content");
        if(givenData.length() > 0){
            content = givenData.split("<p>");
            lineData = new ArrayList<LineObject>();
            for(int i = 1; i < content.length; i++){

                TextView newView = createNewTextView(i-1);

                String parsableText = "";
                if(content[i].length() > 4 && content[i].substring(content[i].length()-4).equals("</p>"))
                    parsableText = content[i].substring(0, content[i].length()-4);
                else
                    parsableText = content[i];

                LineObject lo = new LineObject(i-1, parsableText, pad1, pad2, pad3, false);
                lo.printLineObject(this, newView);
                setPadding(lo);
                lineData.add(lo);
                formattedViewer.addView(newView);

            }
        }

        workingIndex = lineData.size();

        currentLine = createNewTextView(workingIndex);


        LineObject lo = new LineObject(workingIndex, "", pad1, pad2, pad3, true);
        lo.printLineObject(this, currentLine);
        setPadding(lo);
        lineData.add(lo);
        formattedViewer.addView(currentLine);

        editor = (EditText) findViewById(R.id.edit_box);
        editor.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if((currentLine.getY()+currentLine.getHeight()) > (scrollView.getScrollY()+scrollView.getHeight()) || currentLine.getY() < scrollView.getScrollY()){
                            if((int)currentLine.getY() > 0)
                                scrollView.smoothScrollTo(0, (int)currentLine.getY()-scrollView.getHeight()/2);
                        }
                    }
                }, 100);
            }
        });


        editor.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(!switchLine){
                    if(s.length() > 0 && s.toString().indexOf("\n") == s.length()-1){
                        switchLine = true;

                        lineData.get(workingIndex).endWork(currentLine);

                        //TODO split line if in middle
                        editor.setText("");


                        for(int i = lineData.size()-1; i > workingIndex; i--){// insert id in between
                            findViewById(i).setId(i+1);
                        }
                        workingIndex++;
                        currentLine = createNewTextView(workingIndex);
                        LineObject lo = new LineObject(workingIndex, "", pad1, pad2, pad3, true);
                        lo.printLineObject(WorkActivity.this, currentLine);
                        setPadding(lo);
                        if(workingIndex >= lineData.size()){
                            lineData.add(lo);
                            formattedViewer.addView(currentLine);
                        }else{
                            lineData.add(workingIndex, lo);
                            formattedViewer.addView(currentLine, workingIndex);
                        }

                        switchLine = false;
                    }else{
                        currentLine.setText(editor.getText().toString());
                        lineData.get(workingIndex).content = editor.getText().toString();

                        formatLines(workingIndex);

                    }
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if((currentLine.getY()+currentLine.getHeight()) > (scrollView.getScrollY()+scrollView.getHeight()) || currentLine.getY() < scrollView.getScrollY()){
                                if((int)currentLine.getY() > 0)
                                    scrollView.smoothScrollTo(0, (int)currentLine.getY()-scrollView.getHeight()/2);
                            }
                        }
                    }, 150);
                }
            }
        });
        ImageButton captureButton = (ImageButton) findViewById(R.id.camera_icon);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivity(intent);
            }
        }




    );
    }

    public View.OnClickListener onClickLineListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            TextView lineView = (TextView)v;

            if(lineView.getId() != currentLine.getId()){
                switchLine = true;
                lineData.get(workingIndex).endWork(currentLine);
                currentLine = lineView;
                workingIndex = (int)lineView.getId();
                editor.setText(lineView.getText());
                if(workingIndex == 0)
                    lineData.get(workingIndex).copyPaddingFromPreviousLine(null);
                else
                    lineData.get(workingIndex).copyPaddingFromPreviousLine(lineData.get(workingIndex-1));

                lineData.get(workingIndex).content = currentLine.getText().toString();
                setPadding(lineData.get(workingIndex));
                currentLine.setOnClickListener(onClickLineListener);
                currentLine.setBackgroundColor(ContextCompat.getColor(WorkActivity.this, R.color.textHighlight));
                editor.requestFocus();
                switchLine = false;
            }

            InputMethodManager keyboard = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editor, 0);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    editor.setSelection(editorOffset);
                    if((currentLine.getY()+currentLine.getHeight()) > (scrollView.getScrollY()+scrollView.getHeight()) || currentLine.getY() < scrollView.getScrollY()){
                        if((int)currentLine.getY() > 0)
                            scrollView.smoothScrollTo(0, (int)currentLine.getY()-scrollView.getHeight()/2);
                    }
                }
            }, 150);

        }
    };


    public View.OnTouchListener onTouchLineListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                Layout layout = ((TextView) view).getLayout();

                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();
                    editorOffset =  0;
                    if (layout!=null){
                        int line = layout.getLineForVertical(y);
                        editorOffset = layout.getOffsetForHorizontal(line, x-view.getPaddingLeft());
                    }
            }
            return false;
        }
    };

    private void formatLines(int startIndex){
        for(int i = startIndex; i < lineData.size(); i++){

            TextView newView = findViewById(i);
            LineObject lo = lineData.get(i);
            lo.printLineObject(this, newView);
            setPadding(lo);
        }

    }

    private TextView createNewTextView(int index){
        TextView temp = new TextView(this);
        temp.setId(index);
        temp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        temp.setOnClickListener(onClickLineListener);
        temp.setOnTouchListener(onTouchLineListener);
        return temp;
    }


    private void setPadding(LineObject lo){
        pad1 = lo.pad1;
        pad2 = lo.pad2;
        pad3 = lo.pad3;
    }



}
