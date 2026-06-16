"""Full end-to-end test for MyAndroidPT - using click + clipboard workaround"""
import uiautomator2 as u2
import time

d = u2.connect('emulator-5556')
d.app_start('com.ljx.pt', stop=True)
time.sleep(2)

def log(msg):
    print(f'  >>> {msg}')

def screenshot():
    ts = int(time.time())
    d.screenshot(f'test_{ts}.jpg')
    print(f'    screenshot saved as test_{ts}.jpg')

def tap(x, y):
    d.click(x, y)
    time.sleep(0.5)

def long_press_text(text, duration=1.5):
    """Long press to bring context menu, then paste"""
    d.long_click(100, 100, duration)

def paste_text(text):
    """Use clipboard to paste text into EditText"""
    import subprocess
    # Use adb to send text via clipboard
    # Write to clipboard via am command
    subprocess.run(['adb', '-s', 'emulator-5556', 'shell',
                    'am', 'broadcast', '-a', 'ACLIP_BOARD',
                    '--ei', 'aclib_mode', '1', '-es', 'aclib_data', text],
                   capture_output=True)
    time.sleep(0.3)

# ===== Try a different approach: use uiautomator2's send_keys =====
def input_text_via_uiautomator(edittext_xpath, text):
    """Click the EditText, then use device.send_keys"""
    el = d.xpath(edittext_xpath)
    if not el.wait(timeout=3):
        print(f'    [WARN] Element not found: {edittext_xpath}')
        return False
    el.click()
    time.sleep(0.5)
    # d.send_keys sends keystrokes through the IME
    d.send_keys(text, clear=False)
    time.sleep(0.5)
    return True

log('Step 1: Navigate to register page')
wait_btn = d.xpath('//android.widget.Button[contains(@text, "立即注册")]')
if wait_btn.wait(timeout=5):
    wait_btn.click()
    log('Tapped register link')
    time.sleep(2)
else:
    log('Register button not found, might already be on register page')
screenshot()

log('Step 2: Fill registration form')
# Account field
success = input_text_via_uiautomator('//android.widget.EditText[@hint="请输入用户名或手机号"]', 'testuser')
log(f'Account input: {"OK" if success else "FAILED"}')

# Password field
success2 = input_text_via_uiautomator('//android.widget.EditText[@hint="请输入密码"]', '123456')
log(f'Password input: {"OK" if success2 else "FAILED"}')

# Confirm password
success3 = input_text_via_uiautomator('//android.widget.EditText[@hint="请再次输入密码"]', '123456')
log(f'Confirm password input: {"OK" if success3 else "FAILED"}')

screenshot()

# Check agreement
log('Step 3: Check agreement checkbox')
for cb in d.xpath('//android.widget.CheckBox').all():
    info = cb.info
    txt = info.get('text', '')
    log(f'  CheckBox: text="{txt}" checked={info.get("checked", False)}')
    if ('已阅读' in txt or '协议' in txt or '同意' in txt) and not info.get('checked', False):
        cb.click()
        log(f'  Checked: {txt}')
        time.sleep(0.5)
        break

screenshot()

# Click register button
log('Step 4: Click register button')
reg_btn = d.xpath('//android.widget.Button[contains(@text, "注册")]')
if reg_btn.wait(timeout=3):
    reg_btn.click()
    log('Registered')
else:
    log('Register button not found')
time.sleep(2)
screenshot()

log('Step 5: Login')
# Username
input_text_via_uiautomator('//android.widget.EditText[@hint="请输入用户名或手机号"]', 'testuser')
# Password
input_text_via_uiautomator('//android.widget.EditText[@hint="请输入密码"]', '123456')
screenshot()

# Click login
login_btn = d.xpath('//android.widget.Button[@text="登录"]')
if login_btn.wait(timeout=3):
    login_btn.click()
    log('Logged in')
time.sleep(3)
screenshot()

log('Step 6: Navigate to Todo List')
# Welcome page or direct to todo list
todo_btn = d.xpath('//android.widget.Button[contains(@text, "待办")]')
if todo_btn.wait(timeout=5):
    todo_btn.click()
    log('Entered todo list')
    time.sleep(2)
else:
    log('No todo button found, checking if already in todo list')

screenshot()

log('Step 7: Add Todo via FAB')
fab = d.xpath('//android.widget.ImageButton[@content-desc="新增待办"]')
if fab.wait(timeout=5):
    fab.click()
    log('Opened add todo page')
    time.sleep(1)
    screenshot()

    # Fill title
    input_text_via_uiautomator('//android.widget.EditText[@hint="请输入待办标题"]', '自动化测试待办')
    # Fill content
    input_text_via_uiautomator('//android.widget.EditText[@hint="请输入待办内容"]', '这是一条测试待办')
    screenshot()

    # Save
    save_btn = d.xpath('//android.widget.Button[@text="保存"]')
    if save_btn.wait(timeout=3):
        save_btn.click()
        log('Saved todo')
    time.sleep(2)
else:
    log('FAB not found')

screenshot()

log('Step 8: Check todo list')
# Go back to list
d.press('back')
time.sleep(1)
screenshot()

log('=== TEST COMPLETE ===')
