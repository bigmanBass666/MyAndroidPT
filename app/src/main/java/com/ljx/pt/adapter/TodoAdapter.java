package com.ljx.pt.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.ljx.pt.R;
import com.ljx.pt.bean.Todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** 待办列表 RecyclerView 适配器，支持状态切换、长按删除和点击跳转 */
public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoVH> {

    public interface OnTodoActionListener {
        void onToggleDone(long todoId, boolean isDone);
        void onDelete(long todoId, String todoTitle);
        void onItemClick(long todoId);
    }

    private final List<Todo> todos = new ArrayList<>();
    private final OnTodoActionListener listener;
    private static final SimpleDateFormat DATE_FMT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public TodoAdapter(OnTodoActionListener listener) {
        this.listener = listener;
    }

    /** 替换整个数据列表并刷新 UI */
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
        TodoVH holder = new TodoVH(v);

        // 长按删除
        holder.itemView.setOnLongClickListener(v1 -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                Todo todo = todos.get(pos);
                listener.onDelete(todo.getId(), todo.getTitle());
            }
            return true;
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TodoVH holder, int position) {
        Todo todo = todos.get(position);
        holder.tvTitle.setText(todo.getTitle());
        holder.tvTime.setText(DATE_FMT.format(new Date(todo.getCreateTime())));

        // 先解绑避免复用冲突
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(todo.isDone());
        // 再绑定
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) ->
            listener.onToggleDone(todo.getId(), isChecked));

        // 右侧状态指示：已完成=绿色 ✓，未完成=灰色 ●
        if (todo.isDone()) {
            holder.tvStatus.setText("✓");
            holder.tvStatus.setTextColor(0xFF4CAF50);
            // 已完成背景：surface_variant 30% 透明度
            int baseColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.surface_variant);
            int bgColor = Color.argb(77, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
            holder.itemView.setBackgroundColor(bgColor);
        } else {
            holder.tvStatus.setText("●");
            holder.tvStatus.setTextColor(0xFF9E9E9E);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // 点击跳转详情
        holder.itemView.setOnClickListener(v ->
            listener.onItemClick(todo.getId()));
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    static class TodoVH extends RecyclerView.ViewHolder {
        MaterialCheckBox cbDone;
        TextView tvTitle;
        TextView tvTime;
        TextView tvStatus;

        TodoVH(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cb_done);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}