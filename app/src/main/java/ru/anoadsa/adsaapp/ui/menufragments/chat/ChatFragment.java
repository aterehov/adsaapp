package ru.anoadsa.adsaapp.ui.menufragments.chat;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.databinding.FragmentChatBinding;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.IncidentChatMessage;
import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;
import ru.anoadsa.adsaapp.ui.activities.menu.MenuSharedViewModel;
import ru.anoadsa.adsaapp.ui.views.ChatMessageView;

public class ChatFragment extends UiMenuFragment<ChatViewModel, FragmentChatBinding> {
    private MenuSharedViewModel menuSharedVM;
    private ChatSharedViewModel sharedVM;

//    private boolean wasRestored;

    private ConstraintLayout chatMessagesCL;
    private EditText chatMessageInput;
    private Button chatSendButton;

    private NestedScrollView chatScrollView;
    private ExtendedFloatingActionButton chatDownFAB;

    private ArrayList<ChatMessageView> cmvList = new ArrayList<ChatMessageView>();
//    @Override
//    protected void setViewBinding(Class<FragmentChatBinding> vbclass) {
//        super.setViewBinding(vbclass);
//    }

//    @Override
//    protected void getRoot(@NonNull LayoutInflater inflater, ViewGroup container) {
//        super.getRoot(inflater, container);
//    }

    private int newMessagesNumber = 0;
    private boolean firstMessageUpdate = true;
    private boolean checkNewMessagesOnResume = true;

    private boolean restoreState;
    private int scrollOnRestore;
    private int lastVisibleMessageOnRestore;
    private int newMessagesNumberOnRestore;

    @Override
    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.initUi(inflater, container);
        chatMessagesCL = binding.chatMessagesCL;
        chatMessageInput = binding.chatMessageInput;
        chatSendButton = binding.chatSendButton;

        chatScrollView = binding.chatScrollView;
        chatDownFAB = binding.chatDownFAB;
    }

    @Override
    protected void configureUiState() {
        chatDownFAB.hide();
    }

    private void refreshFabCounter() {
        if (newMessagesNumber == 0) {
//            chatDownFAB.setText("");
            chatDownFAB.shrink();
        } else {
            chatDownFAB.setText(Static.getNewMessagesText(newMessagesNumber));
            chatDownFAB.extend();
        }
    }

    private boolean isScrolledToBottom() {
        return chatMessagesCL.getMeasuredHeight() <=
                chatScrollView.getScrollY() + chatScrollView.getHeight();
    }

    private int getScrollDelta() {
        if (cmvList.isEmpty()) {
            return 0;
        }
        return cmvList.get(cmvList.size() - 1).getBottom()
                + chatScrollView.getPaddingBottom()
                - chatScrollView.getScrollY()
                - chatScrollView.getHeight();
    }

    private boolean isViewVisible(@NonNull View view) {
        Rect scrollBounds = new Rect();
        chatScrollView.getHitRect(scrollBounds);
//        if (view.getLocalVisibleRect(scrollBounds)) {
//            // Any portion of the imageView, even a single pixel, is within the visible window
//        } else {
//            // NONE of the imageView is within the visible window
//        }
        return view.getLocalVisibleRect(scrollBounds);
    }

    private int getLastVisibleMessage() {
        boolean prevVisible = false;
        boolean thisVisible = false;
        int i;
        for (i = 0; i < cmvList.size(); ++i) {
            prevVisible = thisVisible;
            thisVisible = isViewVisible(cmvList.get(i));
            if (prevVisible && !thisVisible) {
                break;
            }
        }
//        if (thisVisible) {
//            return i;
//        } else {
        return i - 1;
//        }
    }

//    @Override
//    protected void setViewModel(Class<ChatViewModel> vmclass) {
//        super.setViewModel(vmclass);
//    }

