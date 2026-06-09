package com.ljx.pt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ljx.pt.R;
import com.ljx.pt.bean.Todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoVH> {

    public interface OnTodoActionListener {
        void onToggleDone(int todoId, boolean isDone);
        void onDelete(int todoId);
        void onItemClick(int todoId);
    }

    private final List<Todo> todos = new ArrayList<>();
    private final OnTodoActionListener listener;
    private static final SimpleDateFormat DATE_FMT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public TodoAdapter(OnTodoActionListener listener) {
        this.listener = listener;
    }

    public void setTodos(List<Todo> list) {
        todos.clear();
        if (list != null) todos.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_todo, parent, false);
        return new TodoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoVH holder, int position) {
        Todo todo = todos.get(position);
        holder.tvTitle.setText(todo.getTitle());
        holder.tvTime.setText(DATE_FMT.format(new Date(todo.getCreateTime())));
        holder.cbDone.setChecked(todo.isDone());
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) ->
            listener.onToggleDone(todo.getId(), isChecked));
        holder.btnDelete.setOnClickListener(v ->
            listener.onDelete(todo.getId()));
        holder.itemView.setOnClickListener(v ->
            listener.onItemClick(todo.getId()));
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    static class TodoVH extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        TextView tvTitle;
        TextView tvTime;
        ImageButton btnDelete;

        TodoVH(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cb_done);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
