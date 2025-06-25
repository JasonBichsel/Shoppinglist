package ch.wiss.shopping_list;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private EditText editItemName, editItemDescription, editItemNote;
    private Button buttonAdd;
    private ListView listView;
    private List<ShoppingItem> shoppingList;
    private ArrayAdapter<ShoppingItem> adapter;

    class ShoppingItem {
        String name;
        String description;
        String note;

        public ShoppingItem(String name, String description, String note) {
            this.name = name;
            this.description = description;
            this.note = note;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI-Elemente verbinden
        editItemName = findViewById(R.id.editItemName);
        editItemDescription = findViewById(R.id.editItemDescription);
        editItemNote = findViewById(R.id.editItemNote);
        buttonAdd = findViewById(R.id.buttonAdd);
        listView = findViewById(R.id.listView);

        shoppingList = new ArrayList<>();
        loadShoppingList();

        // Adapter bauen
        adapter = new ArrayAdapter<ShoppingItem>(this, R.layout.list_item, R.id.textView1, shoppingList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView1 = view.findViewById(R.id.textView1);
                TextView textView2 = view.findViewById(R.id.textView2);
                TextView textView3 = view.findViewById(R.id.textView3);

                ShoppingItem item = shoppingList.get(position);
                textView1.setText(item.name);
                textView2.setText(item.description);
                textView3.setText(item.note);

                return view;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            ShoppingItem itemToRemove = shoppingList.get(position);
            shoppingList.remove(position);
            saveShoppingList();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Gelöscht: " + itemToRemove.name, Toast.LENGTH_SHORT).show();
            return true;
        });

        buttonAdd.setOnClickListener(v -> {
            String name = editItemName.getText().toString().trim();
            String description = editItemDescription.getText().toString().trim();
            String note = editItemNote.getText().toString().trim();

            if (!name.isEmpty() && !description.isEmpty()) {
                if (note.isEmpty()) note = "-";
                shoppingList.add(new ShoppingItem(name, description, note));
                saveShoppingList();
                adapter.notifyDataSetChanged();
                editItemName.setText("");
                editItemDescription.setText("");
                editItemNote.setText("");
            } else {
                Toast.makeText(this, "Bitte Name und Beschreibung eingeben!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Serialisierung der Liste zu einem String
    private String serializeShoppingList(List<ShoppingItem> list) {
        StringBuilder sb = new StringBuilder();
        for (ShoppingItem item : list) {
            sb.append(item.name.replace(",", "\\,")).append(",")
                    .append(item.description.replace(",", "\\,")).append(",")
                    .append(item.note.replace(";", "\\;")).append(";");
        }
        return sb.toString();
    }

    // Deserialisierung von String zu Liste
    private List<ShoppingItem> deserializeShoppingList(String data) {
        List<ShoppingItem> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;

        String[] pairs = data.split("(?<!\\\\);");
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("(?<!\\\\),");
            if (parts.length < 3) continue;
            String name = parts[0].replace("\\,", ",");
            String description = parts[1].replace("\\,", ",");
            String note = parts[2].replace("\\;", ";");
            list.add(new ShoppingItem(name, description, note));
        }
        return list;
    }

    // Speichern in SharedPreferences
    private void saveShoppingList() {
        String serialized = serializeShoppingList(shoppingList);
        getSharedPreferences("prefs", MODE_PRIVATE)
                .edit()
                .putString("shopping_list", serialized)
                .apply();
    }

    // Laden aus SharedPreferences
    private void loadShoppingList() {
        String serialized = getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("shopping_list", "");
        shoppingList.clear();
        shoppingList.addAll(deserializeShoppingList(serialized));
    }
}
