package in.hridayan.ashell.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import in.hridayan.ashell.BuildConfig;
import in.hridayan.ashell.R;
import in.hridayan.ashell.activities.ChangelogActivity;
import in.hridayan.ashell.activities.ExamplesActivity;
import in.hridayan.ashell.activities.FabExtendingOnScrollListener;
import in.hridayan.ashell.activities.FabOnScrollDownListener;
import in.hridayan.ashell.activities.FabOnScrollUpListener;
import in.hridayan.ashell.activities.SettingsActivity;
import in.hridayan.ashell.adapters.CommandsAdapter;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.adapters.ShellOutputAdapter;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.ShizukuShell;
import in.hridayan.ashell.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 28, 2022
 */
public class aShellFragment extends Fragment {

  private AppCompatAutoCompleteTextView mCommand, mEnterIsSend;
  private AppCompatEditText mSearchWord;
  private AppCompatImageButton mClearButton,
      mHistoryButton,
      mSearchButton,
      mBookMark,
      mBookMarks,
      mSettingsButton;
  private ExtendedFloatingActionButton mSaveButton;
  private SettingsAdapter adapter;
  private FloatingActionButton mTopButton, mBottomButton, mSendButton;
  private SettingsItem settingsList;
  private RecyclerView mRecyclerViewOutput;
  private ShizukuShell mShizukuShell = null;
  private boolean mExit;
  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private int mPosition = 1;
  private List<String> mHistory = null, mResult = null;

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View mRootView = inflater.inflate(R.layout.fragment_ashell, container, false);
    mEnterIsSend = mRootView.findViewById(R.id.shell_command);
    mCommand = mRootView.findViewById(R.id.shell_command);

    List<SettingsItem> settingsList = new ArrayList<>();
    SettingsAdapter adapter = new SettingsAdapter(settingsList, requireContext());

