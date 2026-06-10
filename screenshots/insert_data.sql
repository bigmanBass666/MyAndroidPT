DROP TABLE IF EXISTS _test_push; CREATE TABLE _test_push AS SELECT * FROM todo WHERE 1=0;
INSERT INTO todo (title, content, is_done, create_time) VALUES ('测试任务A', '这是测试内容A', 0, 1700000000000);
INSERT INTO todo (title, content, is_done, create_time) VALUES ('测试任务B', '这是测试内容B', 1, 1700000100000);
INSERT INTO todo (title, content, is_done, create_time) VALUES ('学习Android', '完成课程设计', 0, 1700000200000);
