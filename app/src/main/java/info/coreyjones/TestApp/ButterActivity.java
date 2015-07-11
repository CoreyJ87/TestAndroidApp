package info.coreyjones.TestApp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnLongClick;

public class ButterActivity extends AppCompatActivity {
    private List<Map<String, Object>> data;

    private static final ButterKnife.Action<View> ALPHA_FADE = new ButterKnife.Action<View>() {
        @Override public void apply(View view, int index) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
            alphaAnimation.setFillBefore(true);
            alphaAnimation.setDuration(500);
            alphaAnimation.setStartOffset(index * 100);
            view.startAnimation(alphaAnimation);
        }
    };

    @Bind(R.id.thetitle) TextView title;
    @Bind(R.id.thesubtitle) TextView subtitle;
    @Bind(R.id.hello) Button hello;
    @Bind(R.id.list_of_things) ListView listOfThings;
    @Bind(R.id.footer) TextView footer;

    @Bind({ R.id.thetitle, R.id.thesubtitle, R.id.hello })
    List<View> headerViews;


    private SimpleAdapter adapter;

    @OnClick(R.id.hello) void sayHello() {
        Toast.makeText(this, "Hello, views!",Toast.LENGTH_SHORT).show();
        ButterKnife.apply(headerViews, ALPHA_FADE);
    }

    @OnLongClick(R.id.hello) boolean sayGetOffMe() {
        Toast.makeText(this, "Let go of me!", Toast.LENGTH_SHORT).show();
        return true;
    }

    @OnItemClick(R.id.list_of_things) void onItemClick(int position) {
        Toast.makeText(this, "You clicked: " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_butter);
        ButterKnife.bind(this);
        PrepareData();
        // Contrived code to use the bound fields.
        title.setText("Butter Knife");
        subtitle.setText("Field and method binding for Android views.");
        footer.setText("by Jake Wharton");
        hello.setText("Say Hello");

        adapter = new SimpleAdapter(this, data,android.R.layout.simple_list_item_1, new String[] { "AAA" },
                new int[] { android.R.id.text1 });
        listOfThings.setAdapter(adapter);
    }
    private void PrepareData() {
        data = new ArrayList<>();
        Map<String, Object> item;
        item = new HashMap<>();
        item.put("1", "A");
        item.put("2", "B");
        data.add(item);
        item = new HashMap<>();
        item.put("3", "C");
        item.put("4", "D");
        data.add(item);
        item = new HashMap<>();
        item.put("5", "E");
        item.put("6", "F");
        data.add(item);
    }
}
