package in.sunilpaulmathew.ashell.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.ashell.R;
import in.sunilpaulmathew.ashell.adapters.ChangelogAdapter;
import in.sunilpaulmathew.ashell.models.ChangelogItem;

public class ChangelogActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_changelog);

    RecyclerView recyclerViewChangelogs = findViewById(R.id.recycler_view_changelogs);

    // Create a list of ChangelogItem
    List<ChangelogItem> changelogItems = new ArrayList<>();
    changelogItems.add(
        new ChangelogItem(
            "Version 0.9 : \n\n• Added dynamic material themeing.\n\n• Revamped whole UI to give a fresh look."));
    changelogItems.add(
        new ChangelogItem(
            "Version 0.9.1 : \n\n• Added changelogs into the app.\n\n• Added highlighted shell output."));
    // Add more items as needed

    ChangelogAdapter adapter = new ChangelogAdapter(changelogItems);
    recyclerViewChangelogs.setAdapter(adapter);

    recyclerViewChangelogs.setLayoutManager(new LinearLayoutManager(this));
  }
}
