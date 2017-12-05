package paradigm.shift.myautonote;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.LineObject;
import paradigm.shift.myautonote.data_util.DataReader;
import paradigm.shift.myautonote.data_util.DataWriter;

import static android.provider.MediaStore.EXTRA_OUTPUT;

public class WorkActivity extends AppCompatActivity{

    public static final String CUR_DIR = "cur_dir";
    public static final String NOTE_CONTENT = "note_content";
    public static final String NOTE_TITLE = "note_title";

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
    private ImageView headerButton;
    private TextView titleView;
    private EditText titleEditor;
    private List<Directory> myNoteDir;
    private String myNoteName;
    private Uri myLatestImgUri;
    private int myMaxImgHeight, myMaxImgWidth;
    private boolean dirty = false;
    private DataWriter dataWriter;
    private LinearLayout headerSelectView;
    private View closeHeaderSelect;
    private float textViewHeight;

    //image stuff
    private static final int CAMERA_REQUEST=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        Resources r = getResources();
        textViewHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

        String[] curPath = getIntent().getStringArrayExtra(CUR_DIR);
        Directory dir = DataReader.getInstance(this).getTopDir();
        myNoteDir = new ArrayList<>();
        myNoteDir.add(dir);
        for (int i = 1; i < curPath.length; i++) {
            dir = dir.getSubDirectory(curPath[i]);
            myNoteDir.add(dir);
        }

        dataWriter = DataWriter.getInstance(WorkActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.work_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        titleView = toolbar.findViewById(R.id.text_note_name);
        titleEditor = toolbar.findViewById(R.id.edit_note_name);
        myNoteName = getIntent().getStringExtra(NOTE_TITLE);
        titleView.setText(myNoteName);
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleEditor.setText(myNoteName);
                titleEditor.setSelectAllOnFocus(true);
                titleView.setVisibility(View.GONE);
                titleEditor.setVisibility(View.VISIBLE);
                titleEditor.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(titleEditor, 0);
            }
        });
        titleEditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveTitleEdit();
                    return true;
                }
                return false;
            }
        });
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        myMaxImgHeight = size.y/2;
        myMaxImgWidth = size.x/2;

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        formattedViewer = (LinearLayout) findViewById(R.id.formatted_viewer);
        scrollView = (ScrollView) findViewById(R.id.scrollable_viewer);


        String givenData = getIntent().getStringExtra("note_content");
        lineData = new ArrayList<>();
        if(givenData.length() > 0){
            content = givenData.split("<p>");
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
                        changeHeaderButtonValue(lineData.get(workingIndex).headerSize);

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

                if(!dirty){
                    dirty = true;
                    Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                save();
                            }
                        }, 1000);

                }




            }
        });

        ImageView captureButton = findViewById(R.id.camera_icon);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("check", "in onClick");

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = null;
                try{
                    f = makeImageFile();
                } catch (IOException ex){
                    Log.d("check","failed to create file");
                }
                if(f != null){
                    Uri imageURI = FileProvider.getUriForFile(WorkActivity.this.getApplicationContext(), "paradigm.shift.myautonote.fileprovider", f);
                    Log.d("uri", imageURI.toString());
                    myLatestImgUri = imageURI;
                    intent.putExtra(EXTRA_OUTPUT, imageURI);
                    startActivityForResult(intent, CAMERA_REQUEST);
                }
            }
        });

        headerButton = findViewById(R.id.header_button);
        headerSelectView = (LinearLayout) findViewById(R.id.header_select_view);
        closeHeaderSelect = (View) findViewById(R.id.close_header_select);

        closeHeaderSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeHeaderSelect.setVisibility(View.GONE);
                if(headerSelectView.getVisibility() == View.VISIBLE){

                    ObjectAnimator a = ObjectAnimator.ofFloat(headerSelectView, "alpha", 1, 0);
                    a.setCurrentPlayTime(150);

                    a.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {  }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            headerSelectView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) { }

                        @Override
                        public void onAnimationRepeat(Animator animator) { }
                    });
                    a.start();

                }
            }
        });

        headerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(headerSelectView.getVisibility() == View.VISIBLE){
                    closeHeaderSelect.setVisibility(View.GONE);

                    ObjectAnimator a = ObjectAnimator.ofFloat(headerSelectView, "alpha", 1, 0);
                    a.setCurrentPlayTime(150);

                    a.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            headerSelectView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    a.start();

                }else{
                    headerSelectView.setVisibility(View.VISIBLE);
                    ObjectAnimator a = ObjectAnimator.ofFloat(headerSelectView, "alpha", 0, 1);
                    a.setCurrentPlayTime(150);
                    a.start();
                    closeHeaderSelect.setVisibility(View.VISIBLE);
                }

            }
        });




    }



    //More image stuff
    String currPath;

    private File makeImageFile() throws IOException{
        Log.d("check", "a whole new world");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //or getExternalFilesDir

        File imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("check","finished getfilesdir");
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                imageDir
        );

        currPath = image.getAbsolutePath();

        Log.d("check", currPath);

        return image;
    }

