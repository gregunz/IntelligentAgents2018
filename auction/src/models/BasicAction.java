package models;

import logist.task.Task;

import java.util.Objects;

public class BasicAction {

    protected final Task task;
    protected final Event event;

    public BasicAction(Event event, Task task) {
        this.task = task;
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicAction that = (BasicAction) o;
        return Objects.equals(task, that.task) &&
                event == that.event;
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, event);
    }
}