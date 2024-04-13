package in.hridayan.ashell.activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.search.SearchBar;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.CustomSearchView;
import in.hridayan.ashell.UI.ExamplesViewModel;
import in.hridayan.ashell.adapters.CommandsSearchAdapter;
import in.hridayan.ashell.adapters.ExamplesAdapter;
import in.hridayan.ashell.utils.CommandItems;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class ExamplesActivity extends AppCompatActivity {
  private ExamplesViewModel viewModel;
  private AppBarLayout appBarLayout;
  private CustomSearchView searchView;
  private RecyclerView mRecyclerView, mSearchRecyclerView;
  private EditText editText;
  private MaterialTextView noCommandFoundText;
  private List<CommandItems> itemList;
  private SearchBar mSearchBar;
  private ExamplesAdapter mExamplesAdapter;
  private Chip mSummaryChip;
  private int isSortingOptionSame;
  private boolean isSummaryChipClicked = false;

  @Override
  protected void onPause() {
    super.onPause();
    viewModel.setToolbarExpanded(Utils.isToolbarExpanded(appBarLayout));
  }

  @Override
  protected void onResume() {
    super.onResume();
    int position = Utils.recyclerViewPosition(mRecyclerView);

    if (viewModel.isToolbarExpanded()) {
      if (position == 0) {
        Utils.expandToolbar(appBarLayout);
      }
    } else {
      Utils.collapseToolbar(appBarLayout);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);

    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_examples);

    appBarLayout = findViewById(R.id.appBarLayout);
    searchView = findViewById(R.id.search_view);
    mSearchBar = findViewById(R.id.search_bar);
    mSearchRecyclerView = findViewById(R.id.search_recycler_view);
    mSummaryChip = findViewById(R.id.search_summary);
    editText = searchView.getSearchEditText();
    noCommandFoundText = findViewById(R.id.no_command_found);
    itemList = Commands.commandList(this);
    viewModel = new ViewModelProvider(this).get(ExamplesViewModel.class);
    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    mSearchBar.clearFocus();
    mSearchBar.setOnMenuItemClickListener(
        item -> {
          switch (item.getItemId()) {
            case R.id.menu:
              sortingDialog(this, this);
              return true;
            default:
              return false;
          }
        });

    mRecyclerView = findViewById(R.id.recycler_view);
    mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    GridLayoutManager mLayoutManager =
        new GridLayoutManager(
            this,
            getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? 2
                : 1);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mExamplesAdapter = new ExamplesAdapter(Commands.commandList(this), this);
    mExamplesAdapter.sortData();

    mRecyclerView.setAdapter(mExamplesAdapter);
    mRecyclerView.setVisibility(View.VISIBLE);
    editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence text, int i, int i1, int i2) {}

          @Override
          public void onTextChanged(CharSequence text, int i, int i1, int i2) {}

          @Override
          public void afterTextChanged(Editable text) {
            filterList(text);
            mSummaryChip.setOnClickListener(
                v -> {
                  isSummaryChipClicked = true;
                  filterList(text);
                });
          }
        });
  }

  private void filterList(CharSequence text) {
    List<CommandItems> filteredList = new ArrayList<>();
    mSummaryChip.setVisibility(View.GONE);
    noCommandFoundText.setVisibility(View.GONE);
    if (text != null && !text.toString().isEmpty()) {
      searchTitle(text, filteredList);

      if (filteredList.isEmpty()) {
        noCommandFoundText.setVisibility(View.VISIBLE);
        mSummaryChip.setVisibility(View.VISIBLE);
        if (isSummaryChipClicked) {
          chipSummaryOnClick(text, filteredList);
        }
      }

      if (filteredList.isEmpty()) {
        noCommandFoundText.setVisibility(View.VISIBLE);
      }
    }

    mSearchRecyclerView.setVisibility(View.VISIBLE);
    CommandsSearchAdapter adapter = new CommandsSearchAdapter(filteredList, this);
    mSearchRecyclerView.setAdapter(adapter);
  }

  private void searchTitle(CharSequence text, List<CommandItems> filteredList) {
    for (CommandItems item : itemList) {
      if (item.getTitle().toLowerCase().contains(text.toString().toLowerCase())) {
        filteredList.add(item);
      }
    }
  }

  private void searchTitleAndSummary(CharSequence text, List<CommandItems> filteredList) {
    for (CommandItems item : itemList) {
      if (item.getTitle().toLowerCase().contains(text.toString().toLowerCase())
          || item.getSummary().toLowerCase().contains(text.toString().toLowerCase())) {
        filteredList.add(item);
      }
    }
  }

  private void chipSummaryOnClick(CharSequence text, List<CommandItems> filteredList) {
    noCommandFoundText.setVisibility(View.GONE);
    searchTitleAndSummary(text, filteredList);
    mSummaryChip.setVisibility(View.GONE);
  }

  private void sortingDialog(Context context, Activity activity) {
    CharSequence[] sortingOptions = {
      context.getString(R.string.sort_A_Z),
      context.getString(R.string.sort_Z_A),
      context.getString(R.string.most_used),
      context.getString(R.string.least_used)
    };
    int currentSortingOption = Preferences.getSortingExamples(context);
    isSortingOptionSame = currentSortingOption;
    final int[] sortingOption = {currentSortingOption};

    new MaterialAlertDialogBuilder(activity)
        .setTitle(context.getString(R.string.sort))
        .setSingleChoiceItems(
            sortingOptions,
            currentSortingOption,
            (dialog, which) -> {
              sortingOption[0] = which;
            })
        .setPositiveButton(
            context.getString(R.string.ok),
            (dialog, which) -> {
              Preferences.setSortingExamples(context, sortingOption[0]);
              if (isSortingOptionSame != sortingOption[0]) {
                mExamplesAdapter.sortData();
              }
            })
        .setNegativeButton(context.getString(R.string.cancel), (dialog, i) -> {})
        .show();
  }
}
