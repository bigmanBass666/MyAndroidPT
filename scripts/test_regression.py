import subprocess, time, json

def shell(cmd, timeout=10):
    subprocess.run(cmd, shell=True, timeout=timeout)

def input_text(text):
    subprocess.run(['adb', '-s', 'emulator-5554', 'shell', 'input', 'text', text], timeout=5)

def layout():
    result = subprocess.run(['android', 'layout', '--device', 'emulator-5554', '-p'],
                          capture_output=True, text=True)
    lines = result.stdout.strip().split('\n')
    if lines and lines[0]:
        return [json.loads(l) for l in lines]
    return []

pass_count = 0
fail_count = 0

def check(name, condition):
    global pass_count, fail_count
    if condition:
        print(f' PASS: {name}')
        pass_count += 1
    else:
        print(f' FAIL: {name}')
        fail_count += 1

# ===== TEST 1: Login =====
print('=== TEST 1: Login ===')
shell('adb -s emulator-5554 shell am start -n com.ljx.pt/.MainActivity')
time.sleep(2)

input_text('testuser02')
time.sleep(0.3)

# Tab to password field
shell('adb -s emulator-5554 shell input keyevent 61')
time.sleep(0.3)
input_text('123456')
time.sleep(0.3)

# Click login
shell('adb -s emulator-5554 shell input tap 540 942')
time.sleep(2)

items = layout()
check('Login success (welcome page)', any(i.get('resource-id') == 'tv_welcome' for i in items))

for item in items:
    if item.get('resource-id') == 'tv_welcome':
        print(f' Welcome text: {item.get("text")}')
        check('Welcome shows username', 'testuser02' in str(item.get('text', '')))

# ===== TEST 2: Todo List =====
print('\n=== TEST 2: Todo List ===')
# Click "进入待办列表" button
shell('adb -s emulator-5554 shell input tap 540 463')
time.sleep(2)
items = layout()
check('Todo list loads', any(i.get('resource-id') == 'rv_todo' for i in items))
check('FAB exists', any(i.get('resource-id') == 'fab_add' for i in items))

todos = [i for i in items if i.get('resource-id') == 'tv_title']
print(f' Todo count: {len(todos)}')
for t in todos:
    print(f' - {t.get("text")}')

# ===== TEST 3: Add Todo =====
print('\n=== TEST 3: Add Todo ===')
shell('adb -s emulator-5554 shell input tap 964 2221')
time.sleep(2)

# Fill fields using Tab key to navigate
shell('adb -s emulator-5554 shell input tap 540 360')
time.sleep(0.5)
input_text('regflow')
time.sleep(0.3)

shell('adb -s emulator-5554 shell input keyevent 61')
time.sleep(0.3)
input_text('regcontent')
time.sleep(0.3)

# Click save
shell('adb -s emulator-5554 shell input tap 964 2274')
time.sleep(2)
items = layout()
check('New todo saved', any('regflow' in str(i.get('text', '')) for i in items))
check('FAB still exists', any(i.get('resource-id') == 'fab_add' for i in items))

# ===== TEST 4: Todo Detail =====
print('\n=== TEST 4: Todo Detail ===')
# Find and tap the first todo item (approx center of rv_todo)
shell('adb -s emulator-5554 shell input tap 540 347')
time.sleep(2)
items = layout()
check('Detail page loads', any(i.get('resource-id') == 'tv_detail_title' for i in items))
check('Detail has delete btn', any(i.get('resource-id') == 'btn_detail_delete' for i in items))
check('Detail has edit btn', any(i.get('resource-id') == 'btn_detail_edit' for i in items))

# Back via keyevent
shell('adb -s emulator-5554 shell input keyevent 4')
time.sleep(2)
items = layout()
check('Back from detail to list', any(i.get('resource-id') == 'rv_todo' for i in items))

# ===== TEST 5: Delete Confirmation =====
print('\n=== TEST 5: Delete Confirmation ===')
items = layout()
for item in items:
    if item.get('resource-id') == 'btn_delete':
        center = item.get('center', '')
        if center:
            x, y = center.strip('[]').split(',')
            shell(f'adb -s emulator-5554 shell input tap {x} {y}')
            break
time.sleep(1)

items = layout()
dialog_items = [i for i in items if i.get('text') in ['删除', '确定', '取消', '确认']]
check('Delete dialog appears', len(dialog_items) > 0)
if dialog_items:
    for d in dialog_items:
        print(f' Dialog: {d.get("text")}')

# Cancel
shell('adb -s emulator-5554 shell input keyevent 4')
time.sleep(1)

# ===== TEST 6: Logout =====
print('\n=== TEST 6: Logout ===')
shell('adb -s emulator-5554 shell input keyevent 4')
time.sleep(1)
items = layout()
check('Back to welcome', any(i.get('resource-id') == 'btn_logout' for i in items))

shell('adb -s emulator-5554 shell input tap 540 621')
time.sleep(2)
items = layout()
check('Logout -> Login page', any(i.get('resource-id') == 'btn_login' for i in items))

for item in items:
    if item.get('resource-id') == 'et_account':
        check('Account field cleared',
              str(item.get('text', '')) == '' or '请输入' in str(item.get('text', '')))

print(f'\n=== RESULTS: {pass_count} passed, {fail_count} failed ===')
