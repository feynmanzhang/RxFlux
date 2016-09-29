package com.lean.android.actions;

import com.lean.android.models.Todo;
import com.lean.rxflux.action.RxAction;
import com.lean.rxflux.action.RxActionsCreator;

/**
 * @author lean
 */
public class TodoActionsCreator extends RxActionsCreator {

    public void create(String text) {
        postAction(new RxAction(TodoActions.TODO_CREATE, text));
    }

    public void destroy(long id) {
        postAction(new RxAction( TodoActions.TODO_DESTROY, id));
    }

    public void undoDestroy() {
        postAction(new RxAction(TodoActions.TODO_UNDO_DESTROY, null));
    }

    public void toggleComplete(Todo todo) {
        long id = todo.getId();
        String actionType = todo.isComplete() ? TodoActions.TODO_UNDO_COMPLETE : TodoActions.TODO_COMPLETE;

        postAction(new RxAction(actionType, id));
    }

    public void toggleCompleteAll() {
        postAction(new RxAction(TodoActions.TODO_TOGGLE_COMPLETE_ALL, null));
    }

    public void destroyCompleted() {
        postAction(new RxAction(TodoActions.TODO_DESTROY_COMPLETED, null));
    }
}
