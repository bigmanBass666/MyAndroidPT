import subprocess, time

def shell(cmd):
    subprocess.run(['adb', '-s', 'emulator-5554'] + cmd.split(), timeout=5)

def input_text(text):
    subprocess.run(['adb', '-s', 'emulator-5554', 'shell', 'input', 'text', text], timeout=5)

def clear_field(field_id):
    """Tap field, long-press to select all, then delete"""
    shell(f'shell input tap 540 {field_id}')
    time.sleep(0.5)
    # Long press to select all, then delete
    for _ in range(3):
        subprocess.run(['adb', '-s', 'emulator-5554', 'shell', 'input', 'keyevent', '67'], timeout=2)

def clear_all(field_id):
    """Clear any field by sending many DEL keyevents"""
    shell(f'shell input tap 540 {field_id}')
    time.sleep(0.3)
    for _ in range(20):
        subprocess.run(['adb', '-s', 'emulator-5554', 'shell', 'input', 'keyevent', '67'], timeout=2)
        time.sleep(0.05)

def reg_register():
    """Check agree box and click register"""
    shell('shell input tap 155 2274')
    time.sleep(0.2)
    shell('shell input tap 540 2127')

def reg_check():
    """Print all fields"""
    result = subprocess.run(['android', 'layout', '--device', 'emulator-5554', '-p'],
                          capture_output=True, text=True)
    for line in result.stdout.strip().split('\n'):
        if not line.strip():
            continue
        try:
            d = __import__('json').loads(line)
            rid = d.get('resource-id', '')
            print(f'  {rid}: text="{d.get("text","")}"')
        except:
            pass

# Force stop to clear all state
shell('shell am force-stop com.ljx.pt')
time.sleep(0.5)
shell('shell am start -n com.ljx.pt/.RegisterActivity')
time.sleep(1.5)

print("=== Registering user01 with password 111111 ===")

# Fill account
clear_all(402)
time.sleep(0.2)
input_text('user01')
time.sleep(0.5)
print(f'Account: ', end='')
result = subprocess.run(['android', 'layout', '--device', 'emulator-5554', '-p'],
                      capture_output=True, text=True)
for line in result.stdout.strip().split('\n'):
    try:
        d = __import__('json').loads(line)
        if d.get('resource-id') == 'et_account':
            print(d.get('text',''))
            break
    except:
        pass

# Fill password
shell('shell input keyevent 61')
time.sleep(0.3)
clear_all(605)
time.sleep(0.2)
input_text('111111')
time.sleep(0.5)

# Fill password confirm
shell('shell input keyevent 61')
time.sleep(0.3)
clear_all(808)
time.sleep(0.2)
input_text('111111')
time.sleep(0.5)

# Register
reg_register()
time.sleep(2)

# Check result
result = subprocess.run(['android', 'layout', '--device', 'emulator-5554', '-p'],
                      capture_output=True, text=True)
for line in result.stdout.strip().split('\n'):
    if not line.strip():
        continue
    try:
        d = __import__('json').loads(line)
        rid = d.get('resource-id', '')
        if rid in ('tv_welcome', 'et_account'):
            print(f'Result: {rid} text="{d.get("text","")}"')
            break
    except:
        pass
