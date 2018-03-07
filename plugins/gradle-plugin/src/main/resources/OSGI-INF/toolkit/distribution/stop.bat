SET /P VALUE_FROM_FILE= < pid.lock
Taskkill /PID %VALUE_FROM_FILE% /F
DEL pid.lock
