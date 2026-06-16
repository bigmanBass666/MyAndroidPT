"""Login + Todo CRUD test for MyAndroidPT on emulator-5556"""
import uiautomator2 as u2
import time

d = u2.connect('emulator-5556')

def log(msg):
    print(f'  [{time.strftime("%H:%M:%S")}] {msg}')

def shot():
    d.screenshot(f'test_{int(time.time())}.jpg')

d.app_stop('com.ljx.pt')
time.sleep(1)
d.app_start('com.ljx.pt', wait=True)
time.sleep(2)
shot()

log('=== LOGIN ===')
# Fill username
d(text='请输入用户名或手机号').click()
time.sleep(0.5)
d.send_keys('testuser')
log('Entered username')

# Fill password
d(resourceId='com.ljx.pt:id/et_password').click()
time.sleep(0.5)
d.send_keys('123456')
log('Entered password')
shot()

# Click login
d(text='登录').click()
time.sleep(3)
shot()
log('Clicked login')

# Navigate to todo list
log('=== TODO LIST ===')
todo_btn = d(textContains='待办')
if todo_btn.wait(timeout=5):
    todo_btn.click()
    log('Entered todo list')
    time.sleep(2)
    shot()
else:
    log('Already on todo list?')

# Add todo
log('=== ADD TODO ===')
fab = d(description='新增待办')
if fab.wait(timeout=3):
    fab.click()
    time.sleep(1)
    shot()
    log('Opened add todo')

    # Title
    d(className='android.widget.EditText', instance=0).click()
    time.sleep(0.5)
    d.send_keys('自动化测试待办')
    log('Entered title')

    # Content
    d(className='android.widget.EditText', instance=1).click()
    time.sleep(0.5)
    d.send_keys('这是一条由自动化脚本添加的测试待办事项')
    log('Entered content')
    shot()

    # Save
    d(text='保存').click()
    time.sleep(2)
    shot()
    log('Saved todo')

# View detail - click first item
log('=== VIEW DETAIL ===')
# Click on the todo item
item = d(className='androidx.recyclerview.widget.RecyclerView').child(className='android.widget.FrameLayout', instance=0)
if item.wait(timeout=3):
    item.click()
    time.sleep(2)
    shot()
    log('Viewed detail')
else:
    log('No item to click, try back first')
    d.press('back')
    time.sleep(1)
    item = d(className='androidx.recyclerview.widget.RecyclerView').child(className='android.widget.FrameLayout', instance=0)
    if item.wait(timeout=2):
        item.click()
        time.sleep(2)
        shot()

log('=== DONE ===')