//    @Override
//    protected void configureViewModel() {
//        super.configureViewModel();
//    }

    // This is updated on every new message addition
    private boolean wasOnBottom = true;
    private boolean wasOnBottomOnRestore;

    // This is set to true on new message sending
    private boolean newMessageSent = false;

    // This is to know if scroll was already applied after state restore
    private boolean scrolledAfterStateRestore = false;

    @NonNull
    @Contract(" -> new")
    private ViewTreeObserver.OnGlobalLayoutListener newDoWhenMessageRendered() {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean run = true;

            @Override
            public void onGlobalLayout() {
                if (run) {
                    run = false;
                    if (
//                            viewModel.getRestoreState()
                    wasRestored
                                    && !scrolledAfterStateRestore) {
                        chatScrollView.scrollTo(
                                0,
                                Integer.max(
                                        0,
//                                        scrollOnRestore
                                        cmvList.get(lastVisibleMessageOnRestore).getBottom()
                                                - chatScrollView.getHeight()
                                )
                        );
                        newMessagesNumber = newMessagesNumberOnRestore;
                        refreshFabCounter();
                        scrolledAfterStateRestore = true;
                        return;
                    }
                    if (wasOnBottom || newMessageSent) {
                        wasOnBottom = true;
                        newMessageSent = false;
                        if (firstMessageUpdate) {
                            firstMessageUpdate = false;
//                        chatScrollView.scroll
//                        chatScrollView.scrollTo(
                            chatScrollView.scrollBy(
                                    0,
//                                chatScrollView.getHeight()
//                                chatScrollView.getMeasuredHeight()
                                    getScrollDelta()
                            );
                        } else {
//                        chatScrollView.smoothScrollTo(
                            chatScrollView.smoothScrollBy(
                                    0,
//                                chatScrollView.getHeight()
//                                chatScrollView.getMeasuredHeight()
                                    getScrollDelta()
                            );
                        }
                        newMessagesNumber = 0;
                    } else {
                        refreshFabCounter();
                    }
                }
            }
        };
    }

    private ViewTreeObserver.OnGlobalLayoutListener doWhenMessageRendered = new ViewTreeObserver.OnGlobalLayoutListener() {
        boolean run = true;

        @Override
        public void onGlobalLayout() {
            if (run) {
                run = false;
                if (wasOnBottom) {
                    if (firstMessageUpdate) {
                        firstMessageUpdate = false;
//                        chatScrollView.scroll
//                        chatScrollView.scrollTo(
                        chatScrollView.scrollBy(
                                0,
//                                chatScrollView.getHeight()
//                                chatScrollView.getMeasuredHeight()
                                getScrollDelta()
                        );
                    } else {
//                        chatScrollView.smoothScrollTo(
                        chatScrollView.smoothScrollBy(
                                0,
//                                chatScrollView.getHeight()
//                                chatScrollView.getMeasuredHeight()
                                getScrollDelta()
                        );
                    }
                    newMessagesNumber = 0;
                } else {
                    refreshFabCounter();
                }
            }
        }
    };

    @Override
    protected void configureUiActions() {
//        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                restoreState = false;
//                this.setEnabled(false);
//            }
//        });

        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatMessageInput.getText() == null || chatMessageInput.getText().length() == 0) {
                    Snackbar.make(chatMessagesCL, "Введите сообщение", Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                chatSendButton.setEnabled(false);
                newMessageSent = true;
                viewModel.sendMessage(chatMessageInput.getText().toString());
                viewModel.setViewedMessageCountInDb(cmvList.size() + 1);
            }
        });

        chatScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY,
                                       int oldScrollX, int oldScrollY) {
                if (isScrolledToBottom()) {
//                    chatDownFAB.setVisibility(View.GONE);
                    chatDownFAB.hide();
                    viewModel.setViewedMessageCountInDb(cmvList.size());
                    viewModel.hideNotification(getContext());
                } else {
//                    chatDownFAB.setVisibility(View.VISIBLE);
                    chatDownFAB.show();
                }
            }
        });

        chatDownFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                chatScrollView.smoothScrollTo(0, chatScrollView.getHeight());
                chatScrollView.smoothScrollBy(0, getScrollDelta());
                newMessagesNumber = 0;
                refreshFabCounter();
            }
        });

        // TODO fix FAB disappearing on screen rotation