//Image Stuff
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d("activity result", myLatestImgUri.toString());
            ImageView imageView = new ImageView(this);
//            imageView.setMaxHeight(myMaxImgHeight);
//            imageView.setMaxWidth(myMaxImgWidth);
            imageView.setAdjustViewBounds(true);
            imageView.setImageURI(myLatestImgUri);

            //formattedViewer.addView(imageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(size.x*0.5)));
            imageView.setPadding(0,12,0,12);
            formattedViewer.addView(imageView, workingIndex);
        }
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
                changeHeaderButtonValue(lineData.get(workingIndex).headerSize);
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

    @Override
    public void onBackPressed() {
        if(headerSelectView.getVisibility() == View.VISIBLE) {
            headerButton.performClick();
        } else if (titleEditor.getVisibility() == View.VISIBLE) {
            saveTitleEdit();
        } else {
            super.onBackPressed();
        }
    }

    private void formatLines(int startIndex){
        if(startIndex > 0)
            setPadding(lineData.get(startIndex-1));
        else
            setPadding(null);
        for(int i = startIndex; i < lineData.size(); i++){

            TextView newView = (TextView) findViewById(i);
            LineObject lo = lineData.get(i);
            lo.printLineObject(this, newView);
            setPadding(lo);
        }

    }

    private TextView createNewTextView(int index){
        TextView temp = new TextView(this);
        temp.setId(index);
        temp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        temp.setMinHeight((int) textViewHeight);
        temp.setOnClickListener(onClickLineListener);
        temp.setOnTouchListener(onTouchLineListener);
        return temp;
    }


    private void setPadding(LineObject lo){
        if(lo == null){
            pad1 = 0;
            pad2 = 0;
            pad3 = 0;
        }else{
            pad1 = lo.pad1;
            pad2 = lo.pad2;
            pad3 = lo.pad3;
        }

    }

    private void changeHeaderButtonValue(int val){
        //Log.d("VAL", val + "");
        switch (val){
            case 0:
                headerButton.setImageResource(R.drawable.ic_text_select_black);
//                headerButton.setScaleX((float)1);
//                headerButton.setScaleY((float)1);
                headerButton.setAlpha((float)1);
                break;
            case 1:
                headerButton.setImageResource(R.drawable.ic_text_select);
//                headerButton.setScaleX((float)0.7);
//                headerButton.setScaleY((float)0.7);
                headerButton.setAlpha((float)1);
                break;
            case 2:
                headerButton.setImageResource(R.drawable.ic_text_select);
//                headerButton.setScaleX((float)0.9);
//                headerButton.setScaleY((float)0.9);
                headerButton.setAlpha((float)0.8);
                break;
            case 3:
                headerButton.setImageResource(R.drawable.ic_text_select);
//                headerButton.setScaleX((float)1);
//                headerButton.setScaleY((float)1);
                headerButton.setAlpha((float)0.6);
                break;
        }
    }

    private void save() {
        save(null);
    }

    private void save(final String destination){
        String result = "";
        for(int i = 0; i < lineData.size(); i++){
            result += lineData.get(i).toString();
        }
        //dir, final String newFileName, final String contents
        try {
            dataWriter.editFile(myNoteDir, myNoteName, destination, result);
            dirty = false;
            //Log.d("saved", "save: ");
        }catch (Exception e){
            e.printStackTrace();
            Snackbar.make(formattedViewer, "Error saving note", Snackbar.LENGTH_SHORT).show();
        }

        //Log.d("RESULT", result);
    }

    private void saveTitleEdit() {
        save(titleEditor.getText().toString());
        myNoteName = titleEditor.getText().toString();
        titleView.setText(myNoteName);
        titleEditor.setVisibility(View.GONE);
        titleView.setVisibility(View.VISIBLE);
    }

}
