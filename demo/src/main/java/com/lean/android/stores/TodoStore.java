package com.lean.android.stores;


import com.lean.android.actions.TodoActions;
import com.lean.android.models.Todo;
import com.lean.rxflux.action.RxAction;
import com.lean.rxflux.dispatcher.RxBus;
import com.lean.rxflux.store.RxStore;
import com.lean.rxflux.store.RxStoreChange;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



/**
 * @author lean
 */
public class TodoStore extends RxStore {

    private final String STORE_ID = this.getClass().getName();
    private final List<Todo> todos = new ArrayList<>();
    private Todo lastDeleted;

    public List<Todo> getTodos() {
        return todos;
    }

    public boolean canUndo() {
        return lastDeleted != null;
    }


    @Override
    public void onAction(RxAction action) {
        switch (action.getType()) {
            case TodoActions.TODO_CREATE:
                String text = ((String) action.getData());
                create(text);
                emitStoreChange(new RxStoreChange());
                break;

            case TodoActions.TODO_DESTROY:
                long id = ((long) action.getData());
                destroy(id);
                emitStoreChange(new RxStoreChange());
                break;

            case TodoActions.TODO_UNDO_DESTROY:
                undoDestroy();
                emitStoreChange(new RxStoreChange());
                break;

            case TodoActions.TODO_COMPLETE:
                id = ((long) action.getData());
                updateComplete(id, true);
                emitStoreChange(new RxStoreChange());
                break;

            case TodoActions.TODO_UNDO_COMPLETE:
                id = ((long) action.getData());
                updateComplete(id, false);
                emitStoreChange(new RxStoreChange());
                break;

            case TodoActions.TODO_DESTROY_COMPLETED:
                destroyCompleted();
                emitStoreChange(new RxStoreChange());
                break;

            case TodoActions.TODO_TOGGLE_COMPLETE_ALL:
                updateCompleteAll();
                emitStoreChange(new RxStoreChange());
                break;
        }
    }

    private void destroyCompleted() {
        Iterator<Todo> iter = todos.iterator();
        while (iter.hasNext()) {
            Todo todo = iter.next();
            if (todo.isComplete()) {
                iter.remove();
            }
        }
    }

    private void updateCompleteAll() {
        if (areAllComplete()) {
            updateAllComplete(false);
        } else {
            updateAllComplete(true);
        }
    }

    private boolean areAllComplete() {
        for (Todo todo : todos) {
            if (!todo.isComplete()) {
                return false;
            }
        }
        return true;
    }

    private void updateAllComplete(boolean complete) {
        for (Todo todo : todos) {
            todo.setComplete(complete);
        }
    }

    private void updateComplete(long id, boolean complete) {
        Todo todo = getById(id);
        if (todo != null) {
            todo.setComplete(complete);
        }
    }

    private void undoDestroy() {
        if (lastDeleted != null) {
            addElement(lastDeleted.clone());
            lastDeleted = null;
        }
    }

    private void create(String text) {
        long id = System.currentTimeMillis();
        Todo todo = new Todo(id, text);
        addElement(todo);
        Collections.sort(todos);
    }

    private void destroy(long id) {
        Iterator<Todo> iter = todos.iterator();
        while (iter.hasNext()) {
            Todo todo = iter.next();
            if (todo.getId() == id) {
                lastDeleted = todo.clone();
                iter.remove();
                break;
            }
        }
    }

    private Todo getById(long id) {
        Iterator<Todo> iter = todos.iterator();
        while (iter.hasNext()) {
            Todo todo = iter.next();
            if (todo.getId() == id) {
                return todo;
            }
        }
        return null;
    }


    private void addElement(Todo clone) {
        todos.add(clone);
        Collections.sort(todos);
    }

}
