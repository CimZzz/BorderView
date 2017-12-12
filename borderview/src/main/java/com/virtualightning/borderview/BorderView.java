package com.virtualightning.borderview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.inputmethod.EditorInfo.IME_ACTION_NONE;

/**
 * Created by CimZzz(王彦雄) on 12/11/17.<br>
 * Project Name : Virtual-Lightning BorderView<br>
 * Since : BorderView_0.0.1<br>
 * Description:<br>
 * 描述
 */
public class BorderView extends View {
    public static final int SHAPE_POSITIVE = 0;
    public static final int SHAPE_IRREGULAR = 1;

    public static final int GRAVITY_CENTER = 0;
    public static final int GRAVITY_START = 1;
    public static final int GRAVITY_END = 2;

    public static final int INPUT_NUMBER = 0;
    public static final int INPUT_TEXT = 1;

    /**
     * 格子数，默认4个
     */
    private int boxCount = 4;

    /**
     * 格子是否为正（高度与宽度相等，以宽度值为准），默认为正
     */
    private int boxShape = SHAPE_POSITIVE;
    /**
     * 格子圆角值，默认为0
     */
    private float boxRadius = 0;
    /**
     * 格子宽度，默认为0
     */
    private float boxWidth = 0;
    /**
     * 格子高度，默认为0
     */
    private float boxHeight = 0;
    /**
     * 格子边框宽度，默认为10像素
     */
    private float boxBorderWidth = 10;
    /**
     * 格子边框颜色，默认为灰色
     */
    private int boxBorderColor = 0xFFD1D1D1;
    /**
     * 格子激活时边框颜色，默认为橙色
     */
    private int boxBorderColorActive = 0xFFE47F47;
    /**
     * 格子之间间隙，默认为6像素
     */
    private float boxGap = 6;
    /**
     * 根据格子数量自动调整格子尺寸，默认为否
     */
    private boolean boxAutoSize = false;

    /**
     * 文字尺寸，默认为 40px
     */
    private float textSize = 40;
    /**
     * 文字颜色，默认为黑色
     */
    private int textColor = Color.BLACK;

    /**
     * 游标颜色，默认为橙色
     */
    private int cursorColor = 0xFFE47F47;
    /**
     * 游标宽度，默认为5像素
     */
    private float cursorWidth = 5;
    /**
     * 游标间隙，默认为5像素
     */
    private float cursorGap = 5;
    /**
     * 游标位置，默认为中心
     */
    private int cursorGravity = GRAVITY_CENTER;

    /**
     * 输入法设置
     */
    private int inputType = INPUT_NUMBER;

    private float textWidth;
    private float textHeight;

    private Paint paint = new Paint();
    private RectF rectF = new RectF();

    private InputMethodManager inputMethodManager;

    private List<BoxItem> boxList;
    private int activeIndex = -1;
    private int touchDownIndex;
    private boolean showCursorFlag = true;
    private Timer cursorTimer;
    private TimerTask cursorTimerTask;

    private OnCompletedInputListener listener;


    public BorderView(Context context) {
        super(context);
        init(context);
    }

