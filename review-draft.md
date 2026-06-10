我在工作中，忽略心跳。

刚才已完成全面的教材/大纲对照审查，共发现 11 个问题（已输出详细报告）。需要我继续修复这些问题吗？按严重程度排序的修复顺序是：

1. `TodoDetailActivity` CheckBox listener 绑定时机导致打开页面触发多余 DB 写
2. 注册插入前未判重（重复用户名会 crash）
3. `Todo.java` isDone field/getter 命名不规范
4. `insert()` 写死 is_done=0 忽略传入值
5. 其余架构偏差和小问题

确认后我逐个修。