package com.ljx.pt.adapter;

import android.util.Log;
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

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

 private static final String TAG = "TodoAdapter";
 private List<Todo> todos = new ArrayList<>();
 private OnItemListener listener;
 private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

 public interface OnItemListener {
 void onStatusChanged(Todo todo, boolean isDone);
 void onDelete(Todo todo);
 void onItemClick(Todo todo);
 }

 public void setOnItemListener(OnItemListener listener) {
 this.listener = listener;
 }

 public void setTodos(List<Todo> todos) {
 this.todos = todos;
 notifyDataSetChanged();
 }

 public Todo getItem(int position) {
 return todos.get(position);
 }

 @NonNull
 @Override
 public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
 View view = LayoutInflater.from(parent.getContext())
 .inflate(R.layout.item_todo, parent, false);
 return new TodoViewHolder(view);
 }

 @Override
 public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
 Todo todo = todos.get(position);
 Log.i(TAG, "bind pos=" + position + " title=" + todo.getTitle());
 holder.tvTitle.setText(todo.getTitle());
 holder.tvTime.setText(sdf.format(new Date(todo.getCreateTime())));

 if (todo.isDone()) {
 holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
 holder.tvTitle.setAlpha(0.5f);
 } else {
 holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
 holder.tvTitle.setAlpha(1.0f);
 }

 holder.cbDone.setOnCheckedChangeListener(null);
 holder.cbDone.setChecked(todo.isDone());
 holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
 if (listener != null) {
 listener.onStatusChanged(todo, isChecked);
 }
 });

 holder.btnDelete.setOnClickListener(v -> {
 if (listener != null) {
 listener.onDelete(todo);
 }
 });

 holder.itemView.setOnClickListener(v -> {
 Log.i(TAG, "onItemClick pos=" + holder.getAdapterPosition() + " title=" + todo.getTitle());
 if (listener != null) {
 listener.onItemClick(todo);
 }
 });
 }

 @Override
 public int getItemCount() {
 return todos == null ? 0 : todos.size();
 }

 static class TodoViewHolder extends RecyclerView.ViewHolder {
 CheckBox cbDone;
 TextView tvTitle;
 TextView tvTime;
 ImageButton btnDelete;

 TodoViewHolder(@NonNull View itemView) {
 super(itemView);
 cbDone = itemView.findViewById(R.id.cb_done);
 tvTitle = itemView.findViewById(R.id.tv_title);
 tvTime = itemView.findViewById(R.id.tv_time);
 btnDelete = itemView.findViewById(R.id.btn_delete);
 }
 }
}
