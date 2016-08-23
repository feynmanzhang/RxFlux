package com.lean.android.actions;

import com.lean.android.models.Todo;
import com.lean.rxflux.Action;
import com.lean.rxflux.ActionsCreator;

/**
 * @author lean
 */
public class TodoActionsCreator extends ActionsCreator {

    public void create(String text) {
        postAction(new Action(TodoActions.TODO_CREATE, text));
    }

    public void destroy(long id) {
        postAction(new Action( TodoActions.TODO_DESTROY, id));
    }

    public void undoDestroy() {
        postAction(new Action(TodoActions.TODO_UNDO_DESTROY, null));
    }

    public void toggleComplete(Todo todo) {
        long id = todo.getId();
        String actionType = todo.isComplete() ? TodoActions.TODO_UNDO_COMPLETE : TodoActions.TODO_COMPLETE;

        postAction(new Action(actionType, id));
    }

    public void toggleCompleteAll() {
        postAction(new Action(TodoActions.TODO_TOGGLE_COMPLETE_ALL, null));
    }

    public void destroyCompleted() {
        postAction(new Action(TodoActions.TODO_DESTROY_COMPLETED, null));
    }
}
