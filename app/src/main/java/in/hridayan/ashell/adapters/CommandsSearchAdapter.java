package in.hridayan.ashell.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.CommandItems;
import in.hridayan.ashell.utils.Utils;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 08, 2022
 */

public class CommandsSearchAdapter extends RecyclerView.Adapter<CommandsSearchAdapter.ViewHolder> {

  private final List<CommandItems> data;
  private Context context;

  public CommandsSearchAdapter(@Nullable List<CommandItems> data, Context context) {
    this.data = data != null ? data : new ArrayList<>();
    this.context = context;
  }

  public void setFilteredList(List<CommandItems> filteredList) {
    this.data.clear();
    this.data.addAll(filteredList);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public CommandsSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View rowItem =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_examples, parent, false);
    return new CommandsSearchAdapter.ViewHolder(rowItem);
  }

  @Override
  public void onBindViewHolder(@NonNull CommandsSearchAdapter.ViewHolder holder, int position) {

    holder.mTitle.setText(this.data.get(position).getTitle());
    if (this.data.get(position).getSummary() != null) {
      holder.mSummary.setText(this.data.get(position).getSummary());

      int paddingInDp;
      if (position == data.size() - 1 && data.size() != 1) {
        paddingInDp = 50;
      } else if (position == 0) {
        paddingInDp = 20;
      } else {
        paddingInDp = 30;
      }

      float scale = context.getResources().getDisplayMetrics().density;
      int paddingInPixels = (int) (paddingInDp * scale + 0.5f);

      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
      if (position == data.size() - 1 && data.size() != 1) {
        layoutParams.bottomMargin = paddingInPixels;
      } else if (position == 0 || data.size() == 1) {
        layoutParams.topMargin = paddingInPixels;
      } else {
        layoutParams.bottomMargin = 30;
      }
      holder.itemView.setLayoutParams(layoutParams);
    }
  }

  @Override
  public int getItemCount() {
    return this.data.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final MaterialTextView mTitle, mSummary;

    public ViewHolder(View view) {
      super(view);
      view.setOnClickListener(this);
      this.mTitle = view.findViewById(R.id.title);
      this.mSummary = view.findViewById(R.id.summary);
    }

    @Override
    public void onClick(View view) {
      if (data.get(getAdapterPosition()).getExample() != null) {
        new MaterialAlertDialogBuilder(view.getContext())
            .setTitle(R.string.example)
            .setMessage(data.get(getAdapterPosition()).getExample())
            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {})
            .setPositiveButton(
                R.string.copy,
                (dialogInterface, i) -> {
                  String sanitizedText = sanitizeText(data.get(getAdapterPosition()).getTitle());
                  Utils.copyToClipboard(sanitizedText, view.getContext());
                })
            .show();
      }
    }

    private String sanitizeText(String text) {
      String sanitizedText = text.replaceAll("<[^>]*>", "");
      return sanitizedText.trim();
    }
  }
}