    public BorderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.BorderView);
        boxCount = ta.getInteger(R.styleable.BorderView_boxCount,boxCount);
        boxShape = ta.getInteger(R.styleable.BorderView_boxShape,boxShape);
        boxRadius = ta.getDimension(R.styleable.BorderView_boxRadius,boxRadius);
        boxWidth = ta.getDimension(R.styleable.BorderView_boxWidth,boxWidth);
        boxHeight = ta.getDimension(R.styleable.BorderView_boxHeight,boxHeight);
        boxBorderWidth = ta.getDimension(R.styleable.BorderView_boxBorderWidth,boxBorderWidth);
        boxBorderColor = ta.getColor(R.styleable.BorderView_boxBorderColor,boxBorderColor);
        boxBorderColorActive = ta.getColor(R.styleable.BorderView_boxBorderColorActive,boxBorderColorActive);
        boxGap = ta.getDimension(R.styleable.BorderView_boxGap,boxGap);
        boxAutoSize = ta.getBoolean(R.styleable.BorderView_boxAutoSize,boxAutoSize);

        textSize = ta.getDimension(R.styleable.BorderView_textSize,textSize);
        textColor = ta.getColor(R.styleable.BorderView_textColor,textColor);

        cursorColor = ta.getColor(R.styleable.BorderView_cursorColor,cursorColor);
        cursorWidth = ta.getDimension(R.styleable.BorderView_cursorWidth,cursorWidth);
        cursorGap = ta.getDimension(R.styleable.BorderView_cursorGap,cursorGap);
        cursorGravity = ta.getInteger(R.styleable.BorderView_cursorGravity,cursorGravity);

        inputType = ta.getInteger(R.styleable.BorderView_inputType,inputType);
        ta.recycle();

        init(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        resizeBoxInfo();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchDownIndex = -1;
                for(int i = 0 ; i < boxCount ; i ++) {
                    BoxItem item = boxList.get(i);
                    if(item.isContain(x,y)) {
                        touchDownIndex = i;
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(touchDownIndex == -1)
                    break;
                BoxItem item = boxList.get(touchDownIndex);
                if(item.isContain(x,y)) {
                    activeIndex = touchDownIndex;
                    startCursorTimer();
                    requestFocus();
                    showSoftInput();
                    postInvalidate();
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for(int i = 0 ; i < boxCount ; i ++) {
            BoxItem item = boxList.get(i);

            boolean isActive = i == activeIndex;
            boolean isHasValue = item.hasValue();

            if (isActive || isHasValue) {
                paint.setColor(boxBorderColorActive);
            } else {
                paint.setColor(boxBorderColor);
            }

            rectF.set(item.left, item.top, item.right, item.bottom);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(boxBorderWidth);
            canvas.drawRoundRect(rectF,boxRadius,boxRadius,paint);
            if (isHasValue) {
                paint.setColor(textColor);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(item.value, item.centerX, item.textY, paint);
            }

            if(isActive && showCursorFlag) {
                float startX = 0,startY = item.centerY - textHeight * 1.0f / 2;
                if(isHasValue) {
                    startX = item.centerX + textWidth / 2 + cursorGap;
                } else {
                    switch (cursorGravity) {
                        case GRAVITY_CENTER:
                            startX = item.centerX;
                            break;
                        case GRAVITY_START:
                            startX = item.left + cursorGap;
                            break;
                        case GRAVITY_END:
                            startX = item.right - cursorGap;
                            break;
                    }
                }
                paint.setStrokeWidth(cursorWidth);
                paint.setColor(cursorColor);
                canvas.drawLine(startX,startY,startX,startY + textHeight,paint);
            }
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(!gainFocus) {
            activeIndex = -1;
            showCursorFlag = false;
            stopCursorTimer();
            hideSoftInput();
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        activeIndex = -1;
        showCursorFlag = false;
        stopCursorTimer();
        hideSoftInput();
        postInvalidate();
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions = IME_ACTION_NONE;
        switch (inputType) {
            case INPUT_NUMBER:
                outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
                break;
            case INPUT_TEXT:
                outAttrs.inputType = InputType.TYPE_CLASS_TEXT;
                break;
        }
        return new CustomInputConnection(new BaseInputConnection(this,false),false);
    }


    /*修改属性值方法*/
    public void setBoxCount(int count) {
        boxCount = count;
        boxList.clear();
        boxList = new ArrayList<>(boxCount);
        for(int i = 0 ;i < boxCount ; i ++)
            boxList.add(new BoxItem());
        resizeBoxInfo();
    }
    public void setBoxShape(int shape) {
        this.boxShape = shape;
        resizeBoxInfo();
    }
    public void setBoxRadius(float boxRadius) {
        this.boxRadius = boxRadius;
        postInvalidate();
    }
    public void setBoxWidth(float boxWidth) {
        this.boxWidth = boxWidth;
        resizeBoxInfo();
    }
    public void setBoxHeight(float boxHeight) {
        this.boxHeight = boxHeight;
        resizeBoxInfo();
    }
    public void setBoxBorderWidth(float boxBorderWidth) {
        this.boxBorderWidth = boxBorderWidth;
        resizeBoxInfo();
    }
    public void setBoxBorderColor(int color) {
        this.boxBorderColor = color;
        postInvalidate();
    }
    public void setBoxBorderActiveColor(int color) {
        this.boxBorderColorActive = color;
        postInvalidate();
    }
    public void setBoxGap(float boxGap) {
        this.boxGap = boxGap;
        resizeBoxInfo();
    }
    public void setBoxAutoSize(boolean autoSize) {
        this.boxAutoSize = autoSize;
        resizeBoxInfo();
    }
    public void setContent(String content) {
        if(content.length() > boxCount)
            setBoxCount(content.length());

        for(int i = 0 ; i < content.length() ; i ++) {
            BoxItem item = boxList.get(i);
            item.value = String.valueOf(content.charAt(i));
        }

        postInvalidate();
    }
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        resizeBoxInfo();
    }
    public void setTextColor(int color) {
        this.textColor = color;
        postInvalidate();
    }
    public void setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
        postInvalidate();
    }
    public void setCursorWidth(float cursorWidth) {
        this.cursorWidth = cursorWidth;
        postInvalidate();
    }
    public void setCursorGap(float cursorGap) {
        this.cursorGap = cursorGap;
        postInvalidate();
    }
    public void setCursorGravity(int gravity) {
        this.cursorGravity = gravity;
        postInvalidate();
    }
    public void clearContent() {
        for(BoxItem item : boxList)
            item.value = null;
        postInvalidate();
    }
    public void setOnCompletedInputListener(OnCompletedInputListener listener) {
        this.listener = listener;
    }


    /*内部处理方法*/

    public void init(Context context) {
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boxList = new ArrayList<>(boxCount);
        for(int i = 0 ;i < boxCount ; i ++)
            boxList.add(new BoxItem());

        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void resizeBoxInfo() {
        int paddingStart = getPaddingLeft();
        int paddingEnd = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int containerWidth = getMeasuredWidth() - paddingStart - paddingEnd;
        int containerHeight = getMeasuredHeight() - paddingTop - paddingBottom;


        if(boxAutoSize) {
            //自动调整格子尺寸
            float boxGaps = (boxCount - 1) * boxGap;
            float lineWidth = boxCount * 2 * boxBorderWidth;
            boxWidth = (containerWidth - boxGaps - lineWidth) / boxCount;
            boxHeight = containerHeight - boxBorderWidth * 2;
        } else {
            float needWidth = (boxWidth + boxBorderWidth * 2) * boxCount + (boxCount - 1) * boxGap;
            float needHeight = boxHeight + boxBorderWidth * 2;

            if(needWidth > containerWidth) {
                float diffVal = (needWidth - containerWidth) / boxCount;
                boxWidth -= diffVal;
            }
            if(needHeight > containerHeight) {
                float diffVal = needHeight - containerHeight;
                boxHeight -= diffVal;
            }
        }



        switch (boxShape) {
            case SHAPE_POSITIVE:
                if(boxWidth > boxHeight)
                    boxWidth = boxHeight;
                else boxHeight = boxWidth;
                break;
            case SHAPE_IRREGULAR:
                break;
        }

        float needWidth = (boxWidth + boxBorderWidth * 2) * boxCount + (boxCount - 1) * boxGap;
        float needHeight = boxHeight + boxBorderWidth * 2;
        float startX = (getMeasuredWidth() - needWidth) / 2;
        float startY = (getMeasuredHeight() - needHeight) / 2;
        float curX = startX + boxBorderWidth;
        float curY = startY;

        paint.setTextSize(textSize);
        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        textWidth = paint.measureText("0");
        textHeight = metrics.bottom - metrics.ascent;
        for(int i = 0 ; i < boxCount ; i ++) {
            BoxItem item = boxList.get(i);
            item.left = curX;
            item.right = curX + boxWidth;
            item.top = curY;
            item.bottom = curY + boxHeight;
            item.centerX = (item.left + item.right) / 2;
            item.centerY = (item.top + item.bottom) / 2.0f;
            item.textY = item.top + (item.bottom - item.top - metrics.bottom + metrics.top) / 2 - metrics.top;
            curX += boxWidth + boxBorderWidth * 2 + boxGap;
        }
        postInvalidate();
    }

    private void startCursorTimer() {
        if(cursorTimer != null)
            return;

        cursorTimer = new Timer();
        cursorTimerTask = new TimerTask() {
            @Override
            public void run() {
                showCursorFlag = !showCursorFlag;
                postInvalidate();
            }
        };
        cursorTimer.schedule(cursorTimerTask,0,500);
    }

    private void stopCursorTimer() {
        if(cursorTimer == null)
            return;
        cursorTimer.cancel();
        cursorTimerTask.cancel();

        cursorTimer = null;
        cursorTimerTask = null;
    }

    private void showSoftInput() {
        inputMethodManager.showSoftInput(this, 0);
    }

    private void hideSoftInput() {
        if (inputMethodManager.isActive())
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);

    }

    private void accessNewChar(String c) {
        if(c.equals(""))
            return;

        BoxItem item = boxList.get(activeIndex);

        if(item.hasValue())
            return;


        item.value = c;

        StringBuilder builder = new StringBuilder();
        boolean isCompleted = true;
        for(BoxItem boxItem : boxList) {
            if(boxItem.hasValue())
                builder.append(boxItem.value);
            else {
                isCompleted = false;
                break;
            }
        }

        activeIndex ++;
        if(activeIndex == boxCount)
            activeIndex = boxCount - 1;

        postInvalidate();


        if(isCompleted && listener != null)
            listener.onCompletedInput(builder.toString());
    }

    private void accessNewAction(int action) {
        switch (action) {
            case KeyEvent.KEYCODE_DEL:
                BoxItem item = boxList.get(activeIndex);
                item.value = null;
                activeIndex--;
                if(activeIndex < 0)
                    activeIndex = 0;

                postInvalidate();
                break;
            case KeyEvent.KEYCODE_0:
                accessNewChar("0");
                break;
            case KeyEvent.KEYCODE_1:
                accessNewChar("1");
                break;
            case KeyEvent.KEYCODE_2:
                accessNewChar("2");
                break;
            case KeyEvent.KEYCODE_3:
                accessNewChar("3");
                break;
            case KeyEvent.KEYCODE_4:
                accessNewChar("4");
                break;
            case KeyEvent.KEYCODE_5:
                accessNewChar("5");
                break;
            case KeyEvent.KEYCODE_6:
                accessNewChar("6");
                break;
            case KeyEvent.KEYCODE_7:
                accessNewChar("7");
                break;
            case KeyEvent.KEYCODE_8:
                accessNewChar("8");
                break;
            case KeyEvent.KEYCODE_9:
                accessNewChar("9");
                break;
        }
    }

    /*内部类*/

    public interface OnCompletedInputListener {
        void onCompletedInput(String inputContent);
    }

    private class BoxItem {
        private float left;
        private float right;
        private float top;
        private float bottom;
        private float centerX;
        private float centerY;
        private float textY;

        private String value;

        private boolean isContain(float x ,float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        private boolean hasValue() {
            return value != null;
        }
    }


    private class CustomInputConnection extends InputConnectionWrapper{

        /**
         * Initializes a wrapper.
         * <p>
         * <p><b>Caveat:</b> Although the system can accept {@code (InputConnection) null} in some
         * places, you cannot emulate such a behavior by non-null {@link InputConnectionWrapper} that
         * has {@code null} in {@code target}.</p>
         *
         * @param target  the {@link InputConnection} to be proxied.
         * @param mutable set {@code true} to protect this object from being reconfigured to target
         *                another {@link InputConnection}.  Note that this is ignored while the target is {@code null}.
         */
        public CustomInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }



        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            Log.d("TEST",text.toString());
            accessNewChar(text.toString());
            return super.commitText(text, newCursorPosition);
        }



        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if(event.getAction() == KeyEvent.ACTION_UP)
                accessNewAction(event.getKeyCode());
            return super.sendKeyEvent(event);
        }
    }


}