//        chatDownFAB.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            private boolean ran;
//
//            @Override
//            public void onGlobalLayout() {
//                if (!ran) {
//                    if (isScrolledToBottom()) {
////                    chatDownFAB.setVisibility(View.GONE);
//                        chatDownFAB.hide();
////                        viewModel.setViewedMessageCountInDb(cmvList.size());
////                        viewModel.hideNotification(getContext());
//                    } else {
////                    chatDownFAB.setVisibility(View.VISIBLE);
//                        chatDownFAB.show();
//                    }
//                    ran = true;
//                }
//            }
//        });
    }

    @Override
    protected void configureViewModelActions() {
        viewModel.getIncident().observe(getViewLifecycleOwner(), new Observer<Incident>() {
            private boolean runBefore;

            @Override
            public void onChanged(Incident incident) {
                if (incident == null) {
                    return;
                }

                if (!incident.isFullInfoLoaded()) {
                    viewModel.loadFullIncidentInfo();
                    return;
                }
                if (!incident.isCanWriteChat()) {
                    chatSendButton.setEnabled(false);
                    chatMessageInput.setEnabled(false);
                    viewModel.stopCheckingNewMessages();
                    // get new messages only once
//                    if (checkNewMessagesOnResume) {
                        // this is done in onresume
//                        viewModel.getNewMessages();
                        checkNewMessagesOnResume = false;
//                        return;
//                    }
                }
                if (incident.getChatMessages() == null) {
                    return;
                }



                if (cmvList == null) {
                    cmvList = new ArrayList<ChatMessageView>();
                }
//                boolean

                if (
//                        viewModel.getRestoreState()
                wasRestored
                                && !runBefore) {
                    wasOnBottom = wasOnBottomOnRestore;
                    runBefore = true;
                } else {
                    wasOnBottom = isScrolledToBottom();
                }
                boolean messagesAdded = cmvList.size() < incident.getChatMessages().size();
                for (int i = cmvList.size(); i < incident.getChatMessages().size(); ++i) {
                    IncidentChatMessage icm = incident.getChatMessages().get(i);
                    ChatMessageView cmv = new ChatMessageView(getContext());

                    cmv.setSentDateTime(icm.getCreatedDateTime());
                    cmv.setSender(icm.getSender());
                    cmv.setSenderType(icm.getSenderIndex());
                    cmv.setMessageText(icm.getContent());

                    cmv.setId(View.generateViewId());

                    ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    if (cmvList.isEmpty()) {
                        lp.topToTop = chatMessagesCL.getId();
                    } else {
                        lp.topToBottom = cmvList.get(cmvList.size() - 1).getId();
                    }
                    if (icm.getSenderIndex() == -1) {
                        lp.startToStart = chatMessagesCL.getId();
                        lp.endToEnd = chatMessagesCL.getId();
                    } else if (icm.getSenderIndex() == 0
                            || icm.getSenderIndex() == 1
                            || icm.getSenderIndex() == 999) {
                        lp.startToStart = chatMessagesCL.getId();
                    } else if (icm.getSenderIndex() == 2) {
                        lp.endToEnd = chatMessagesCL.getId();
                    }

                    cmv.setLayoutParams(lp);

//                    cmv.getViewTreeObserver().addOnGlobalLayoutListener();

                    chatMessagesCL.addView(cmv);

                    cmvList.add(cmv);

                    ++newMessagesNumber;
                }

                if (firstMessageUpdate) {
                    viewModel.setViewedMessageCountInDb(
//                            incident.getMessageCount()
                            cmvList.size()
                    );
                    viewModel.hideNotification(getContext());
                }

                if (messagesAdded) {

                    cmvList.get(cmvList.size() - 1).getViewTreeObserver()
                            .addOnGlobalLayoutListener(newDoWhenMessageRendered());
                }
//                if (wasOnBottom) {
//                    if (firstMessageUpdate) {
//                        firstMessageUpdate = false;
////                        chatScrollView.scroll
////                        chatScrollView.scrollTo(
//                        chatScrollView.scrollBy(
//                                0,
////                                chatScrollView.getHeight()
////                                chatScrollView.getMeasuredHeight()
//                                getScrollDelta()
//                        );
//                    } else {
////                        chatScrollView.smoothScrollTo(
//                        chatScrollView.smoothScrollBy(
//                                0,
////                                chatScrollView.getHeight()
////                                chatScrollView.getMeasuredHeight()
//                                getScrollDelta()
//                        );
//                    }
//                    newMessagesNumber = 0;
//                } else {
//                    refreshFabCounter();
//                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null && !error.isEmpty()) {
                    Snackbar.make(chatMessagesCL, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getMessageSentSuccess().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            private boolean runBefore;

            @Override
            public void onChanged(Boolean success) {
                if (
//                        viewModel.getRestoreState()
                wasRestored
                                && !runBefore) {
                    runBefore = true;
                    return;
                }
                if (success) {
                    chatMessageInput.setText("");
                }
                if (viewModel.getIncident().getValue().isCanWriteChat()) {
                    chatSendButton.setEnabled(true);
                }
            }
        });
    }

    protected void configureSharedViewModelActions() {
        sharedVM.getIncident().observe(getViewLifecycleOwner(), new Observer<Incident>() {
            @Override
            public void onChanged(Incident incident) {
                if (
//                        !viewModel.getRestoreState()
                        !wasRestored
                ) {
                    viewModel.setIncident(incident);
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        wasRestored = savedInstanceState != null;
        sharedVM = new ViewModelProvider(requireActivity()).get(ChatSharedViewModel.class);
        menuSharedVM = new ViewModelProvider(requireActivity()).get(MenuSharedViewModel.class);
        setViewModel(ChatViewModel.class);
        setViewBinding(FragmentChatBinding.class);
        configureSharedViewModelActions();
//        restoreState = true;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        viewModel.setRestoreState(restoreState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        viewModel.setRestoreState(restoreState);
//        if (restoreState) {
        outState.putString("chatMessageInputText", chatMessageInput.getText().toString());
//        outState.putInt("chatScrollViewScrollY", chatScrollView.getScrollY() + chatScrollView.getHeight());
        outState.putInt("lastVisibleMessage", getLastVisibleMessage());
//        chatScrollView.getMeasuredHeight()
        outState.putInt("newMessagesNumber", newMessagesNumber);
        outState.putBoolean("firstMessageUpdate", firstMessageUpdate);
//        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null
                // wasRestored check unneeded, it is the same as savedInstanceState != null
//                &&
//                viewModel.getRestoreState()
//                wasRestored
        ) {
            chatMessageInput.setText(savedInstanceState.getString("chatMessageInputText"));
//            scrollOnRestore = savedInstanceState.getInt("chatScrollViewScrollY");
            lastVisibleMessageOnRestore = savedInstanceState.getInt("lastVisibleMessage");
            newMessagesNumberOnRestore = savedInstanceState.getInt("newMessagesNumber");
            firstMessageUpdate = savedInstanceState.getBoolean("firstMessageUpdate");
            newMessageSent = false;
        }
//        restoreState = true;
    }

    @Override
    public void onPause() {
        viewModel.stopCheckingNewMessages();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
//        viewModel.setRestoreState(true);
//        restoreState = true;
//        if (checkNewMessagesOnResume) {
            viewModel.getNewMessages();
//            if (viewModel.getIncident().getValue().isCanWriteChat()) {
        if (checkNewMessagesOnResume) {
            viewModel.startCheckingNewMessages();
//            } else {
//                checkNewMessagesOnResume = false;
//            }
        }
    }
}
