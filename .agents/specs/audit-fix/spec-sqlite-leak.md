# SQLite 连接泄漏修复 Spec

## Why
logcat 报错 `A SQLiteConnection object for database was leaked`，根因是 `UserDao`/`TodoDao` 创建了 `SQLiteOpenHelper` 但从未调用 `close()`，连接池未在 Activity 销毁时释放。

## What Changes
- `UserDao` / `TodoDao`：新增 `close()` 方法，委托 `dbHelper.close()`
- 5 个 Activity：在 `onDestroy()` 中调用 `dao.close()`

## Impact
- 涉及 DAO 层和所有使用 DAO 的 Activity
- 不影响任何业务逻辑，纯资源释放修复
