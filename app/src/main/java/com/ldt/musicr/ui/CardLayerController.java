package com.ldt.musicr.ui;


import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.CountDownTimer;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ldt.musicr.R;
import com.ldt.musicr.ui.page.CardLayerFragment;
import com.ldt.musicr.ui.widget.gesture.SwipeDetectorGestureListener;
import com.ldt.musicr.util.InterpolatorUtil;
import com.ldt.musicr.util.PlaylistsUtil;
import com.ldt.musicr.util.Tool;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import butterknife.BindView;

public class CardLayerController {
    private static final String TAG = "LayerController";
    public static int SINGLE_TAP_CONFIRM = 1;
    public static int SINGLE_TAP_UP = 3;
    public static int LONG_PRESSED = 2;

    public void onConfigurationChanged(Configuration newConfig) {
        if (activity != null) {
//            Log.d(TAG, "onConfigurationChanged " + newConfig.screenHeightDp * activity.getResources().getDimension(R.dimen.oneDP));
//            oneDp = activity.getResources().getDimension(R.dimen.oneDP);
//            ScreenSize[0] = (int) (oneDp*newConfig.screenWidthDp);
//            ScreenSize[1] = (int) (oneDp*newConfig.screenHeightDp);
//            status_height = Tool.getStatusHeight(activity.getResources());
            Log.d(TAG, "onConfigurationChanged: " + ScreenSize[0] + ", " + ScreenSize[1]);
            //  animateLayerChanged();
        }
    }

    public interface CardLayer {

        /**
         * Ph????ng th???c ???????c g???i khi c?? s??? thay ?????i thu???c t??nh c???a layer (position, state ..)
         * <br>D??ng ph????ng th???c n??y ????? c???p nh???t ui cho layer
         * <br>Note: Kh??ng c??i ?????t s??? ki???n ch???m cho rootView
         * <br> Thay v??o ???? s??? ki???n ch???m s??? ???????c truy???n t???i h??m onTouchParentView
         */
        default void onLayerUpdate(ArrayList<CardLayerAttribute> attrs, ArrayList<Integer> actives, int me) {
        }

        default void onLayerHeightChanged(CardLayerAttribute attr) {
        }

        boolean onTouchParentView(boolean handled);

        View getLayerRootView(Activity activity, ViewGroup viewGroup, int maxPosition);

        default void onAddedToLayerController(CardLayerAttribute attr) {
        }

        /**
         * C??i ?????t kho???ng c??ch gi???a ?????nh layer v?? vi???n tr??n
         * khi layer ?????t v??? tr?? max
         *
         * @return true : full screen, false : below the status bar and below the back_layer_margin_top
         */
        boolean isFullscreenLayer();

        default boolean onBackPressed() {
            return false;
        }

        /**
         * The minimum value of a card layer
         *
         * @return Gi?? tr??? pixel c???a Margin d?????i
         */
        int getLayerMinHeight(Context context, int maxHeight);

        /**
         * Tag nh???m ph??n bi???t gi???a c??c layer
         *
         * @return String tag
         */
        String getCardLayerTag();

        default boolean onGestureDetected(int gesture) {
            return false;
        }
    }

    public AppCompatActivity getActivity() {
        return activity;
    }

    private AppCompatActivity activity;
    public float margin_inDp = 10f;
    public float mMaxMarginTop;
    public float oneDp;
    public int[] ScreenSize = new int[2];
    public float status_height = 0;

    public float bottom_navigation_height;


    // Distance to travel before a drag may begin
    private int mTouchSlop;
    private float mMaxVelocity;
    private float mMinVelocity;

    @BindView(R.id.child_layer_container)
    FrameLayout mChildLayerContainer;

    FrameLayout mLayerContainer;

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView mBottomNavigationView;



    private void bindView(View view) {
        mChildLayerContainer = view.findViewById(R.id.child_layer_container);

        mBottomNavigationView = view.findViewById(R.id.bottom_navigation_view);

    }

    @SuppressLint("ClickableViewAccessibility")
    public CardLayerController(AppCompatActivity activity) {
        this.activity = activity;
        oneDp = Tool.getOneDps(activity);
        mMaxMarginTop = margin_inDp * oneDp;
        ScreenSize[0] = ((AppActivity) activity).mAppRootView.getWidth();
        ScreenSize[1] = ((AppActivity) activity).mAppRootView.getHeight();

        mCardLayerCount = 0;
        mCardLayers = new ArrayList<>();
        mCardLayerAttrs = new ArrayList<>();
        this.status_height = (status_height == 0) ? 24 * oneDp : status_height;

        this.bottom_navigation_height = activity.getResources().getDimension(R.dimen.bottom_navigation_height);

        mTouchListener = (view, motionEvent) -> {
            //Log.d(TAG,"onTouchEvent");
            for (int i = 0; i < mCardLayers.size(); i++)
                if (mCardLayerAttrs.get(i).parent == view)
                    return onTouchEvent(i, view, motionEvent);
            return true;
        };

        final ViewConfiguration vc = ViewConfiguration.get(activity);
        mTouchSlop = vc.getScaledTouchSlop();
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        mMinVelocity = vc.getScaledMinimumFlingVelocity();
        mGestureDetector = new GestureDetector(activity, mGestureListener);

    }

    private long mStart = System.currentTimeMillis();

    private void assign(Object mark) {
        long current = System.currentTimeMillis();
        Log.d(TAG, "logTime: Time " + mark + " = " + (current - mStart));
        mStart = current;
    }

