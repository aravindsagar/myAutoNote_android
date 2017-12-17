package paradigm.shift.myautonote.data_model;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.widget.TextView;

import paradigm.shift.myautonote.R;

/**
 * Class to represent a single line of a note.
 * Created by Drew on 11/20/2017.
 */

public class LineObject {

    public boolean imageType = false;

    public int index;
    public String content;
    public int headerSize = 0;
    public int manualHeaderSize = -1;
    private int vocab = -1;

    public int pad1 = 0;
    public int pad2 = 0;
    public int pad3 = 0;

    private boolean working = false;

    public LineObject(int idx, String c, int p1, int p2, int p3, boolean w, boolean it){
        index = idx;
        content = c;


        pad1 = p1;
        pad2 = p2;
        pad3 = p3;

        working = w;
        imageType = it;

        isHeader();
    }

    public void copyPaddingFromPreviousLine(LineObject other){
        if(other == null){
            pad1 = 0;
            pad2 = 0;
            pad3 = 0;
        }else{
            pad1 = other.pad1;
            pad2 = other.pad2;
            pad3 = other.pad3;
        }

    }

    public void setPadding(int p1, int p2, int p3){
        pad1 = p1;
        pad2 = p2;
        pad3 = p3;
    }

    public void endWork(TextView view){
        view.setBackgroundColor(0x00000000);
        working = false;
    }



    public void printLineObject(Context context, TextView output){
//        Log.d("Line: " + index, manualHeaderSize + "");
        vocab = -1;



        if(manualHeaderSize < 0){
            headerSize = 0;
            isTitle();
        }else{
            headerSize = manualHeaderSize;
        }



        //valueTV.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        //valueTV.setTextSize(16);
        //valueTV.setPadding(50,0,0,0);
        //valueTV.setTypeface(Typeface.DEFAULT_BOLD);

        int [] fontSizeTypes = { 14, 22, 20, 18, 17, 17, 17 };



        int color = R.color.textColor;

        if(content.trim().length() == 0){
            if(working){
                output.setText(R.string.new_line);
                output.setTypeface(null, Typeface.ITALIC);
            }
        }

        if(working){
            output.setBackgroundColor(ContextCompat.getColor(context, R.color.textHighlight));
        }

        int paddingLeft = 0;
        int paddingTop = 0;
        if(headerSize > 0){
            if(headerSize != 3)
                paddingLeft += pad3;
            if(headerSize != 2)
                paddingLeft += pad2;
            if(headerSize != 1)
                paddingLeft += pad1;
            paddingTop = 50;
        }else
            paddingLeft += pad1+pad2+pad3;

        if (headerSize > 0) {
            color = R.color.textHeaderColor;
            double opacity = 1 - (0.1) * headerSize;

            if (headerSize == 1) {
                //this.paddingLevel == [];
                //data.padding = "";
                pad3 = 5;
                pad1 = 0;
                pad2 = 0;
            }
            if (headerSize == 2) {
                pad2 = 3;
                pad1 = 0;
            }
            if (headerSize == 3) {
                pad1 = 2;
            }

            output.setTypeface(null, Typeface.BOLD);
            output.setAlpha((float)opacity);
        }else{
            output.setTypeface(null, Typeface.NORMAL);
        }

        output.setTextColor(ContextCompat.getColor(context, color));
        output.setTextSize(fontSizeTypes[headerSize]);

        output.setPadding(paddingLeft*15,paddingTop, 0, 0);
        output.setText(Html.fromHtml(content));

        if(headerSize == 1)
            pad3 = 5;

    }

    private void isHeader(){
        int hs = 0;
        headerSize = 0;
        int i = content.length()-1;
        while(i >= 0 && content.charAt(i) == '`'){
            hs++;
            i--;
        }
        content = content.substring(0, content.length()-hs);
        if(hs > 6)
            hs =  6;

            headerSize = hs;
            if(headerSize > 0)
        manualHeaderSize = hs;
    }


    private void isTitle(){
        //console.log(nextContent);
        String[] words;
        int score;

        if(headerSize == 0 && vocab == -1){

            words = content.split(" ");
            score = 0;
            for (String word : words) {
                if (word.length() > 0)
                    score = (this.isWordUperCase(word) || this.containsIntegers(word) ? score + 1 : score);
            }
            if((double)score/words.length >= 0.6){
                //if(nextIsHeader)
                //data.headerSize = 3;
                //  else
                if(pad3 > 0){
                    if(pad2 > 0)
                        headerSize = 2;
                    else
                        headerSize = 2;

                }else
                    headerSize = 2;



                //data.headerSize = 2;

            }

        }

    }

    private boolean isWordUperCase(String input){
        return input.charAt(0) >= 65 && input.charAt(0) <= 90;
    }

    private boolean containsIntegers(String input){
        return input.charAt(0) >= 48 && input.charAt(0) <= 57;
    }

    public String toString(){
        if(imageType){
            return String.format("<p>%s</img>", content);
        }else{
            StringBuilder result = new StringBuilder("<p>").append(content);
            for(int i = 0; i < headerSize; i++){
                result.append("`");
            }
            result.append("</p>");
            return result.toString();
        }

    }
}

