package ch.wiss.shopping_list;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private EditText editItemName, editItemQuantity, editItemUnit, editItemPlace;
    private Button buttonAdd;
    private ListView listView;
    private List<ShoppingItem> shoppingList;
    private ShoppingListAdapter adapter;

    // Neues Datenmodell mit Menge, Einheit und Ort
    class ShoppingItem {
        String name;
        String quantity;  // Menge als String, z.B. "500"
        String unit;      // Einheit z.B. "Gramm"
        String place;     // Einkaufsort z.B. "Landi"

        public ShoppingItem(String name, String quantity, String unit, String place) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.place = place;
        }
    }

    // Custom Adapter mit Checkbox
    class ShoppingListAdapter extends ArrayAdapter<ShoppingItem> {
        public ShoppingListAdapter() {
            super(MainActivity.this, R.layout.list_item, shoppingList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            TextView textName = convertView.findViewById(R.id.textViewName);
            TextView textQuantityUnit = convertView.findViewById(R.id.textViewQuantityUnit);
            TextView textPlace = convertView.findViewById(R.id.textViewPlace);
            CheckBox checkBox = convertView.findViewById(R.id.checkBoxDone);

            ShoppingItem item = shoppingList.get(position);
            textName.setText(item.name);
            textQuantityUnit.setText(item.quantity + " " + item.unit);
            textPlace.setText(item.place);

            checkBox.setOnCheckedChangeListener(null); // Listener vor dem Setzen entfernen
            checkBox.setChecked(false); // Standard: nicht abgehakt

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Artikel entfernen, wenn abgehakt
                    shoppingList.remove(position);
                    saveShoppingList();
                    notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Artikel entfernt: " + item.name, Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editItemName = findViewById(R.id.editItemName);
        editItemQuantity = findViewById(R.id.editItemQuantity);
        editItemUnit = findViewById(R.id.editItemUnit);
        editItemPlace = findViewById(R.id.editItemPlace);
        buttonAdd = findViewById(R.id.buttonAdd);
        listView = findViewById(R.id.listView);

        shoppingList = new ArrayList<>();
        loadShoppingList();

        adapter = new ShoppingListAdapter();
        listView.setAdapter(adapter);

        buttonAdd.setOnClickListener(v -> {
            String name = editItemName.getText().toString().trim();
            String quantity = editItemQuantity.getText().toString().trim();
            String unit = editItemUnit.getText().toString().trim();
            String place = editItemPlace.getText().toString().trim();

            if (name.isEmpty() || quantity.isEmpty() || unit.isEmpty() || place.isEmpty()) {
                Toast.makeText(MainActivity.this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            shoppingList.add(new ShoppingItem(name, quantity, unit, place));
            saveShoppingList();
            adapter.notifyDataSetChanged();

            editItemName.setText("");
            editItemQuantity.setText("");
            editItemUnit.setText("");
            editItemPlace.setText("");
        });
    }

    private String serializeShoppingList(List<ShoppingItem> list) {
        StringBuilder sb = new StringBuilder();
        for (ShoppingItem item : list) {
            sb.append(item.name.replace(",", "\\,")).append(",")
                    .append(item.quantity.replace(",", "\\,")).append(",")
                    .append(item.unit.replace(",", "\\,")).append(",")
                    .append(item.place.replace(";", "\\;")).append(";");
        }
        return sb.toString();
    }

    private List<ShoppingItem> deserializeShoppingList(String data) {
        List<ShoppingItem> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;

        String[] items = data.split("(?<!\\\\);");
        for (String item : items) {
            if (item.isEmpty()) continue;
            String[] parts = item.split("(?<!\\\\),");
            if (parts.length < 4) continue;
            String name = parts[0].replace("\\,", ",");
            String quantity = parts[1].replace("\\,", ",");
            String unit = parts[2].replace("\\,", ",");
            String place = parts[3].replace("\\;", ";");
            list.add(new ShoppingItem(name, quantity, unit, place));
        }
        return list;
    }

    private void saveShoppingList() {
        String serialized = serializeShoppingList(shoppingList);
        getSharedPreferences("prefs", MODE_PRIVATE)
                .edit()
                .putString("shopping_list", serialized)
                .apply();
    }

    private void loadShoppingList() {
        String serialized = getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("shopping_list", "");
        shoppingList.clear();
        shoppingList.addAll(deserializeShoppingList(serialized));
    }
}