    public void init(FrameLayout layerContainer, CardLayerFragment... fragments) {
        assign(0);
        mLayerContainer = layerContainer;

        //ButterKnife.bind(this,layerContainer);
        bindView(layerContainer);
        assign("bind");
        mCardLayers.clear();

        for (int i = 0; i < fragments.length; i++) {
            addCardLayerFragment(fragments[i], 0);
            assign("add base layer " + i);
        }

        mLayerContainer.setVisibility(View.VISIBLE);
        float value = activity.getResources().getDimension(R.dimen.bottom_navigation_height);

       /* mBottomNavigationParent.setTranslationY(value);
        mBottomNavigationParent.animate().translationYBy(-value);
        */
        for (int i = 0; i < mCardLayerAttrs.size(); i++) {
            mCardLayerAttrs.get(i).expandImmediately();
        }

        assign(3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ObjectAnimator.ofArgb(mLayerContainer, "backgroundColor", 0, 0x11000000).setDuration(350).start();
        } else {
            ObjectAnimator.ofObject(mLayerContainer, "backgroundColor", new ArgbEvaluator(), 0, 0x11000000).setDuration(350).start();
        }
        assign(4);

        if (activity != null) {
            activity.getOnBackPressedDispatcher().addCallback(activity, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    boolean backPressed = onBackPressed();
                    if (!backPressed) {
                        setEnabled(false);
                        if (activity != null) {
                            Dialog dialog1= new MaterialDialog.Builder(getActivity())
                                    .title("Do you want to exit?")
                                    .positiveText("Exit")
                                    .negativeText(android.R.string.cancel)
                                    .onPositive((dialog, which) -> {
                                        activity.getOnBackPressedDispatcher().onBackPressed();
                                    })
                                    .build();
                            dialog1.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
                            dialog1.show();

                        }
                    }
                }
            });
        }
    }


    /**
     * Ph????ng th???c tr??? v??? gi?? tr??? ph???n tr??m scale khi scale view ???? ????? ?????t ???????c hi???u qu???
     * t????ng ???ng nh?? khi ?????t margin ph???i-tr??i l?? marginInPx
     *
     * @param marginInPx
     * @return
     */
    private float convertPixelToScaleX(float marginInPx) {
        return 1 - marginInPx * 2 / ScreenSize[0];
    }

    /**
     * C???p nh???t l???i margin l??c pc = 1 c???a m???i layer
     * ???????c g???i b???t c??? khi n??o m???t pc c???a m???t layer b???t k??? ???????c thay ?????i (s??? ki???n ch???m)
     */
    int mFocusedCardLayerPosition = -1;
    int active_number = 0;

    private void findCurrentFocusLayer() {
        mFocusedCardLayerPosition = -1;

        active_number = 0;
        for (int i = 0; i < mCardLayerCount; i++)
            if (mCardLayerAttrs.get(i).getRuntimePercent() != 0 || mCardLayerAttrs.get(i).mCurrentTranslate == mCardLayerAttrs.get(i).getMaxPosition()) {
                if (active_number == 0) {
                    mFocusedCardLayerPosition = i;
                }
                active_number++;
            }
    }

    private void animateLayerChanged() {

        // C??c layer s??? ???????c update
        // l?? c??c layer kh??ng b??? minimize
        ArrayList<Integer> actives = new ArrayList<>();
        for (int i = 0; i < mCardLayerAttrs.size(); i++) {
            // Reset
            mCardLayerAttrs.get(i).mScaleXY = 1;
            mCardLayerAttrs.get(i).mScaleDeltaTranslate = 0;
            // Only Active Layer
            if (mCardLayerAttrs.get(i).getRuntimePercent() != 0 || mCardLayerAttrs.get(i).mCurrentTranslate == mCardLayerAttrs.get(i).getMaxPosition()) {
                //Log.d(TAG, "animateLayerChanged: runtime : "+mBaseAttrs.get(i).getRuntimePercent());
                actives.add(i);
            } else {
                mCardLayerAttrs.get(i).parent.setScaleX(mCardLayerAttrs.get(i).mScaleXY);
                mCardLayerAttrs.get(i).parent.setScaleY(mCardLayerAttrs.get(i).mScaleXY);
                mCardLayerAttrs.get(i).updateTranslateY();
            }
        }
        // Size
        int activeSize = actives.size();
        //Log.d(TAG, "animateLayerChanged: size = "+activeSize);
//        if(activeSize==1) {
//            mBaseAttrs.get(actives.get(0)).parent.setScaleX(mBaseAttrs.get(actives.get(0)).mScaleXY);
//            mBaseAttrs.get(actives.get(0)).parent.setScaleY(mBaseAttrs.get(actives.get(0)).mScaleXY);
//            mBaseAttrs.get(actives.get(0)).updateTranslateY();
//        }

        // Sau ????y ch??? th???c hi???n t??nh to??n v???i c??c layer hi???n ho???t

        // Gi?? tr??? scale m???i c???a m???i layer theo th??? t???
        // <br>C??c layer ???n kh??ng t??nh

        /*
         *  mScaleXY l?? gi?? tr??? t????ng ???ng khi scale view ????? ?????t hi???u qu???
         *  t????ng t??? khi c??i ?????t vi???n tr??i ????? view n???m c??ch vi???n tr??i ph???i m???t kho???ng c??ch mong mu???n
         */
        float[] scaleXY = new float[activeSize];

        /*
         *  mScaleDeltaTranslate l?? gi?? tr??? c???n ph???i translate view theo tr???c y (sau khi view ???? scale)
         *  ????? ?????nh c???a view c??ch m??n h??nh m???t kho???ng c??ch mong mu???n
         */
        float[] deltaTranslateY = new float[activeSize];

        // Save the percent of the top focus layer (pos 0 )
        float pcOfFocusLayer_End = 1;

        if (activeSize != 0) {
            CardLayerAttribute a = mCardLayerAttrs.get(actives.get(0));

            pcOfFocusLayer_End = a.getPercent();
        }

        for (int item = 1; item < activeSize; item++) {

            // layer tr??n c??ng m???c nhi??n scale = 1 n??n kh??ng c???n ph???i t??nh
            // n??n b??? qua item 0
            // b???t ?????u v??ng l???p t??? item 1
            int position = actives.get(item);

            scaleXY[item] = convertPixelToScaleX((item - 1) * mMaxMarginTop * (1 - pcOfFocusLayer_End)
                    + pcOfFocusLayer_End * item * mMaxMarginTop);

            // khi scale m???t gi?? tr??? l?? scaleXY[item] th?? layer s??? nh??? ??i
            // v?? khi ???? ???? n?? l??m t??ng vi??n tr??n th??m m???t gi?? tr??? trong pixel l??:
            float scale_marginY = ScreenSize[1] * (1 - scaleXY[item]) / 2.0f;

            float need_marginY = 0;
            //item n??y c???n c???ng th??m gi?? tr??? (kho???ng c??ch max - v??? tr?? "chu???n")
            if (item == 1) {
                // Layer n??y kh??c v???i c??c layer kh??c, n?? ph???i ??i t??? v??? tr?? getMaxPositionType() -> margin c???a t????ng ???ng c???a n??
                need_marginY = pcOfFocusLayer_End * (mCardLayerAttrs.get(position).getMaxPosition() - (ScreenSize[1] - status_height - 2 * oneDp - mMaxMarginTop));
            } else
                need_marginY = mCardLayerAttrs.get(position).getMaxPosition() - (ScreenSize[1] - status_height - 2 * oneDp - mMaxMarginTop);


            if (activeSize == 2) {
                need_marginY -= mMaxMarginTop * pcOfFocusLayer_End;
            } else { // activeSize >=3
                need_marginY -= mMaxMarginTop * (item - 1f) / (activeSize - 2f) + pcOfFocusLayer_End * (mMaxMarginTop * item / (activeSize - 1) - mMaxMarginTop * (item - 1) / (activeSize - 2));
            }
            deltaTranslateY[item] = need_marginY - scale_marginY;
            //Log.d(TAG, "updateLayerChanged: item "+item +", delatTranslateY = "+deltaTranslateY[item]);
        }

        // Update UI
        CardLayerAttribute attr;
        for (int item = 1; item < activeSize; item++) {
            attr = mCardLayerAttrs.get(actives.get(item));

            attr.mScaleXY = scaleXY[item];
            attr.mScaleDeltaTranslate = deltaTranslateY[item];
            //Log.d(TAG, "updateLayerChanged: deltaLayer["+item+"] = "+deltaTranslateY[item]);
            // Scale v?? translate nh???ng layer ph??a sau

            TimeInterpolator interpolator = InterpolatorUtil.getInterpolator(7);
            int duration = 650;

            attr.parent.animate().scaleX(attr.mScaleXY).setDuration(duration).setInterpolator(interpolator);
            //Log.d(TAG, "animateLayerChanged: item "+actives.get(item)+" : scaleX from "+attr.parent.getScaleX()+" to "+attr.mScaleXY);
            attr.parent.animate().scaleY(attr.mScaleXY).setDuration(duration).setInterpolator(interpolator);

            //          translationY(getRealTranslateY()).setDuration((long) (350 + 150f/ScreenSize[1]*minPosition)).setInterpolator(Animation.sInterpolator)

            final int item_copy = item;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                attr.parent.animate().translationY(attr.getRealTranslateY()).setDuration(duration).setInterpolator(interpolator).setUpdateListener(animation -> {
                    mCardLayers.get(actives.get(item_copy)).onLayerUpdate(mCardLayerAttrs, actives, item_copy);
                });
            }
        }

    }

    private void updateLayerChanged() {


        // ??i t??? 0 - n
        // Ch??? x??t nh???ng layer c?? pc !=0, g???i l?? layer hi???n ho???t
        // Nh???ng layer c?? pc = 0 s??? b??? b??? qua v?? kh??ng t??nh v??o b??? layer, g???i l?? layer ???n
        // Layer c?? pc !=1 ngh??a l?? ??ang c?? s??? ki???n x???y ra

        // ?????m s??? l?????ng layer hi???n ho???t
        // v?? t??m ra on-top-layer
        // on-top-layer l?? layer ?????u ti??n ???????c t??m th???y c?? pc !=0 ( th?????ng l?? kh??c 1)
        // c??c layer c??n l???i m???c ?????nh c?? pc = 1
        // pc c???a on-top-layer ???nh h?????ng l??n c??c layer kh??c ph??a sau n??
        findCurrentFocusLayer();
        if (mFocusedCardLayerPosition < 0) return;

        int touchLayer = mGestureListener.item;
        // C??c layer s??? ???????c update
        // l?? c??c layer kh??ng b??? minimize
        ArrayList<Integer> actives = new ArrayList<>();
        for (int i = 0; i < mCardLayerAttrs.size(); i++) {
            // Reset
            mCardLayerAttrs.get(i).mScaleXY = 1;
            mCardLayerAttrs.get(i).mScaleDeltaTranslate = 0;
            // Only Active Layer
            if (mCardLayerAttrs.get(i).getState() != CardLayerAttribute.MINIMIZED || mCardLayerAttrs.get(i).mCurrentTranslate == mCardLayerAttrs.get(i).getMaxPosition())
                actives.add(i);
            else {
                mCardLayerAttrs.get(i).parent.setScaleX(mCardLayerAttrs.get(i).mScaleXY);
                mCardLayerAttrs.get(i).parent.setScaleY(mCardLayerAttrs.get(i).mScaleXY);
                mCardLayerAttrs.get(i).updateTranslateY();
            }
        }
        // Size
        int activeSize = actives.size();

        if (activeSize == 1) {
            mCardLayerAttrs.get(actives.get(0)).parent.setScaleX(mCardLayerAttrs.get(actives.get(0)).mScaleXY);
            mCardLayerAttrs.get(actives.get(0)).parent.setScaleY(mCardLayerAttrs.get(actives.get(0)).mScaleXY);
            mCardLayerAttrs.get(actives.get(0)).updateTranslateY();
        }

        // Sau ????y ch??? th???c hi???n t??nh to??n v???i c??c layer hi???n ho???t

        // Gi?? tr??? scale m???i c???a m???i layer theo th??? t???
        // <br>C??c layer ???n kh??ng t??nh

        /*
         *  mScaleXY l?? gi?? tr??? t????ng ???ng khi scale view ????? ?????t hi???u qu???
         *  t????ng t??? khi c??i ?????t vi???n tr??i ????? view n???m c??ch vi???n tr??i ph???i m???t kho???ng c??ch mong mu???n
         */
        float[] scaleXY = new float[activeSize];

        /*
         *  mScaleDeltaTranslate l?? gi?? tr??? c???n ph???i translate view theo tr???c y (sau khi view ???? scale)
         *  ????? ?????nh c???a view c??ch m??n h??nh m???t kho???ng c??ch mong mu???n
         */
        float[] deltaTranslateY = new float[activeSize];

        // Save the percent of the top focus layer (pos 0 )
        float pcOfTopFocusLayer = 1;
        if (activeSize != 0)
            pcOfTopFocusLayer = mCardLayerAttrs.get(actives.get(0)).getRuntimePercent();

        for (int item = 1; item < activeSize; item++) {

            // layer tr??n c??ng m???c nhi??n scale = 1 n??n kh??ng c???n ph???i t??nh
            // n??n b??? qua item 0
            // b???t ?????u v??ng l???p t??? item 1
            int position = actives.get(item);

            scaleXY[item] = convertPixelToScaleX((item - 1) * mMaxMarginTop * (1 - pcOfTopFocusLayer)
                    + pcOfTopFocusLayer * item * mMaxMarginTop);

            // khi scale m???t gi?? tr??? l?? scaleXY[item] th?? layer s??? nh??? ??i
            // v?? khi ???? ???? n?? l??m t??ng vi??n tr??n th??m m???t gi?? tr??? trong pixel l??:
            float scale_marginY = ScreenSize[1] * (1 - scaleXY[item]) / 2.0f;

            float need_marginY = 0;
            //item n??y c???n c???ng th??m gi?? tr??? (kho???ng c??ch max - v??? tr?? "chu???n")
            if (item == 1) {
                // Layer n??y kh??c v???i c??c layer kh??c, n?? ph???i ??i t??? v??? tr?? getMaxPositionType() -> margin c???a t????ng ???ng c???a n??
                need_marginY = pcOfTopFocusLayer * (mCardLayerAttrs.get(position).getMaxPosition() - (ScreenSize[1] - status_height - 2 * oneDp - mMaxMarginTop));
            } else
                need_marginY = mCardLayerAttrs.get(position).getMaxPosition() - (ScreenSize[1] - status_height - 2 * oneDp - mMaxMarginTop);


            if (activeSize == 2) {
                need_marginY -= mMaxMarginTop * pcOfTopFocusLayer;
            } else { // activeSize >=3
                need_marginY -= mMaxMarginTop * (item - 1f) / (activeSize - 2f) + pcOfTopFocusLayer * (mMaxMarginTop * item / (activeSize - 1) - mMaxMarginTop * (item - 1) / (activeSize - 2));
            }
            deltaTranslateY[item] = need_marginY - scale_marginY;
            //Log.d(TAG, "updateLayerChanged: item "+item +", delatTranslateY = "+deltaTranslateY[item]);
        }

        // Update UI
        CardLayerAttribute attr;
        for (int item = 1; item < activeSize; item++) {
            attr = mCardLayerAttrs.get(actives.get(item));

            attr.mScaleXY = scaleXY[item];
            attr.mScaleDeltaTranslate = deltaTranslateY[item];
            //Log.d(TAG, "updateLayerChanged: deltaLayer["+item+"] = "+deltaTranslateY[item]);
            // Scale v?? translate nh???ng layer ph??a sau

            attr.parent.setScaleX(attr.mScaleXY);
            attr.parent.setScaleY(attr.mScaleXY);

            attr.updateTranslateY();
            final int item_copy = item;
            mCardLayers.get(actives.get(item)).onLayerUpdate(mCardLayerAttrs, actives, item_copy);
        }

    }

    private ArrayList<CardLayerFragment> mCardLayers;
    private ArrayList<CardLayerAttribute> mCardLayerAttrs;
    private View.OnTouchListener mTouchListener;

    enum MOVE_DIRECTION {
        NONE,
        MOVE_UP,
        MOVE_DOWN
    }

    private GestureDetector mGestureDetector;
    public SwipeGestureListener mGestureListener = new SwipeGestureListener();

    static class SwipeGestureListener extends SwipeDetectorGestureListener {
        public boolean down = false;
        private boolean flingMasked = false;
        public float assignPosY0;
        public float assignPosX0;
        public boolean handled = true;
        private MOVE_DIRECTION direction;


        private float prevY;

        private boolean isMoveUp() {
            return direction == MOVE_DIRECTION.MOVE_UP;
        }

        private boolean isMoveDown() {
            return direction == MOVE_DIRECTION.MOVE_DOWN;
        }

        @Override
        public boolean onUp(MotionEvent e) {
            down = false;
            if (flingMasked) {
                flingMasked = false;
                //Log.d(TAG, "onUp: fling mask, cancelled handle");
                return false;
            }
            //TODO: when user touch up, what should we do ?
            if (onMoveUp()) {
                if (attr.isBigger1_4())
                    attr.animateToMax();
                else attr.animateToMin();
            } else if (onMoveDown()) {
                if (attr.isSmaller3_4())
                    attr.animateToMin();
                else attr.animateToMax();
            } else {
                if (attr.isSmaller_1_2()) attr.animateToMin();
                else attr.animateToMax();
            }


            return false;
        }

        private boolean onMoveUp() {
            return direction == MOVE_DIRECTION.MOVE_UP;
        }

        private boolean onMoveDown() {
            return direction == MOVE_DIRECTION.MOVE_DOWN;
        }

        @Override
        public boolean onMove(MotionEvent e) {

            if (!down) {
                down = true;
                handled = true;
                prevY = assignPosY0 = e.getRawY();
                assignPosX0 = e.getRawX();
                direction = MOVE_DIRECTION.NONE;
                return handled;
            } else {
                if (!handled) return false;
                float y = e.getRawY();
                if (direction == MOVE_DIRECTION.NONE) {
                    float diffX = Math.abs(e.getRawX() - assignPosX0);
                    float diffY = Math.abs(e.getRawY() - assignPosY0);
                    if (diffX / diffY >= 2) {
                        handled = false;
                        return false;
                    }
                }
                direction = (y > prevY) ? MOVE_DIRECTION.MOVE_DOWN : (y == prevY) ? MOVE_DIRECTION.NONE : MOVE_DIRECTION.MOVE_UP;

//                if (isLayerAvailable()) {
//                    attr.moveTo(attr.mCurrentTranslate - y + prevY);
//                }
                //TODO: When user move and we know the direction, what should we do ?

                prevY = y;
                return handled;
            }
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (direction != MOVE_DIRECTION.NONE) return;
            if (isLayerAvailable()) layer.onGestureDetected(LONG_PRESSED);
        }

        @Override
        public boolean onSwipeTop(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onSwipeTop layer " + item);
            if (isLayerAvailable()) {
                attr.animateToMax();

            }
            return handled;
        }

        @Override
        public boolean onSwipeBottom(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onSwipeBottom layer " + item);
            if (isLayerAvailable()) attr.animateToMin();
            return handled;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            down = true;
            handled = true;
            prevY = assignPosY0 = e.getRawY();
            assignPosX0 = e.getRawX();
            direction = MOVE_DIRECTION.NONE;
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isLayerAvailable()) return layer.onGestureDetected(SINGLE_TAP_UP);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //     Toast.makeText(activity,"single tap confirmed",Toast.LENGTH_SHORT).show();
            if (isLayerAvailable()) return layer.onGestureDetected(SINGLE_TAP_CONFIRM);
            return super.onSingleTapConfirmed(e);
        }
    }


    private boolean onLayerTouchEvent(int i, View view, MotionEvent event) {

        //Log.d(TAG, "event = "+logAction(event));
        view.performClick();
        mGestureListener.setMotionLayer(i, mCardLayers.get(i), mCardLayerAttrs.get(i));
        mGestureListener.setAdaptiveView(view);

        boolean b = mGestureDetector.onTouchEvent(event);
        boolean c = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                c = mGestureListener.onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                c = mGestureListener.onUp(event);
                break;

        }
        Log.d(TAG, "onLayerTouchEvent: b = " + b + ", c = " + c);
        return b || c;
    }

    private String logAction(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return "Down";
            case MotionEvent.ACTION_MOVE:
                return "Move";
            case MotionEvent.ACTION_UP:
                return "UP";
        }
        return "Unsupported";
    }

    /**
     * T???t c??? s??? ki???n ch???m c???a t???t c??? c??c view ???????c x??? l?? trong h??m n??y
     * X??? l?? s??? ki???n c???a m???t view hi???n th???i ??ang x???y ra s??? ki???n ch???m :
     * <br>Capture gestures as slide up, slide down, click ..
     *
     * @param view View ???? g???i s??? ki???n t???i
     * @param event S??? ki???n ch???m
     * @return true n???u s??? ki???n ???????c x??? l??, false n???u s??? ki???n b??? b??? qua
     */
    int currentLayerEvent = -1;
    private int topMargin;
    private int _xDelta;
    private int _yDelta;
    private boolean onDown = true;
    private long timeDown = 0;

    private boolean onTouchEvent(int i, View view, MotionEvent event) {
        return onLayerTouchEvent(i, view, event);

    }

    CountDownTimer countDownTimer;
    boolean inCountDownTime = false;

    /**
     * X??? l?? s??? ki???n nh???n n??t back
     */
    public boolean onBackPressed() {
        /*
         * N???u c?? b???t c??? focusLayer n??o ( focusLayer >=0)
         * Ti???n h??nh g???i l???nh back t???i layer ????, n???u kh??ng th?? ngh??a l?? n?? ??ang trong b??? ?????m delta time
         * N???u n?? kh??ng x??? l?? l???nh back, th?? ti???n h??nh "pop down" n?? ??i
         * N???u n?? l?? layer cu???i c??ng v?? b??? status =="pop down", CardController kh??ng x??? l?? s??? ki???n v?? tr??? v??? false
         */

        /*
         * Find the current focused CardLayer.
         *    +. Found, send back pressed event to it. If it doesn't consume the event, let's minimize the layer.
         *    +. Not found, this means there are zero layer in the card layer controller. We return false to the activity
         */

        findCurrentFocusLayer();
        if (mFocusedCardLayerPosition != -1) {
            final boolean focusedCardConsumesEvent = mCardLayers.get(mFocusedCardLayerPosition).onBackPressed();

            if (focusedCardConsumesEvent) {
                return true;
            }


            if (mFocusedCardLayerPosition < mCardLayerCount - 1) {
                /* we try to minimize the focused card layer */
                mCardLayerAttrs.get(mFocusedCardLayerPosition).animateToMin();
                return true;
            }

        }
        return false;

    }

    /**
     * Gi??? l???p r???ng c?? s??? ki???n ch???m c???a rootView c???a Layer c?? tag l?? tagLayer
     * <br>Truy???n tr???c ti???p s??? ki???n ch???m t???i h??m n??y
     *
     * @param tagLayer
     * @param view
     * @param motionEvent
     * @return
     */

    private int mCardLayerCount = 0;

    public boolean dispatchOnTouchEvent(View view, MotionEvent motionEvent) {
        for (int i = 0; i < mCardLayerCount; i++) {
            if (mCardLayerAttrs.get(i).parent == view) {
                return onTouchEvent(i, view, motionEvent);
            }
        }
        throw new NoSuchElementException("No layer has that view");
    }

    public int getMyPosition(CardLayerAttribute attr) {
        return mCardLayerAttrs.indexOf(attr);
    }

    public class CardLayerAttribute {

        public CardLayerAttribute() {
            mScaleXY = 1;
            mScaleDeltaTranslate = 0;
            upInterpolator = downInterpolator = 4;
            upDuration = 400;
            downDuration = 500;
            initDuration = 1000;
        }

        public float mScaleXY;
        public float mScaleDeltaTranslate = 0;
        public float mCurrentTranslate = 0;
        public static final int MINIMIZED = -1;
        public static final int MAXIMIZED = 1;
        public static final int CAPTURED = 0;

        public int getState() {
            if (minHeight == mCurrentTranslate) return MINIMIZED;
            if (getMaxPosition() == mCurrentTranslate) return MAXIMIZED;
            return CAPTURED;
        }

        public float getPercent() {
            return (mCurrentTranslate - minHeight + 0f) / (getMaxPosition() - minHeight + 0f);
        }

        public float getRuntimePercent() {
            return ((getMaxPosition() - parent.getTranslationY() + mScaleDeltaTranslate) - minHeight + 0f) / (getMaxPosition() - minHeight + 0f);
        }

        public float getRuntimeSelfTranslate() {
            return (getMaxPosition() - parent.getTranslationY() + mScaleDeltaTranslate);
        }

        public boolean isBigger1_4() {
            return (mCurrentTranslate - minHeight) * 4 > (getMaxPosition() - minHeight);
        }

        public boolean isSmaller3_4() {
            return (mCurrentTranslate - minHeight) * 4 < 3 * (getMaxPosition() - minHeight);
        }

        public boolean isSmaller_1_2() {
            return (mCurrentTranslate - minHeight) * 2 < (getMaxPosition() - minHeight);
        }

        public float getRealTranslateY() {
            return getMaxPosition() - mCurrentTranslate + mScaleDeltaTranslate;
        }

        public void animateAndGone() {

        }

        public void animateOnInit() {
            parent.setTranslationY(getMaxPosition());
            parent.animate().translationYBy(-getMaxPosition() + getRealTranslateY()).setDuration((long) (350 + 150f / ScreenSize[1] * minHeight)).setInterpolator(InterpolatorUtil.sInterpolator);
            //  parent.animate().translationYBy(-getMaxPositionType()+getRealTranslateY()).setDuration(computeSettleDuration(0,(int) Math.abs(-getMaxPositionType() + getRealTranslateY()),0,(int)getMaxPositionType())).setInterpolator(Animation.sInterpolator);
            mCurrentTranslate = minHeight;
        }

        public void expandImmediately() {
            parent.setTranslationY(getRealTranslateY());
        }

        public CardLayerAttribute setCurrentTranslate(float current) {
            mCurrentTranslate = current;
            if (mCurrentTranslate > getMaxPosition()) mCurrentTranslate = getMaxPosition();
            return this;
        }

        public void moveTo(float translateY) {
            if (translateY == mCurrentTranslate) return;
            setCurrentTranslate(translateY);
            updateTranslateY();
            if (mGestureListener.isLayerAvailable())
                mCardLayers.get(mGestureListener.item).onLayerHeightChanged(this);
            updateLayerChanged();
        }

        public void shakeOnMax(float _value) {
            float value = 10 * oneDp + _value;
            if (value > 30 * oneDp) value = 30 * oneDp;
            moveTo(mCurrentTranslate - value);
            animateTo(mCurrentTranslate - value);

        }


        public void animateTo(float selfTranslateY) {
            if (selfTranslateY == mCurrentTranslate) return;
            mCurrentTranslate = selfTranslateY;
            final int item = mGestureListener.item;
            if (parent != null) {
                animateLayerChanged();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    parent.animate().translationY(getRealTranslateY()).setDuration((long) (350 + 150f / ScreenSize[1] * minHeight)).setInterpolator(InterpolatorUtil.sInterpolator)
                            .setUpdateListener(animation -> {
                                if (item != -1)
                                    mCardLayers.get(item).onLayerHeightChanged(CardLayerAttribute.this);
                            });
                } else {
                    ObjectAnimator oa = ObjectAnimator.ofFloat(parent, View.TRANSLATION_Y, getRealTranslateY()).setDuration((long) (350 + 150f / ScreenSize[1] * minHeight));
                    oa.addUpdateListener(animation -> {
                        if (item != -1)
                            mCardLayers.get(item).onLayerHeightChanged(CardLayerAttribute.this);
                    });
                    oa.setInterpolator(InterpolatorUtil.sInterpolator);
                    oa.start();
                }
            }
        }

        public void animateTo(float selfTranslateY, float velocityY) {
            if (selfTranslateY == mCurrentTranslate) return;
            mCurrentTranslate = selfTranslateY;
            if (parent != null) {
                parent.animate().translationY(getRealTranslateY()).setDuration((long) (350 + 150f / ScreenSize[1] * minHeight)).setInterpolator(InterpolatorUtil.sInterpolator);
            }
        }

        private int computeSettleDuration(int dx, int dy, int xvel, int yvel) {
            xvel = clampMag(xvel, (int) mMinVelocity, (int) mMaxVelocity);
            yvel = clampMag(yvel, (int) mMinVelocity, (int) mMaxVelocity);
            final int absDx = Math.abs(dx);
            final int absDy = Math.abs(dy);
            final int absXVel = Math.abs(xvel);
            final int absYVel = Math.abs(yvel);
            final int addedVel = absXVel + absYVel;
            final int addedDistance = absDx + absDy;

            final float xweight = xvel != 0 ? (float) absXVel / addedVel :
                    (float) absDx / addedDistance;
            final float yweight = yvel != 0 ? (float) absYVel / addedVel :
                    (float) absDy / addedDistance;

            int xduration = computeAxisDuration(dx, xvel, 0);
            int yduration = computeAxisDuration(dy, yvel, (int) Math.abs(getMaxPosition() - minHeight));

            return (int) (xduration * xweight + yduration * yweight);
        }

        private int computeAxisDuration(int delta, int velocity, int motionRange) {
            if (delta == 0) {
                return 0;
            }

            final int width = ScreenSize[0];
            final int halfWidth = width / 2;
            final float distanceRatio = Math.min(1f, (float) Math.abs(delta) / width);
            final float distance = halfWidth + halfWidth *
                    distanceInfluenceForSnapDuration(distanceRatio);

            int duration;
            velocity = Math.abs(velocity);
            if (velocity > 0) {
                duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
            } else {
                final float range = (float) Math.abs(delta) / motionRange;
                duration = (int) ((range + 1) * BASE_SETTLE_DURATION);
            }
            return Math.min(duration, MAX_SETTLE_DURATION);
        }

        private float distanceInfluenceForSnapDuration(float f) {
            f -= 0.5f; // center the values about 0.
            f *= 0.3f * Math.PI / 2.0f;
            return (float) Math.sin(f);
        }

        private static final int BASE_SETTLE_DURATION = 256; // ms
        private static final int MAX_SETTLE_DURATION = 600; // ms

        /**
         * Clamp the magnitude of value for absMin and absMax.
         * If the value is below the minimum, it will be clamped to zero.
         * If the value is above the maximum, it will be clamped to the maximum.
         *
         * @param value  Value to clamp
         * @param absMin Absolute value of the minimum significant value to return
         * @param absMax Absolute value of the maximum value to return
         * @return The clamped value with the same sign as <code>value</code>
         */
        private int clampMag(int value, int absMin, int absMax) {
            final int absValue = Math.abs(value);
            if (absValue < absMin) return 0;
            if (absValue > absMax) return value > 0 ? absMax : -absMax;
            return value;
        }

        public void animateToMax() {
            mGestureListener.item = getMyPosition(this);
            animateTo(getMaxPosition());
        }

        public void animateToMin() {
            mGestureListener.item = getMyPosition(this);
            animateTo(minHeight);
        }

        public void updateTranslateY() {
            if (parent != null) parent.setTranslationY(getRealTranslateY());
//            if(parent instanceof DarkenAndRoundedBackgroundContraintLayout) {
//                RoundColorable roundable = (RoundColorable)parent;
//                if(minPosition!=getMaxPositionType())
//                    roundable.setRoundNumber(getPercent(),true);
//                else {
//                    float pc = mCurrentTranslate/getMaxPositionType();
//                    //Log.d(TAG, "updateTranslateY: "+mCurrentTranslate+" of "+getMaxPositionType()+" : "+(1-pc));
//
//                    roundable.setRoundNumber(1-pc, true);
//                }
//            }
        }

        public String Tag;
        public float minHeight;
        public int upInterpolator;
        public int downInterpolator;
        public int upDuration;
        public int downDuration;
        public int initDuration;

        public View getParent() {
            return parent;
        }

        public View parent;


        public float getScaleXY() {
            return mScaleXY;
        }

        public float getScaleDeltaTranslate() {
            return mScaleDeltaTranslate;
        }

        public CardLayerAttribute setScaleDeltaTranslate(float scaleDeltaTranslate) {
            mScaleDeltaTranslate = scaleDeltaTranslate;
            return this;
        }

        public String getTag() {
            return Tag;
        }

        public CardLayerAttribute setTag(String tag) {
            Tag = tag;
            return this;
        }

        public float getMinHeight() {
            return minHeight;
        }


        public CardLayerAttribute setMinHeight(float value) {
            this.minHeight = value;
            return this;
        }

        public int getUpInterpolator() {
            return upInterpolator;
        }

        public CardLayerAttribute setUpInterpolator(int upInterpolator) {
            this.upInterpolator = upInterpolator;
            return this;
        }

        public int getDownInterpolator() {
            return downInterpolator;
        }

        public CardLayerAttribute setDownInterpolator(int downInterpolator) {
            this.downInterpolator = downInterpolator;
            return this;
        }

        public int getUpDuration() {
            return upDuration;
        }

        public CardLayerAttribute setUpDuration(int upDuration) {
            this.upDuration = upDuration;
            return this;
        }

        public int getDownDuration() {
            return downDuration;
        }

        public CardLayerAttribute setDownDuration(int downDuration) {
            this.downDuration = downDuration;
            return this;
        }

        public int getInitDuration() {
            return initDuration;

        }

        public CardLayerAttribute setInitDuration(int initDuration) {
            this.initDuration = initDuration;
            return this;
        }

        public CardLayerAttribute set(CardLayer l) {
            this.setTag(l.getCardLayerTag())
                    .setMinHeight(l.getLayerMinHeight(activity, ScreenSize[1]))
//                    .setMaxPosition(l.isFullscreenLayer())
                    .setCurrentTranslate(this.getMinHeight());

            return this;
        }

        public CardLayerAttribute attachView(View view) {
            if (parent != null) parent.setOnTouchListener(null);
            parent = view;
            parent.setOnTouchListener(mTouchListener);
            return this;
        }

        private boolean mM = true;

        public int getMaxPosition() {
            if (mM) return ScreenSize[1];
            else return (int) (ScreenSize[1] - status_height - 2 * oneDp - mMaxMarginTop);
        }

        public CardLayerAttribute setMaxPosition(boolean m) {
            mM = m;
            return this;
        }
    }

    /**
     * C??i ?????t v??? tr?? ban ?????u v?? k??ch c??? cho layer
     * Th???c hi???n hi???u ???ng ????a layer t??? d?????i c??ng l??n t???i v??? tr?? minPosition ( pc = 0)
     * h??m initLayer ???????c th???c hi???n m???t l???n, l??c n?? ???????c ch??n v??o controller
     *
     * @param i
     */
    private void initLayer(int i) {
        CardLayerFragment layer = mCardLayers.get(i);
        CardLayerAttribute attr = mCardLayerAttrs.get(i);
        attr.set(layer);
        attr.attachView(layer.getLayerRootView(activity, mChildLayerContainer, (int) attr.getMaxPosition()));

        activity.getSupportFragmentManager().beginTransaction().add(mChildLayerContainer.getId(), layer).commitNow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            attr.parent.setElevation(0);
        }
        layer.onAddedToLayerController(attr);
    }


    /**
     * Th???c hi???n hi???u ???ng lo???i b??? layer ra kh???i controller
     *
     * @param i
     */
    private void removeCardLayerInternal(int i) {

    }

    /**
     * add fragment to the last of stack
     *
     * @param cardLayerFragment
     */
    public CardLayerAttribute addCardLayerFragment(CardLayerFragment cardLayerFragment) {
        return addCardLayerFragment(cardLayerFragment, mCardLayerCount);
    }

    public CardLayerAttribute addCardLayerFragment(CardLayerFragment cardLayerFragment, int index) {
        int p = Math.min(index, mCardLayerCount);
        CardLayerAttribute attribute = new CardLayerAttribute();
        if (mCardLayers.size() > index) {
            mCardLayers.add(index, cardLayerFragment);
            mCardLayerAttrs.add(index, attribute);
        } else {
            mCardLayers.add(cardLayerFragment);
            mCardLayerAttrs.add(attribute);
        }

        mCardLayerCount++;

        cardLayerFragment.setCardLayerController(this);
        initLayer(p);
        findCurrentFocusLayer();
        return attribute;
    }

    public void removeCardLayer(String tag) {
        for (int i = 0; i < mCardLayerCount; i++)
            if (tag.equals(mCardLayerAttrs.get(i).Tag)) {
                removeCardLayerInternal(i);
                return;
            }
    }

    public CardLayerAttribute getMyAttr(CardLayer l) {
        int pos = mCardLayers.indexOf(l);
        if (pos != -1) return mCardLayerAttrs.get(pos);
        return null;
    }

    public int getMyPosition(@NonNull CardLayer l) {
        return mCardLayers.indexOf(l);
    }

}