    mSearchWord = mRootView.findViewById(R.id.search_word);
    mSaveButton = mRootView.findViewById(R.id.extended_FabActivity);
    mTopButton = mRootView.findViewById(R.id.fab_up);
    mBottomButton = mRootView.findViewById(R.id.fab_down);
    mClearButton = mRootView.findViewById(R.id.clear);
    mHistoryButton = mRootView.findViewById(R.id.history);
    mSettingsButton = mRootView.findViewById(R.id.settings);
    mSearchButton = mRootView.findViewById(R.id.search);
    mBookMark = mRootView.findViewById(R.id.bookmark);
    mBookMarks = mRootView.findViewById(R.id.bookmarks);
    mSendButton = mRootView.findViewById(R.id.send);
    mRecyclerViewOutput = mRootView.findViewById(R.id.recycler_view_output);
    mRecyclerViewOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));
    mRecyclerViewOutput.addOnScrollListener(new FabExtendingOnScrollListener(mSaveButton));
    mRecyclerViewOutput.addOnScrollListener(new FabOnScrollUpListener(mTopButton));
    mRecyclerViewOutput.addOnScrollListener(new FabOnScrollDownListener(mBottomButton));

    mCommand.requestFocus();
    mBookMarks.setVisibility(
        Utils.getBookmarks(requireActivity()).size() > 0 ? View.VISIBLE : View.GONE);

    mTopButton.setOnClickListener(
        new View.OnClickListener() {
          private long lastClickTime = 0;

          @Override
          public void onClick(View v) {

            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - lastClickTime;

            if (timeDifference < 200) {
              mRecyclerViewOutput.scrollToPosition(0);
            } else {

              boolean switchState = adapter.getSavedSwitchState("Smooth Scrolling");

              if (switchState) {

                mRecyclerViewOutput.smoothScrollToPosition(0);
              } else {
                mRecyclerViewOutput.scrollToPosition(0);
              }
            }

            lastClickTime = currentTime;
          }
        });
    mBottomButton.setOnClickListener(
        new View.OnClickListener() {
          private long lastClickTime = 0;

          @Override
          public void onClick(View v) {

            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - lastClickTime;

            if (timeDifference < 200) {
              mRecyclerViewOutput.scrollToPosition(
                  Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);
            } else {

              boolean switchState = adapter.getSavedSwitchState("Smooth Scrolling");

              if (switchState) {
                mRecyclerViewOutput.smoothScrollToPosition(
                    Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);

              } else {
                mRecyclerViewOutput.scrollToPosition(
                    Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);
              }
            }

            lastClickTime = currentTime;
          }
        });

    mCommand.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @SuppressLint("SetTextI18n")
          @Override
          public void afterTextChanged(Editable s) {
            if (s.toString().contains("\n")) {
              if (!s.toString().endsWith("\n")) {
                mCommand.setText(s.toString().replace("\n", ""));
              }
              initializeShell(requireActivity());
            } else {
              if (mShizukuShell != null && mShizukuShell.isBusy()) {
                return;
              }
              RecyclerView mRecyclerViewCommands =
                  mRootView.findViewById(R.id.recycler_view_commands);
              if (!s.toString().trim().isEmpty()) {
                mSendButton.setImageDrawable(
                    Utils.getDrawable(R.drawable.ic_send, requireActivity()));
                mBookMark.setImageDrawable(
                    Utils.getDrawable(
                        Utils.isBookmarked(s.toString().trim(), requireActivity())
                            ? R.drawable.ic_starred
                            : R.drawable.ic_star,
                        requireActivity()));
                mBookMark.setVisibility(View.VISIBLE);
                mBookMark.setOnClickListener(
                    v -> {
                      if (Utils.isBookmarked(s.toString().trim(), requireActivity())) {
                        Utils.deleteFromBookmark(s.toString().trim(), requireActivity());
                        Utils.snackBar(
                                mRootView,
                                getString(R.string.bookmark_removed_message, s.toString().trim()))
                            .show();
                      } else {
                        Utils.addToBookmark(s.toString().trim(), requireActivity());
                        Utils.snackBar(
                                mRootView,
                                getString(R.string.bookmark_added_message, s.toString().trim()))
                            .show();
                      }
                      mBookMark.setImageDrawable(
                          Utils.getDrawable(
                              Utils.isBookmarked(s.toString().trim(), requireActivity())
                                  ? R.drawable.ic_starred
                                  : R.drawable.ic_star,
                              requireActivity()));
                      mBookMarks.setVisibility(
                          Utils.getBookmarks(requireActivity()).size() > 0
                              ? View.VISIBLE
                              : View.GONE);
                    });
                new Handler(Looper.getMainLooper())
                    .post(
                        () -> {
                          CommandsAdapter mCommandsAdapter;
                          if (s.toString().contains(" ") && s.toString().contains(".")) {
                            String[] splitCommands = {
                              s.toString().substring(0, lastIndexOf(s.toString(), ".")),
                              s.toString().substring(lastIndexOf(s.toString(), "."))
                            };

                            String packageNamePrefix;
                            if (splitCommands[0].contains(" ")) {
                              packageNamePrefix = splitPrefix(splitCommands[0], 1);
                            } else {
                              packageNamePrefix = splitCommands[0];
                            }

                            mCommandsAdapter =
                                new CommandsAdapter(
                                    Commands.getPackageInfo(packageNamePrefix + "."));
                            mRecyclerViewCommands.setLayoutManager(
                                new LinearLayoutManager(requireActivity()));
                            mRecyclerViewCommands.setAdapter(mCommandsAdapter);
                            mRecyclerViewCommands.setVisibility(View.VISIBLE);
                            mCommandsAdapter.setOnItemClickListener(
                                (command, v) -> {
                                  mCommand.setText(
                                      splitCommands[0].contains(" ")
                                          ? splitPrefix(splitCommands[0], 0) + " " + command
                                          : command);
                                  mCommand.setSelection(mCommand.getText().length());
                                  mRecyclerViewCommands.setVisibility(View.GONE);
                                });
                          } else {
                            mCommandsAdapter =
                                new CommandsAdapter(Commands.getCommand(s.toString()));
                            mRecyclerViewCommands.setLayoutManager(
                                new LinearLayoutManager(requireActivity()));
                            mRecyclerViewCommands.setAdapter(mCommandsAdapter);
                            mRecyclerViewCommands.setVisibility(View.VISIBLE);
                            mCommandsAdapter.setOnItemClickListener(
                                (command, v) -> {
                                  if (command.contains(" <")) {
                                    mCommand.setText(command.split("<")[0]);
                                  } else {
                                    mCommand.setText(command);
                                  }
                                  mCommand.setSelection(mCommand.getText().length());
                                });
                          }
                        });
              } else {
                mBookMark.setVisibility(View.GONE);
                mRecyclerViewCommands.setVisibility(View.GONE);
                mSendButton.setImageDrawable(
                    Utils.getDrawable(R.drawable.ic_help, requireActivity()));
                mSendButton.clearColorFilter();
              }
            }
          }
        });

    mEnterIsSend.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
              if (mShizukuShell != null && mShizukuShell.isBusy()) {
                mShizukuShell.destroy();
                mSendButton.setImageDrawable(
                    Utils.getDrawable(R.drawable.ic_help, requireActivity()));
                mSendButton.clearColorFilter();
              } else if (mCommand.getText() == null
                  || mCommand.getText().toString().trim().isEmpty()) {
                Intent examples = new Intent(requireActivity(), ExamplesActivity.class);
                startActivity(examples);
              } else {
                initializeShell(requireActivity());
              }
              return true;
            }
            return false;
          }
        });
    mSendButton.setOnClickListener(
        v -> {
          if (mShizukuShell != null && mShizukuShell.isBusy()) {
            mShizukuShell.destroy();
            mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
            mSendButton.clearColorFilter();

          } else if (mCommand.getText() == null || mCommand.getText().toString().trim().isEmpty()) {
            Intent examples = new Intent(requireActivity(), ExamplesActivity.class);
            startActivity(examples);
          } else {
            initializeShell(requireActivity());
          }
        });

    mSettingsButton.setOnClickListener(
        v -> {
          Intent settingsIntent = new Intent(requireActivity(), SettingsActivity.class);
          startActivity(settingsIntent);
        });

    mClearButton.setOnClickListener(
        v -> {
          if (mResult == null) return;
          if (PreferenceManager.getDefaultSharedPreferences(requireActivity())
              .getBoolean("clearAllMessage", true)) {
            new MaterialAlertDialogBuilder(requireActivity())
                .setIcon(R.mipmap.adb_launcher)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.clear_all_message))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
                .setPositiveButton(
                    getString(R.string.yes),
                    (dialogInterface, i) -> {
                      PreferenceManager.getDefaultSharedPreferences(requireActivity())
                          .edit()
                          .putBoolean("clearAllMessage", false)
                          .apply();
                      clearAll();
                    })
                .show();
          } else {
            clearAll();
          }
        });

    mSearchButton.setOnClickListener(
        v -> {
          if (mHistoryButton.getVisibility() == View.VISIBLE) {
            mHistoryButton.setVisibility(View.GONE);
          }
          if (mClearButton.getVisibility() == View.VISIBLE) {
            mClearButton.setVisibility(View.GONE);
          }
          mBookMarks.setVisibility(View.GONE);
          mSettingsButton.setVisibility(View.GONE);
          mSearchButton.setVisibility(View.GONE);
          mSearchWord.setVisibility(View.VISIBLE);
          mSearchWord.requestFocus();
          mCommand.setText(null);
        });

    mSearchWord.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (s == null || s.toString().trim().isEmpty()) {
              updateUI(mResult);
            } else {
              List<String> mResultSorted = new ArrayList<>();
              for (int i = mPosition; i < mResult.size(); i++) {
                if (mResult.get(i).toLowerCase().contains(s.toString().toLowerCase())) {
                  mResultSorted.add(mResult.get(i));
                }
              }
              updateUI(mResultSorted);
            }
          }
        });

    mBookMarks.setOnClickListener(
        v -> {
          PopupMenu popupMenu = new PopupMenu(requireContext(), mCommand);
          Menu menu = popupMenu.getMenu();
          for (int i = 0; i < Utils.getBookmarks(requireActivity()).size(); i++) {
            menu.add(Menu.NONE, i, Menu.NONE, Utils.getBookmarks(requireActivity()).get(i));
          }
          popupMenu.setOnMenuItemClickListener(
              item -> {
                for (int i = 0; i < Utils.getBookmarks(requireActivity()).size(); i++) {
                  if (item.getItemId() == i) {
                    mCommand.setText(Utils.getBookmarks(requireActivity()).get(i));
                    mCommand.setSelection(mCommand.getText().length());
                  }
                }
                return false;
              });
          popupMenu.show();
        });

    mHistoryButton.setOnClickListener(
        v -> {
          PopupMenu popupMenu = new PopupMenu(requireContext(), mCommand);
          Menu menu = popupMenu.getMenu();
          for (int i = 0; i < getRecentCommands().size(); i++) {
            menu.add(Menu.NONE, i, Menu.NONE, getRecentCommands().get(i));
          }
          popupMenu.setOnMenuItemClickListener(
              item -> {
                for (int i = 0; i < getRecentCommands().size(); i++) {
                  if (item.getItemId() == i) {
                    mCommand.setText(getRecentCommands().get(i));
                    mCommand.setSelection(mCommand.getText().length());
                  }
                }
                return false;
              });
          popupMenu.show();
        });
    mSaveButton.setOnClickListener(
        v -> {
          StringBuilder sb = new StringBuilder();
          for (int i = mPosition; i < mResult.size(); i++) {
            String result = mResult.get(i);
            if (!"aShell: Finish".equals(result) && !"<i></i>".equals(result)) {
              sb.append(result).append("\n");
            }
          }

          boolean saved = false;
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
              ContentValues values = new ContentValues();
              String fileName =
                  mHistory.get(mHistory.size() - 1).replace("/", "-").replace(" ", "") + ".txt";
              values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
              values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
              values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
              Uri uri =
                  requireActivity()
                      .getContentResolver()
                      .insert(MediaStore.Files.getContentUri("external"), values);

              if (uri != null) {
                try (OutputStream outputStream =
                    requireActivity().getContentResolver().openOutputStream(uri)) {
                  outputStream.write(sb.toString().getBytes());
                  saved = true;
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          } else {
            if (requireActivity()
                    .checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
              ActivityCompat.requestPermissions(
                  requireActivity(),
                  new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                  0);
              return;
            }

            try {
              String fileName =
                  mHistory.get(mHistory.size() - 1).replace("/", "-").replace(" ", "") + ".txt";
              File file = new File(Environment.DIRECTORY_DOWNLOADS, fileName);
              Utils.create(sb.toString(), file);
              saved = true;
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          String message =
              saved
                  ? getString(R.string.shell_output_saved_message, Environment.DIRECTORY_DOWNLOADS)
                  : getString(R.string.shell_output_not_saved_message);
          new MaterialAlertDialogBuilder(requireActivity())
              .setIcon(R.mipmap.adb_launcher)
              .setTitle(getString(R.string.app_name))
              .setMessage(message)
              .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {})
              .show();
        });
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(
        () -> {
          if (mResult != null
              && mResult.size() > 0
              && !mResult.get(mResult.size() - 1).equals("aShell: Finish")) {
            updateUI(mResult);
          }
        },
        0,
        250,
        TimeUnit.MILLISECONDS);

    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                  hideSearchBar();
                } else if (mShizukuShell != null && mShizukuShell.isBusy()) {
                  new MaterialAlertDialogBuilder(requireActivity())
                      .setCancelable(false)
                      .setIcon(R.mipmap.adb_launcher)
                      .setTitle(getString(R.string.app_name))
                      .setMessage(getString(R.string.process_destroy_message))
                      .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
                      .setPositiveButton(
                          getString(R.string.yes), (dialogInterface, i) -> mShizukuShell.destroy())
                      .show();
                } else if (mExit) {
                  mExit = false;
                  requireActivity().finish();
                } else {
                  Utils.snackBar(mRootView, getString(R.string.press_back)).show();
                  mExit = true;
                  mHandler.postDelayed(() -> mExit = false, 2000);
                }
              }
            });

    return mRootView;
  }

  private int lastIndexOf(String s, String splitTxt) {
    return s.lastIndexOf(splitTxt);
  }

  private List<String> getRecentCommands() {
    List<String> mRecentCommands = new ArrayList<>(mHistory);
    Collections.reverse(mRecentCommands);
    return mRecentCommands;
  }

  private String splitPrefix(String s, int i) {
    String[] splitPrefix = {s.substring(0, lastIndexOf(s, " ")), s.substring(lastIndexOf(s, " "))};
    return splitPrefix[i].trim();
  }

  private void clearAll() {
    if (mShizukuShell != null) mShizukuShell.destroy();
    mResult = null;
    mRecyclerViewOutput.setAdapter(null);
    mSearchButton.setVisibility(View.GONE);
    mSaveButton.setVisibility(View.GONE);
    mClearButton.setVisibility(View.GONE);
    if (!mCommand.isFocused()) mCommand.requestFocus();
  }

  private void hideSearchBar() {
    mSearchWord.setText(null);
    mSearchWord.setVisibility(View.GONE);
    if (!mCommand.isFocused()) mCommand.requestFocus();
    mBookMarks.setVisibility(View.VISIBLE);
    mSettingsButton.setVisibility(View.VISIBLE);
    if (mHistory != null && mHistory.size() > 0) {
      mHistoryButton.setVisibility(View.VISIBLE);
    }
    if (mResult != null && mResult.size() > 0 && !mShizukuShell.isBusy()) {
      mClearButton.setVisibility(View.VISIBLE);
      mSearchButton.setVisibility(View.VISIBLE);
    }
  }

  private void initializeShell(Activity activity) {
    if (mCommand.getText() == null || mCommand.getText().toString().trim().isEmpty()) {
      return;
    }
    if (mShizukuShell != null && mShizukuShell.isBusy()) {
      new MaterialAlertDialogBuilder(activity)
          .setCancelable(false)
          .setIcon(R.mipmap.adb_launcher)
          .setTitle(getString(R.string.app_name))
          .setMessage(getString(R.string.app_working_message))
          .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {})
          .show();
      return;
    }
    runShellCommand(mCommand.getText().toString().replace("\n", ""), activity);
  }

  private void runShellCommand(String command, Activity activity) {
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    mCommand.setText(null);
    mCommand.clearFocus();
    if (mSearchWord.getVisibility() == View.VISIBLE) {
      mSearchWord.setText(null);
      mSearchWord.setVisibility(View.GONE);
      mBookMarks.setVisibility(View.VISIBLE);
      mSettingsButton.setVisibility(View.VISIBLE);
    } else {
      mHistoryButton.setVisibility(View.GONE);
      mClearButton.setVisibility(View.GONE);
      mSearchButton.setVisibility(View.GONE);
    }

    String finalCommand;
    if (command.startsWith("adb shell ")) {
      finalCommand = command.replace("adb shell ", "");
    } else if (command.startsWith("adb -d shell ")) {
      finalCommand = command.replace("adb -d shell ", "");
    } else {
      finalCommand = command;
    }

    if (finalCommand.equals("clear")) {
      if (mResult != null) {
        mResult.clear();
        updateUI(mResult);
        mSaveButton.setVisibility(View.GONE);
      }
      return;
    }

    // Fun Commands
    if (finalCommand.equals("goto top")) {
      if (mResult != null) {
        mRecyclerViewOutput.scrollToPosition(0);
      }
      return;
    }
    if (finalCommand.equals("goto bottom")) {
      if (mResult != null) {
        mRecyclerViewOutput.scrollToPosition(
            Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);
      }
      return;
    }
    // Fun Commands

    if (finalCommand.equals("exit")) {
      new MaterialAlertDialogBuilder(activity)
          .setCancelable(false)
          .setIcon(R.mipmap.adb_launcher)
          .setTitle(getString(R.string.app_name))
          .setMessage(getString(R.string.quit_app_message))
          .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
          .setPositiveButton(getString(R.string.quit), (dialogInterface, i) -> activity.finish())
          .show();
      return;
    }

    if (finalCommand.startsWith("su")) {
      Utils.snackBar(
              activity.findViewById(android.R.id.content), getString(R.string.su_warning_message))
          .show();
      return;
    }

    if (mHistory == null) {
      mHistory = new ArrayList<>();
    }
    mHistory.add(finalCommand);

    mSaveButton.setVisibility(View.GONE);
    mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_stop, requireActivity()));
    mSendButton.setColorFilter(Utils.getColor(R.color.colorRed, requireActivity()));

    String mTitleText =
        "<font color=\""
            + Utils.getColor(R.color.colorBlue, activity)
            + "\">shell@"
            + Utils.getDeviceName()
            + "</font>  #  <i>"
            + finalCommand
            + "</i>";

    if (mResult == null) {
      mResult = new ArrayList<>();
    }
    mResult.add(mTitleText);

    ExecutorService mExecutors = Executors.newSingleThreadExecutor();
    mExecutors.execute(
        () -> {
          if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            mPosition = mResult.size();
            mShizukuShell = new ShizukuShell(mResult, finalCommand);
            mShizukuShell.exec();
            try {
              TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException ignored) {
            }
          }
          new Handler(Looper.getMainLooper())
              .post(
                  () -> {
                    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                      if (mHistory != null
                          && mHistory.size() > 0
                          && mHistoryButton.getVisibility() != View.VISIBLE) {
                        mHistoryButton.setVisibility(View.VISIBLE);
                      }
                      if (mResult != null && mResult.size() > 0) {

                        mClearButton.setVisibility(View.VISIBLE);
                        mSaveButton.setVisibility(View.VISIBLE);
                        mSearchButton.setVisibility(View.VISIBLE);
                        mResult.add("<i></i>");
                        mResult.add("aShell: Finish");
                      }
                    } else {
                      new MaterialAlertDialogBuilder(activity)
                          .setCancelable(false)
                          .setIcon(R.mipmap.adb_launcher)
                          .setTitle(getString(R.string.app_name))
                          .setMessage(getString(R.string.shizuku_access_denied_message))
                          .setNegativeButton(
                              getString(R.string.quit), (dialogInterface, i) -> activity.finish())
                          .setPositiveButton(
                              getString(R.string.request_permission),
                              (dialogInterface, i) -> Shizuku.requestPermission(0))
                          .show();
                    }
                    if (mCommand.getText() == null
                        || mCommand.getText().toString().trim().isEmpty()) {
                      mSendButton.setImageDrawable(
                          Utils.getDrawable(R.drawable.ic_help, requireActivity()));
                      mSendButton.clearColorFilter();
                    } else {
                      mSendButton.setImageDrawable(
                          Utils.getDrawable(R.drawable.ic_send, requireActivity()));
                      mSendButton.clearColorFilter();
                    }

                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    if (!mCommand.isFocused()) mCommand.requestFocus();
                  });
          if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
  }

  private void updateUI(List<String> data) {
    if (data == null) {
      return;
    }

    List<String> mData = new ArrayList<>();
    try {
      for (String result : data) {
        if (!TextUtils.isEmpty(result) && !result.equals("aShell: Finish")) {
          mData.add(result);
        }
      }
    } catch (ConcurrentModificationException ignored) {
      // Handle concurrent modification gracefully
    }

    ExecutorService mExecutors = Executors.newSingleThreadExecutor();
    mExecutors.execute(
        () -> {
          ShellOutputAdapter mShellOutputAdapter = new ShellOutputAdapter(mData);
          new Handler(Looper.getMainLooper())
              .post(
                  () -> {
                    if (mRecyclerViewOutput != null) {
                      mRecyclerViewOutput.setAdapter(mShellOutputAdapter);
                      mRecyclerViewOutput.scrollToPosition(mData.size() - 1);
                    }
                  });
          if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mShizukuShell != null) mShizukuShell.destroy();
  }
}