package com.lean.android;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.lean.android.stores.TodoStore;
import com.lean.android.actions.TodoActionsCreator;
import com.lean.rxflux.view.RxBaseActivity;
import com.lean.rxflux.store.RxStoreChange;



public class MainActivity extends RxBaseActivity<TodoActionsCreator, TodoStore> {

    private EditText mainInput;
    private ViewGroup mainLayout;
    private TodoRecyclerAdapter listAdapter;
    private CheckBox mainCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupView();
    }

    private void setupView() {
        mainLayout = ((ViewGroup) findViewById(R.id.main_layout));
        mainInput = (EditText) findViewById(R.id.main_input);

        Button mainAdd = (Button) findViewById(R.id.main_add);
        mainAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = mainInput.getText().toString();
                if (!TextUtils.isEmpty(input)) {
                    getActionsCreator().create(input);
                }
                mainInput.setText("");
            }
        });
        mainCheck = (CheckBox) findViewById(R.id.main_checkbox);
        mainCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActionsCreator().toggleCompleteAll();
            }
        });
        Button mainClearCompleted = (Button) findViewById(R.id.main_clear_completed);
        mainClearCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActionsCreator().destroyCompleted();
                if (mainCheck.isChecked()) {
                    mainCheck.setChecked(false);
                }
            }
        });


        RecyclerView mainList = (RecyclerView) findViewById(R.id.main_list);
        mainList.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new TodoRecyclerAdapter(getActionsCreator());
        mainList.setAdapter(listAdapter);
    }

    public void onRxStoreChanged(RxStoreChange event) {
        updateUI();
    }

    private void updateUI() {
        listAdapter.setItems(getStore().getTodos());

        if (getStore().canUndo()) {
            Snackbar snackbar = Snackbar.make(mainLayout, "Element deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActionsCreator().undoDestroy();
                }
            });
            snackbar.show();
        }
    }
}
